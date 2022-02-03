package com.altbionics.GripTool.ui;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.digi.xbee.api.android.XBeeBLEDevice;
import com.altbionics.GripTool.MainActivity;
import com.altbionics.GripTool.R;

import java.util.ArrayList;


public class DevicesFragment extends Fragment {

    private ProgressBar scanProgress;
    private ListView deviceList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.devices_fragment, container, false);

        scanProgress = view.findViewById(R.id.scanProgress);
        scanProgress.setVisibility(View.INVISIBLE);

        deviceList = view.findViewById(R.id.deviceList);
        deviceList.setOnItemClickListener((parent, view1, position, id) -> {
            scanProgress.setVisibility(View.INVISIBLE);
            MainActivity.bluetoothAdapter.cancelDiscovery();
            String tempMAC = deviceList.getItemAtPosition(position).toString();
            tempMAC = tempMAC.substring(tempMAC.length() - 17);
            MainActivity.device = MainActivity.bluetoothAdapter.getRemoteDevice(tempMAC);
            MainActivity.xbeeDevice = new XBeeBLEDevice(requireContext(), MainActivity.device, MainActivity.PASSWORD);
            MainActivity.setDeviceInfo();
            Toast toast = Toast.makeText(getActivity(), MainActivity.device.getName() + " selected.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        });

        ImageButton refreshButton = view.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(v -> {
            if (v.getId() == R.id.refreshButton) {
                MainActivity.detectedAdapter.clear();
                MainActivity.detectedAdapter.notifyDataSetChanged();
                MainActivity.arrayListBluetoothDevices = new ArrayList<>();
                startSearching();
            }
        });

        deviceList.setAdapter(MainActivity.detectedAdapter);
        MainActivity.detectedAdapter.clear();
        if (!(MainActivity.tempDeviceName.equals("")))
            MainActivity.detectedAdapter.add(MainActivity.device.getName() + "\n" + MainActivity.device.getAddress());
        MainActivity.detectedAdapter.notifyDataSetChanged();

        return view;
    }

    private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getName() != null) {
                    if (device.getName().contains("Alt-Bionics")) {
                        if (MainActivity.arrayListBluetoothDevices.size() < 1) // this checks if the size of bluetooth device is 0,then add the
                        {
                            MainActivity.detectedAdapter.add(device.getName() + "\n" + device.getAddress());
                            MainActivity.arrayListBluetoothDevices.add(device);
                            MainActivity.detectedAdapter.notifyDataSetChanged();
                        } else {
                            boolean flag = true;    // flag to indicate that particular device is already in the list or not
                            for (int i = 0; i < MainActivity.arrayListBluetoothDevices.size(); i++) {
                                if (device.getAddress().equals(MainActivity.arrayListBluetoothDevices.get(i).getAddress())) {
                                    flag = false;
                                }
                            }
                            if (flag) {
                                MainActivity.detectedAdapter.add(device.getName() + "\n" + device.getAddress());
                                MainActivity.arrayListBluetoothDevices.add(device);
                                MainActivity.detectedAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                scanProgress.setVisibility(View.INVISIBLE);
            }
        }
    };

    private void startSearching() {
        AlertDialog.Builder bluetoothAlert = new AlertDialog.Builder(getContext());
        AlertDialog.Builder scanAlert = new AlertDialog.Builder(getContext());

        scanAlert.setTitle(getString(R.string.scan_alert_title));
        scanAlert.setMessage(getString(R.string.scan_alert_description));
        scanAlert.setCancelable(true);
        scanAlert.setPositiveButton("Ok", (dialog, which) -> dialog.cancel());

        bluetoothAlert.setTitle(getString(R.string.bluetooth_alert_title));
        bluetoothAlert.setMessage(getString(R.string.bluetooth_alert_description));
        bluetoothAlert.setCancelable(true);
        bluetoothAlert.setPositiveButton("Yes", (dialog, which) -> {
            MainActivity.bluetoothAdapter.enable();
            MainActivity.bluetoothAdapter.startDiscovery();
            scanProgress.setVisibility(View.VISIBLE);
        });
        bluetoothAlert.setNegativeButton("No", (dialog, id) -> {
            dialog.cancel();
            scanAlert.show();
        });

        Log.i("Log", "in the start searching method");
        IntentFilter intentDiscovery = new IntentFilter();
        intentDiscovery.addAction(BluetoothDevice.ACTION_FOUND);
        intentDiscovery.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        requireContext().registerReceiver(myReceiver, intentDiscovery);
        if (MainActivity.bluetoothAdapter.isEnabled()) {
            if (MainActivity.bluetoothAdapter.isDiscovering()) {
                MainActivity.bluetoothAdapter.cancelDiscovery();
            }
            MainActivity.bluetoothAdapter.startDiscovery();
            scanProgress.setVisibility(View.VISIBLE);
        } else {
            bluetoothAlert.show();
        }
    }
}
