package com.altbionics.GripTool.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.altbionics.GripTool.MainActivity;
import com.altbionics.GripTool.R;
import com.altbionics.GripTool.ui.GripsFragment;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IUserDataRelayReceiveListener;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


public class DataManagement extends AppCompatActivity {
    private static final String TAG = "DataManagement";
    private final Context context;
    private boolean ackReceived = false;
    private static boolean userCancelled = false;
    private static boolean listenerActive = false;
    private static final String MSG_ACK = "OK";
    private static final int MAX_RETRIES = 3;
    private static final int ACK_TIMEOUT = 10000;
    private static final int CANCEL_TIMEOUT = 5000;
    private static final int CANCEL_TIMER_TICKS = 1000;
    private static final int WAIT_TIMEOUT = 1000;
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    private static AlertDialog uploadAlert;
    private volatile Thread blinker;
    private final MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
    private final IUserDataRelayReceiveListener relayListener = userDataRelayMessage -> {
        String receivedData = userDataRelayMessage.getDataString();

        for(Grip item : MainActivity.gripList){
            item.setUploaded(false);
            MainActivity.detectedAdapter.notifyDataSetChanged();
        }

        if (receivedData.equals(MSG_ACK)) {
            ackReceived = true;
            synchronized (lock1) {
                lock1.notify();
            }
        }
    };


    public DataManagement(Context context) {
        this.context = context;
    }


    //Public method for passing data to the selected device
    public void sendData(String dataToSend) {
        userCancelled = false;
        displayUploadStatus(); //Open upload progress spinner
        startUploadProcess(runUploadProcess(dataToSend));
    }


    private void startUploadProcess(Runnable runnable) {
        blinker = new Thread(runnable);
        blinker.start();
    }


    private void stopUploadProcess() {
        blinker = null;
    }

    @NotNull
    @Contract(pure = true)
    private Runnable runUploadProcess(String data) {
        return () -> {
            Thread startUpload = Thread.currentThread();
            while (blinker == startUpload) {
                try {
                    if (MainActivity.xbeeDevice.isOpen()) {
                        Log.d(TAG, "runUploadProcess: Reopening device connection");
                        MainActivity.xbeeDevice.close();
                        synchronized (lock2) {
                            Log.d(TAG, "runUploadProcess: " +
                                    "Pausing for system response");
                            lock2.wait(WAIT_TIMEOUT);
                            lock2.notify();
                        }
                    }
                    MainActivity.xbeeDevice.open();
                    sendDataAndWaitResponse(data.getBytes());
                } catch (XBeeException | InterruptedException e) {
                    e.printStackTrace();
                    Log.d(TAG, "sendDataToDevice: " + e.getMessage());
                    uploadAlert.cancel();
                    if (!userCancelled)
                        showErrorConnecting();
                } finally {
                    if (listenerActive)
                        removeUserDataListener();
                    if (MainActivity.xbeeDevice.isOpen()) {
                        MainActivity.xbeeDevice.close();
                        Log.d(TAG, "connectToDevice: Device connection closed");
                    }
                }
            }
        };
    }

    //Logic for sending data and waiting for MCU acknowledgement
    synchronized void sendDataAndWaitResponse(byte[] data) {
        ackReceived = false;

        //Try to transmit the data
        addUserDataListener(); //Listener for device acknowledgement response
        for (int retry = 0; retry < MAX_RETRIES; retry++) {
            if (userCancelled)
                return;
            if (ackReceived)
                break;
            try {
                MainActivity.xbeeDevice.sendSerialData(data);
            } catch (XBeeException e) {
                e.printStackTrace();
                if (!userCancelled)
                    showUploadError();
                return;
            }

            // Wait until the ACK = OK is received
            try {
                synchronized (lock1) {
                    Log.d(TAG, "sendDataAndWaitResponse: " +
                            "timer implemented and waiting for response");
                    lock1.wait(ACK_TIMEOUT);
                    if (ackReceived)
                        break;
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "sendDataAndWaitResponse: upload interrupt: Error message - "
                        + e.getMessage());
                break;
            }
        }

        if (!userCancelled) {
            if (!ackReceived)
                showAcknowledgeError();
            else {
                Log.d(TAG, "sendDataAndWaitResponse: Grip position = " +GripsFragment.position);
                MainActivity.gripList.get(GripsFragment.position).setUploaded(true);
                runOnUiThread(() -> GripsFragment.mRecyclerViewAdapter.notifyDataSetChanged());
                showUploadSuccess();
            }
        }

        removeUserDataListener();

    }

