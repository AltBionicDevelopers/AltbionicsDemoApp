package com.altbionics.GripTool.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.altbionics.GripTool.MainActivity;
import com.altbionics.GripTool.R;
import com.altbionics.GripTool.util.DialogInputInterface;
import com.altbionics.GripTool.util.Grip;
import com.altbionics.GripTool.util.GripItemTouchHelper;
import com.altbionics.GripTool.util.RecyclerViewAdapter;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;


public class GripsFragment extends Fragment implements RecyclerViewAdapter.OnGripListener {

    private static final String TAG = "GripList";
    public static RecyclerViewAdapter mRecyclerViewAdapter;
    public static RecyclerView mRecyclerView;
    private static String[] listFileNames;
    private static int fileNumber;
    public static int position;
    private View view;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.grips_fragment, container, false);
        initRecyclerView(view);

        ImageButton addButton = view.findViewById(R.id.addButton);

        TooltipCompat.setTooltipText(addButton, getString(R.string.add_grip_tooltip));
        addButton.setOnClickListener(v -> gripAlertDialog());

        return view;
    }

    private void initRecyclerView(@NotNull View view) {
        mRecyclerView = view.findViewById(R.id.grip_list);
        mRecyclerViewAdapter = new RecyclerViewAdapter(this);
        ItemTouchHelper.Callback callback = new GripItemTouchHelper(mRecyclerViewAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        mRecyclerViewAdapter.setTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerViewAdapter.notifyDataSetChanged();
    }

    //Reset grips button functionality
    public static void resetConfirm() {
        AlertDialog.Builder confirmResetBuilder = new AlertDialog.Builder(MainActivity.detectedAdapter.getContext());
        confirmResetBuilder.setTitle(R.string.confirm_reset_title);
        confirmResetBuilder.setMessage(R.string.confirm_reset_description);
        confirmResetBuilder.setCancelable(false);
        confirmResetBuilder.setPositiveButton("Yes", (dialog, which) -> {
            MainActivity.GripManagement.setDefaultValues();
            mRecyclerViewAdapter.notifyDataSetChanged();
            Toast toast = Toast.makeText(MainActivity.detectedAdapter.getContext(), "Grips reset", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        });
        confirmResetBuilder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
        confirmResetBuilder.show();
    }

    //Check if grip name already exists and prompt for overwrite
    private void setUserDataCapture(@NotNull String name, String description) {
        ArrayList<String> names = new ArrayList<>();

        for (Grip item : MainActivity.gripList) {
            names.add(item.getName());
        }


        if (name.isEmpty() || description.isEmpty()) {
            AlertDialog.Builder dataNull = new AlertDialog.Builder(getContext());
            dataNull.setTitle(R.string.data_null_title);
            dataNull.setMessage(R.string.data_null_description);
            dataNull.setCancelable(false);
            dataNull.setPositiveButton("Ok", (dialog, which) -> {
                dialog.cancel();
                gripAlertDialog();
            });
            dataNull.show();
            return;
        } else if (names.contains(name)) {
            AlertDialog.Builder dataExists = new AlertDialog.Builder(getContext());
            dataExists.setTitle(R.string.data_exists_title);
            dataExists.setMessage(R.string.data_exists_description);
            dataExists.setCancelable(false);
            dataExists.setPositiveButton("Yes", (dialog, which) -> {
                MainActivity.GripManagement.setGripName(names.indexOf(name), name, "0,0,0,0,0", description);
                mRecyclerViewAdapter.notifyDataSetChanged();
            });
            dataExists.setNegativeButton("No", (dialog, which) -> dialog.cancel());
            dataExists.show();

            return;
        } else {
            MainActivity.GripManagement.addGrip(name, "0,0,0,0,0", description);
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    //Add grip button functionality
    private void gripAlertDialog() {
        View v = RelativeLayout.inflate(getActivity(), R.layout.alert_dialog_grip_edit, null);
        EditText gripNameField = v.findViewById(R.id.gripNameField);
        EditText gripDescriptionField = v.findViewById(R.id.gripDescriptionField);

        gripNameField.setInputType(InputType.TYPE_CLASS_TEXT);
        gripDescriptionField.setInputType(InputType.TYPE_CLASS_TEXT);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.data_entry_title);
        builder.setMessage(R.string.data_entry_description);
        builder.setView(v);
        builder.setPositiveButton("Save", (dialog, which) ->
                setUserDataCapture(gripNameField.getText().toString().toUpperCase(),
                        gripDescriptionField.getText().toString()));
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    //Set the position clicked within the list
    @Override
    public void onGripClick(int position) {
        GripsFragment.position = position;
        new Handler().post(() -> Navigation.findNavController(view).navigate(R.id.nav_grip_settings));
        Log.d(TAG, "onGripClick: clicked on grip " + position);
    }

    //Save grip list button functionality
    public static void saveGripListAlert() {
        EditText[] input = new EditText[1];

        getListName(R.string.save_title, R.string.save_description, new DialogInputInterface() {
            @Override
            public View onBuildDialog() {
                input[0] = new EditText(MainActivity.detectedAdapter.getContext());
                input[0].setHint(R.string.save_and_import_hint);
                input[0].setSingleLine(true);
                return input[0];
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onResult(View v) {
                String fileName = input[0].getText().toString().toUpperCase().trim();

                if (!(fileName.equals(""))) {
                    try {
                        checkFile(fileName, R.string.file_exists_title, R.string.file_exists_description, new DialogInputInterface() {
                            @Override
                            public View onBuildDialog() {
                                return null;
                            }

                            @Override
                            public void onCancel() {

                            }

                            @Override
                            public void onResult(View v) {
                                createFile(fileName);
                                Toast toast = Toast.makeText(MainActivity.detectedAdapter.getContext(), fileName + " saved", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        });


                    } catch (Exception e) {
                        Log.d(TAG, "saveGripList: " + e.getMessage());
                    }
                } else
                    nullAlert(R.string.null_entry_title, R.string.null_entry_description);
            }
        });
    }

    //Custom dialog interface for user input filename capture
    public static void getListName(int dlgTitle, int dlgMessage, @NotNull final DialogInputInterface dlg) {
        AlertDialog.Builder fileNameAlert = new AlertDialog.Builder(MainActivity.detectedAdapter.getContext());
        fileNameAlert.setTitle(dlgTitle);
        fileNameAlert.setMessage(dlgMessage);
        final View v = dlg.onBuildDialog();
        if (v != null) {
            fileNameAlert.setView(v);
        }
        fileNameAlert.setPositiveButton("Ok", (dialog, whichButton) -> {
            dlg.onResult(v);
            dialog.dismiss();
            return;
        });

        fileNameAlert.setNegativeButton("Cancel", (dialog, which) -> {
            dlg.onCancel();
            dialog.dismiss();
            return;
        });
        fileNameAlert.show();
    }

    //Custom interface for capturing user overwrite selection
    public static void checkFile(String fileName, int dlgTitle, int dlgMessage, @NotNull final DialogInputInterface alert) {
        AlertDialog.Builder fileNameAlert = new AlertDialog.Builder(MainActivity.detectedAdapter.getContext());
        fileNameAlert.setTitle(dlgTitle);
        fileNameAlert.setMessage(dlgMessage);
        final View v = alert.onBuildDialog();
        if (v != null) {
            fileNameAlert.setView(v);
        }
        fileNameAlert.setPositiveButton("Yes", (dialog, whichButton) -> {
            alert.onResult(v);
            dialog.dismiss();
            return;
        });

        fileNameAlert.setNegativeButton("No", (dialog, which) -> {
            alert.onCancel();
            dialog.dismiss();
            return;
        });
        if (MainActivity.detectedAdapter.getContext().getFileStreamPath(fileName + ".txt").exists())
            fileNameAlert.show();
        else
            createFile(fileName);
    }

    //Alert for no file name entered
    public static void nullAlert(int title, int description) {
        AlertDialog.Builder nullEntryAlert = new AlertDialog.Builder(MainActivity.detectedAdapter.getContext());
        nullEntryAlert.setTitle(title);
        nullEntryAlert.setMessage(description);
        nullEntryAlert.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
        nullEntryAlert.show();
    }

    //Method for writing arrays to private app device memory
    private static void createFile(String fileName) {
        FileOutputStream fos = null;

        try {
            fos = MainActivity.detectedAdapter.getContext().openFileOutput(fileName + ".txt", Context.MODE_PRIVATE);
            fos.write(("").getBytes());
            fos.close();

            fos = MainActivity.detectedAdapter.getContext().openFileOutput(fileName + ".txt", Context.MODE_PRIVATE);

            for (Grip item : MainActivity.gripList) {
                fos.write((item.toString() + "\n").getBytes());
            }

            Toast toast = Toast.makeText(MainActivity.detectedAdapter.getContext(), "Grip files saved", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            Log.d(TAG, "createFile: Saved to " + MainActivity.detectedAdapter.getContext().getFilesDir() + "/" + fileName + ".txt");
        } catch (IOException e) {
            Log.d(TAG, "createFile: " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.d(TAG, "createFile: " + e.getMessage());
                }
            }
        }
    }

    //Method for importing grip list text file
    public static void importGripListAlert() {
        try {
            NumberPicker[] fileList = new NumberPicker[1];
            String[] fileName = new String[1];
            File dir = new File(String.valueOf(MainActivity.detectedAdapter.getContext().getFilesDir()));
            fileNumber = Objects.requireNonNull(dir.listFiles()).length;
            File[] dirFileList = dir.listFiles();
            listFileNames = new String[fileNumber];
            for (int i = 0; i < Objects.requireNonNull(dirFileList).length; i++) {
                listFileNames[i] = dirFileList[i].getName().replace(".txt", "");
            }

            ArrayList<String> tempList = new ArrayList<>(Arrays.asList(listFileNames));
            tempList.remove(MainActivity.MASTER_GRIP_LIST.replace(".txt", ""));
            listFileNames = tempList.toArray(new String[0]);
            fileNumber = listFileNames.length;

            selectFileAlert(R.string.import_title, R.string.import_description, new DialogInputInterface() {
                @Override
                public View onBuildDialog() {
                    Log.d(TAG, "onBuildDialog: entered onBuild");
                    fileList[0] = new NumberPicker(MainActivity.detectedAdapter.getContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    fileList[0].setLayoutParams(layoutParams);
                    fileList[0].setMinValue(0);
                    fileList[0].setMaxValue(fileNumber - 1);
                    fileList[0].setWrapSelectorWheel(true);
                    fileList[0].setDisplayedValues(listFileNames);

                    return fileList[0];
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onResult(View v) {
                    Log.d(TAG, "onResult: entered onResult");
                    fileName[0] = listFileNames[fileList[0].getValue()];
                    readFile(fileName[0]);
                    Toast toast = Toast.makeText(MainActivity.detectedAdapter.getContext(), fileName[0] + " imported", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "importGripList: " + e.getMessage());
            nullAlert(R.string.null_directory_title, R.string.null_directory_description);
        }
    }

    //Method for deleting grip list text file
    public static void deleteGripListAlert() {
        try {
            NumberPicker[] fileList = new NumberPicker[1];
            File dir = new File(String.valueOf(MainActivity.detectedAdapter.getContext().getFilesDir()));
            fileNumber = Objects.requireNonNull(dir.listFiles()).length;
            File[] dirFileList = dir.listFiles();
            listFileNames = new String[fileNumber];
            for (int i = 0; i < Objects.requireNonNull(dirFileList).length; i++) {
                listFileNames[i] = dirFileList[i].getName().replace(".txt", "");
            }

            ArrayList<String> tempList = new ArrayList<>(Arrays.asList(listFileNames));
            tempList.remove(MainActivity.MASTER_GRIP_LIST.replace(".txt", ""));
            listFileNames = tempList.toArray(new String[0]);
            fileNumber = listFileNames.length;

            selectFileAlert(R.string.delete_title, R.string.delete_description, new DialogInputInterface() {
                @Override
                public View onBuildDialog() {
                    Log.d(TAG, "onBuildDialog: entered onBuild");
                    fileList[0] = new NumberPicker(MainActivity.detectedAdapter.getContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    fileList[0].setLayoutParams(layoutParams);
                    fileList[0].setMinValue(0);
                    fileList[0].setMaxValue(fileNumber - 1);
                    fileList[0].setWrapSelectorWheel(true);
                    fileList[0].setDisplayedValues(listFileNames);

                    return fileList[0];
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onResult(View v) {
                    File file = new File(String.valueOf(v.getContext().getFileStreamPath(listFileNames[fileList[0].getValue()] + ".txt")));
                    String fileName = listFileNames[fileList[0].getValue()];

                    if (file.delete()) {
                        Toast toast = Toast.makeText(MainActivity.detectedAdapter.getContext(), fileName + " deleted", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        Log.d(TAG, "onResult: file deleted.");
                    } else {
                        Toast toast = Toast.makeText(MainActivity.detectedAdapter.getContext(), "Error: " + fileName + " note deleted", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        Log.d(TAG, "onResult: file not deleted.");
                    }
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "deleteGripListAlert: " + e.getMessage());
            nullAlert(R.string.null_directory_title, R.string.null_directory_description);
        }
    }

    //Custom interface for selecting grip list text file to import/delete
    public static void selectFileAlert(int dlgTitle, int dlgMessage, @NotNull final DialogInputInterface fileAlert) {
        AlertDialog.Builder fileNameAlert = new AlertDialog.Builder(MainActivity.detectedAdapter.getContext());
        fileNameAlert.setTitle(dlgTitle);
        fileNameAlert.setMessage(dlgMessage);
        final View v = fileAlert.onBuildDialog();
        if (v != null) {
            fileNameAlert.setView(v);
        }
        fileNameAlert.setPositiveButton("Select", (dialog, whichButton) -> {
            fileAlert.onResult(v);
            dialog.dismiss();
            return;
        });

        fileNameAlert.setNegativeButton("Cancel", (dialog, which) -> {
            fileAlert.onCancel();
            dialog.dismiss();
            return;
        });
        fileNameAlert.show();
    }

    //Method for parsing selected grip list text file
    private static void readFile(String string) {
        String fileName = string + ".txt";
        String[] tempGripArray;


        //Read from file into arrays
        try {
            File file = new File(String.valueOf(MainActivity.detectedAdapter.getContext().getFileStreamPath(fileName)));
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuffer = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append("\n");
            }
            fileReader.close();
            tempGripArray = stringBuffer.toString().split("\n");

            MainActivity.gripList.clear();
            for (String grip : tempGripArray) {
                MainActivity.gripList.add(new Grip(grip));
            }

            mRecyclerViewAdapter.notifyDataSetChanged();
        } catch (IOException e) {
            Log.d(TAG, "readFile: Error handling file " + e.getMessage()
            );
        }
    }
}