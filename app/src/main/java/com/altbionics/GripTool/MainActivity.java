package com.altbionics.GripTool;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.altbionics.GripTool.ui.GripsFragment;
import com.altbionics.GripTool.util.Grip;
import com.digi.xbee.api.android.XBeeBLEDevice;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public static Resources mResources;
    public String appStartup;
    public String currentTheme;
    public static String tempDeviceName;
    public static String tempDeviceMAC;
    public static SharedPreferences mPreferences;
    public static SharedPreferences.Editor mEditor;

    public static final String PASSWORD = "zaq1ZAQ!";
    public static BluetoothAdapter bluetoothAdapter = null;
    public static BluetoothLeScanner bluetoothLeScanner = null;

    public static BluetoothDevice device = null;
    public static ArrayAdapter<String> detectedAdapter;
    public static ArrayList<BluetoothDevice> arrayListBluetoothDevices = null;
    public static XBeeBLEDevice xbeeDevice;

    private DrawerLayout drawerLayout;
    public NavController navController;
    private NavigationView navigationView;
    private AppBarConfiguration mAppBarConfiguration;
    public static final String TAG = "MainActivity"; //TAG ID for the main activity
    private static final int REQUEST_PERMISSIONS = 1; //Permission ID

    public static ArrayList<Grip> gripList = new ArrayList<>();
    public static final String MASTER_GRIP_LIST = "MASTER_GRIP_LIST.txt";
    public static final String[] DEFAULT_VALUES = {"POWER::0,0,0,0,0::Default power grip::false",
            "PINCH::0,0,180,180,180::Default pinch grip::false",
            "CLAW::180,90,90,90,90::Default claw grip::false"};
    private int counter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        mResources = getResources();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEditor = mPreferences.edit();
        checkSharedPreferences();

        if (!appStartup.equals("true")) {
            appStartup = "true";
            mEditor.putString(getString(R.string.appStartup), appStartup);
            mEditor.putString(getString(R.string.currentTheme), "Fire");
            mEditor.apply();
            recreate();

            GripManagement.setDefaultValues();
        } else {
            getGripFiles();
        }

        currentTheme = mPreferences.getString(getString(R.string.currentTheme), "Fire");
        if (currentTheme.equals("Fire"))
            setTheme(R.style.Fire);
        else
            setTheme(R.style.Electric);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        initNavDrawer();
        initBluetooth();
        getDeviceInfo(this);

        Log.d(TAG, "onCreate: Starting.");
    }

    private void checkSharedPreferences() {
        appStartup = mPreferences.getString(getString(R.string.appStartup), "false");
        Log.d(TAG, "checkSharedPreferences: " + appStartup);
    }

    public static void setDeviceInfo() {
        String selectedDeviceName;
        String selectedDeviceMAC;

        if (!(device.getName().equals(""))) {
            selectedDeviceName = device.getName();
            mEditor.putString(mResources.getString(R.string.selectedDeviceName), selectedDeviceName);
            mEditor.apply();
            Log.d(TAG, "setGripFiles: " + selectedDeviceName);

            selectedDeviceMAC = device.getAddress();
            mEditor.putString(mResources.getString(R.string.selectedDeviceMAC), selectedDeviceMAC);
            mEditor.apply();
            Log.d(TAG, "setGripFiles: " + selectedDeviceMAC);
        }
    }

    public static void getDeviceInfo(Context context) {
        tempDeviceName = mPreferences.getString(mResources.getString(R.string.selectedDeviceName), "");
        tempDeviceMAC = mPreferences.getString(mResources.getString(R.string.selectedDeviceMAC), "");

        try {
            if (!(tempDeviceName.equals(""))) {
                if (bluetoothAdapter.isEnabled()) {
                    device = bluetoothAdapter.getRemoteDevice(tempDeviceMAC);
                } else if (!(bluetoothAdapter.isEnabled())) {
                    bluetoothAdapter.enable();
                    device = bluetoothAdapter.getRemoteDevice(tempDeviceMAC);
                }

                MainActivity.xbeeDevice = new XBeeBLEDevice(context, device, PASSWORD);
            }
        } catch (Exception e) {
            Log.d(TAG, "getDeviceInfo: " + e.getMessage());
        }
    }

    public void setGripFiles() {
        FileOutputStream fos = null;

        try {
            fos = openFileOutput(MASTER_GRIP_LIST, Context.MODE_PRIVATE);
            fos.write(("").getBytes());
            fos.close();

            fos = openFileOutput(MASTER_GRIP_LIST, Context.MODE_PRIVATE);

            //Write grips to current grip list file
            for (Grip item : gripList) {
                fos.write((item.toString() + "\n").getBytes());
            }

            Log.d(TAG, "setGripFiles: Saved to " + getFilesDir() + "/" + MASTER_GRIP_LIST);
        } catch (IOException e) {
            Log.d(TAG, "setGripFiles: " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.d(TAG, "setGripFiles: " + e.getMessage());
                }
            }
        }
    }

    public void getGripFiles() {
        String[] tempGripArray;
        gripList.clear();

        try {
            File file = new File(String.valueOf(getFileStreamPath(MASTER_GRIP_LIST)));
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

            for (String grip : tempGripArray) {
                gripList.add(new Grip(grip));
            }
        } catch (IOException e) {
            Log.d(TAG, "readFile: Error handling file " + e.getMessage()
            );
        }
    }

    private void initNavDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull @NotNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull @NotNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull @NotNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_grips, R.id.nav_grip_settings, R.id.nav_devices)
                .setOpenableLayout(drawerLayout)
                .build();
        navController = getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.theme_switch); // This is the menu item that contains your switch
        ImageButton themeToggle = menuItem.getActionView().findViewById(R.id.theme_toggle);
        themeToggle.setOnClickListener(v -> {
            currentTheme = mPreferences.getString(getString(R.string.currentTheme), "");
            if (currentTheme.equals("Electric"))
                mEditor.putString(getString(R.string.currentTheme), "Fire").apply();
            else
                mEditor.putString(getString(R.string.currentTheme), "Electric").apply();
            recreate();
        });
    }

    private void initBluetooth() {
        checkPermissions();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        arrayListBluetoothDevices = new ArrayList<>();
        detectedAdapter = new ArrayAdapter<>(this, R.layout.device_list_layout, R.id.list_text_format);
    }


    //Permissions checks
    @AfterPermissionGranted(REQUEST_PERMISSIONS)
    public void checkPermissions() {
        //Bluetooth, Bluetooth admin, and location are required for Bluetooth scanning
        String[] perms = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION};

        //If permissions don't exist, request them
        if (!EasyPermissions.hasPermissions(this, perms)) {

            AlertDialog.Builder permissionsAlert = new AlertDialog.Builder(this);
            AlertDialog.Builder cancellationAlert = new AlertDialog.Builder(this);

            permissionsAlert.setTitle(getString(R.string.permissions_needed_title));
            permissionsAlert.setMessage(getString(R.string.permissions_needed_description));
            permissionsAlert.setCancelable(true);
            permissionsAlert.setPositiveButton("Ok", (dialog, which) -> {
                requestPermissions(perms, REQUEST_PERMISSIONS); //Check to see if permissions have been granted
            });
            permissionsAlert.setNegativeButton("Cancel", (dialog, id) -> {
                dialog.cancel();
                cancellationAlert.show();
            });

            cancellationAlert.setTitle(R.string.confirm_title);
            cancellationAlert.setMessage(R.string.confirm_description);
            cancellationAlert.setCancelable(true);
            cancellationAlert.setPositiveButton("Yes",
                    (dialog1, id1) -> dialog1.cancel());
            cancellationAlert.setNegativeButton("Retry",
                    (dialog12, id12) -> requestPermissions(perms, REQUEST_PERMISSIONS));

            permissionsAlert.show();
        }
    }

    //Override the default request for permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        try {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else
                super.onBackPressed();
        } catch (Exception e) {
            Log.d(TAG, "onBackPressed: " + e.getMessage());
        }

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent browserIntent;
        drawerLayout.closeDrawer(GravityCompat.START);
        NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.nav_grips, false).build();
        switch (item.getItemId()) {
            case R.id.nav_grips:
                if (isValidDestination(R.id.nav_grips))
                    new Handler().post(() -> navController.navigate(R.id.nav_grips, null, navOptions));
                break;
            case R.id.nav_devices:
                if (isValidDestination(R.id.nav_devices))
                    new Handler().post(() -> navController.navigate(R.id.nav_devices, null, navOptions));
                break;
            case R.id.theme_switch:
                MenuItem menuItem = navigationView.getMenu().findItem(R.id.theme_switch); // This is the menu item that contains your switch
                ImageButton themeToggle = menuItem.getActionView().findViewById(R.id.theme_toggle);
                themeToggle.setOnClickListener(v -> {
                    currentTheme = mPreferences.getString(getString(R.string.currentTheme), "");
                    if (currentTheme.equals("Electric"))
                        mEditor.putString(getString(R.string.currentTheme), "Fire").apply();
                    else
                        mEditor.putString(getString(R.string.currentTheme), "Electric").apply();
                    recreate();
                });
                break;
            case R.id.action_about_us:
                browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.about_us_link)));
                startActivity(browserIntent);
                break;
            case R.id.action_privacy_policy:
                browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.privacy_policy_link)));
                startActivity(browserIntent);
                break;
            case R.id.reset_grips:
                GripsFragment.resetConfirm();
                break;
            case R.id.save_list:
                GripsFragment.saveGripListAlert();
                break;
            case R.id.import_list:
                GripsFragment.importGripListAlert();
                break;
            case R.id.delete_list:
                GripsFragment.deleteGripListAlert();
                break;

        }
        counter = 0;
        item.setChecked(true);
        return true;
    }

    private boolean isValidDestination(int destination) {
        return destination != Objects.requireNonNull(navController.getCurrentDestination()).getId();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(@NotNull View view) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + getString(R.string.easter_egg)));
        appIntent.putExtra("force_fullscreen", true);
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + getString(R.string.easter_egg)));
        webIntent.putExtra("force_fullscreen", true);

        if (view.getId() == R.id.icon_altbionics) {
            counter++;
            if (counter == 7) {
                try {
                    startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    startActivity(webIntent);
                }
                counter = 0;
            }
        } else {
            counter = 0;
        }
    }

    public static class GripManagement {

        public static void setDefaultValues() {
            //ensure all data is cleared
            gripList.clear();
            gripList.add(new Grip(DEFAULT_VALUES[0]));
            gripList.add(new Grip(DEFAULT_VALUES[1]));
            gripList.add(new Grip(DEFAULT_VALUES[2]));

        }

        public static String getGripName(int position) {
            return gripList.get(position).getName();
        }

        @NotNull
        public static String getGripValues(int position) {
            int[] values = new int[5];

            values[0] = gripList.get(position).getThumb();
            values[1] = gripList.get(position).getIndex();
            values[2] = gripList.get(position).getMiddle();
            values[3] = gripList.get(position).getRing();
            values[4] = gripList.get(position).getPinky();

            String tempValues = "";
            tempValues = tempValues.concat(values[0] + "," + values[1]
                    + "," + values[2] + "," + values[3] + "," + values[4]);

            return tempValues;
        }

        public static String getGripDescription(int position) {
            return gripList.get(position).getDescription();
        }

        public static boolean getGripStatus(int position) {
            return gripList.get(position).getUploaded();
        }

        public static void setGripName(int position, String name, @NotNull String values, String description) {
            gripList.get(position).setName(name);
            gripList.get(position).setDescription(description);
            gripList.get(position).setThumb(Byte.parseByte(values.split(",")[0]));
            gripList.get(position).setIndex(Byte.parseByte(values.split(",")[1]));
            gripList.get(position).setMiddle(Byte.parseByte(values.split(",")[2]));
            gripList.get(position).setRing(Byte.parseByte(values.split(",")[3]));
            gripList.get(position).setPinky(Byte.parseByte(values.split(",")[4]));
        }

        public static void removeGrip(int position) {
            gripList.remove(position);
        }

        public static void addGrip(String name, String values, String description) {
            gripList.add(new Grip(name + "::" + values + "::" + description + "::false"));
        }

        public static void addGrip(int position, String name, String values, String description, boolean status) {
            gripList.add(position, new Grip(name + "::" + values + "::" + description + "::" + status));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        setGripFiles();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setGripFiles();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setGripFiles();
    }

    @NonNull
    private NavController getNavController() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (!(fragment instanceof NavHostFragment)) {
            throw new IllegalStateException("Activity " + this
                    + " does not have a NavHostFragment");
        }
        return ((NavHostFragment) fragment).getNavController();
    }
}