    private void displayUploadStatus() {
        AlertDialog.Builder uploadAlertBuilder = new AlertDialog.Builder(context);
        uploadAlertBuilder.setTitle(R.string.sending_data_title);
        uploadAlertBuilder.setMessage(R.string.sending_data_description);
        uploadAlertBuilder.setView(R.layout.upload_progress_alert);
        uploadAlertBuilder.setPositiveButton("Cancel", (dialog, which) -> {
            Log.d(TAG, "displayUploadStatus: Cancel button pressed");
            userCancelled = true;
            uploadAlert.cancel();
        });
        uploadAlertBuilder.setOnCancelListener(dialog -> {
            if(userCancelled)
                showCancelAlert();
            stopUploadProcess();
        });
        uploadAlertBuilder.setOnDismissListener(dialog -> {
            if(userCancelled)
                showCancelAlert();
            stopUploadProcess();
        });

        uploadAlert = uploadAlertBuilder.show();
        uploadAlert.setCanceledOnTouchOutside(false);
        uploadAlert.setCancelable(false);
    }

    //Error connecting message builder
    private void showErrorConnecting() {
        Log.d(TAG, "showErrorConnecting: Displayed");
        messagePopup(R.string.error_connecting_title, R.string.error_connecting_description);
    }

    //Upload error message builder
    private void showUploadError() {
        Log.d(TAG, "showUploadError: Displayed");
        messagePopup(R.string.error_sending_title, R.string.error_sending_description);
    }

    //Acknowledgement error message builder
    private void showAcknowledgeError() {
        Log.d(TAG, "showAcknowledgeError: Displayed");
        messagePopup(R.string.error_sending_title, R.string.error_sending_file_ack);
    }

    //Success message builder
    private void showUploadSuccess() {
        Log.d(TAG, "showUploadSuccess: Displayed");
        messagePopup(R.string.upload_complete_title, R.string.upload_complete_description);
    }

    //Popup message builder
    private void messagePopup(int titleID, int messageID) {
        Log.d(TAG, "messagePopup: Method called");
        uploadAlert.cancel();
        ((Activity) context).runOnUiThread(() -> new AlertDialog.Builder(context)
                .setTitle(context.getString(titleID))
                .setMessage(context.getString(messageID))
                .setPositiveButton(android.R.string.ok, null)
                .show());
    }

    private void showCancelAlert() {
        ((Activity) context).runOnUiThread(() -> {
            final AlertDialog.Builder cancelAlertBuilder = new AlertDialog.Builder(context);
            cancelAlertBuilder.setTitle(R.string.cancelling_alert_title);
            cancelAlertBuilder.setMessage(R.string.cancelling_alert_description);
            cancelAlertBuilder.setView(R.layout.upload_progress_alert);
            final AlertDialog cancelAlert = cancelAlertBuilder.create();
            cancelAlert.show();
            cancelAlert.setCanceledOnTouchOutside(false);

            new CountDownTimer(CANCEL_TIMEOUT, CANCEL_TIMER_TICKS) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    cancelAlert.dismiss();
                }
            }.start();
        });
    }


    //Add Listener for MCU data acknowledgement
    private void addUserDataListener() {
        listenerActive = true;
        Log.d(TAG, "addUserDataListener: User data listener added");
        MainActivity.xbeeDevice.addUserDataRelayListener(relayListener);
        context.registerReceiver(myBroadcastReceiver,
                new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
    }

    //Remove Listener for MCU data acknowledgement
    private void removeUserDataListener() {
        listenerActive = false;
        Log.d(TAG, "removeUserDataListener: User data listener removed");
        try {
            MainActivity.xbeeDevice.removeUserDataRelayListener(relayListener);
            context.unregisterReceiver(myBroadcastReceiver);
        } catch(IllegalArgumentException e) {
            Log.d(TAG, "removeUserDataListener: " + e.getMessage());
        }
    }

    //Custom class for determining if the connection is lost during data transfer
    private static class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, @NotNull Intent intent) {
            Log.d(TAG, "onReceive: Receive event occurred");
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
                Log.d(TAG, "onReceive: Connection error during receive event");
                if (MainActivity.xbeeDevice.isOpen()) {
                    MainActivity.xbeeDevice.close();
                    Log.d(TAG, "onReceive: Device connection closed");
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        addUserDataListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeUserDataListener();
    }
}