package saminofal.cerberusdrone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.jjoe64.graphview.series.DataPoint;


public class MainActivity extends AppCompatActivity {


    /*
     * ********************************** *********** **********************************
     * ********************************** Declaration **********************************
     * ********************************** *********** **********************************
     */
    // Layout elements
    public TextView AppTitle;
    public View BarSep;
    public TabLayout tabLayout;
    public ViewPager mViewPager;
    public String TAG;
    public BluetoothAdapter mBluetoothAdapter;
    public static boolean BluetoothDeviceState;
    public int ERROR_DIALOG_REQUEST = 9001;
    public ProgressDialog mmProgressDialog;
    public Drawable Backgrounds[] = new Drawable[2];
    public String inString;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public int RSSI;
    public StringBuilder messages; // Read incoming messages and post them using StringBuilder
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static boolean ConActivityState;
    private static final int Control_Code = 1;
    private static MainActivity instance;
    public static MainActivity getInstance() {
        return instance;
    }
    public Handler AckHandler = new Handler();
    public Runnable AckRunnable;
    public static boolean GraphState = false;
    public double graph2LastXValue = 5d;
    // Class elements
    public BluetoothConnectionService mBluetoothConnection;
    public BluetoothDevice mBTDevice;
    public DeviceListAdapter mDeviceListAdapter;
    public SectionsPageAdapter mSectionsPageAdapter;





    /*
     * ********************************** ******************* **********************************
     * ********************************** Broadcast Receivers **********************************
     * ********************************** ******************* **********************************
     */
    /**
     * Create a BroadcastReceiver for ACTION_FOUND
     */
    public final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        FragmentSettings.Bluetooth_Switch.setChecked(false);
                        FragmentSettings.Bluetooth_SubLayout.setVisibility(View.GONE);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        FragmentSettings.Bluetooth_Switch.setChecked(true);
                        FragmentSettings.Bluetooth_SubLayout.setVisibility(View.VISIBLE);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };


    /**
     * Broadcast Receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire.
     */
    public final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            assert action != null;
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };



    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    public BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                RSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view,
                        mBTDevices, RSSI);
                FragmentSettings.lvBLDevices.setAdapter(mDeviceListAdapter);
                FragmentSettings.lvBLDevices.setVisibility(View.VISIBLE);
                mmProgressDialog.dismiss();
            }
        }
    };


    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            assert action != null;
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    mBTDevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };


    public final BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //String Message = mBluetoothConnection.RawData();
            String text = intent.getStringExtra("theMessage");
            messages.append(text);
            //FragmentData.RXData.setText(messages);
            try {
                inString = messages.toString();
                if(inString.contains("D") && inString.contains(".")) {
                    messages.setLength(0);
                    inString = inString.substring(inString.indexOf('D')+1, inString.indexOf('.'));
                    FragmentData.RXData.setText(inString);
                    if(isInteger(inString)) {
                        if (GraphState) {
                            PlotPoints(Integer.parseInt(inString));
                        }
                    }
                }

                else if(inString.contains("G") && inString.contains(".")) {
                    messages.setLength(0);
                    inString = inString.substring(inString.indexOf('G')+1, inString.indexOf('.'));
                    FragmentData.RXData.setText(inString);
                    if(isInteger(inString)) {
                        if(Integer.parseInt(inString) == 404) {
                            FragmentData.GPS_Fix_State.setTextColor(getResources().getColor(R.color.Red));
                            FragmentData.GPS_Fix_State.setText("No fix");
                        }
                        else if (Integer.parseInt(inString) == 101) {
                            FragmentData.GPS_Fix_State.setTextColor(getResources().getColor(R.color.NeonGreen));
                            FragmentData.GPS_Fix_State.setText("Fixed");
                        }
                    }
                }
            }
            catch (Exception e) {
                messages.setLength(0);
            }
        }
    };


    public final BroadcastReceiver mBroadcastReceiver6 = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")@Override
        public void onReceive(Context context, Intent intent) {
            try {
                BluetoothDeviceState = true;
                FragmentSettings.BL_CancelCon.setVisibility(View.VISIBLE);

                TransitionDrawable crossFade = new TransitionDrawable(Backgrounds);
                BarSep.setBackground(crossFade);
                crossFade.startTransition(1000);

                // Reset layouts
                FragmentHome.FragHomeLayoutMsg.setVisibility(View.GONE);
                FragmentData.FragDataLayoutMsg.setVisibility(View.GONE);
                FragmentData.FragDataLayoutCont.setVisibility(View.VISIBLE);
                FragmentHome.FragHomeLayoutCont.setVisibility(View.VISIBLE);

                mmProgressDialog.dismiss();

                Toast.makeText(MainActivity.this, "Device Connected", Toast.LENGTH_SHORT).show();

                /*// Initialize Acknowledge Runnable
                AckRunnable = new Runnable() {
                    @Override
                    public void run() {
                        SendChar('@');
                        AckHandler.postDelayed(this, 1000);
                    }
                };
                // Initialize Acknowledge handler
                AckHandler.postDelayed(AckRunnable, 1000);*/
            }
            catch (Exception e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };


    public final BroadcastReceiver mBroadcastReceiver7 = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")@Override
        public void onReceive(Context context, Intent intent) {
            try {
                BluetoothDeviceState = false;
                FragmentSettings.BL_CancelCon.setVisibility(View.GONE);

                TransitionDrawable crossFade = new TransitionDrawable(Backgrounds);
                BarSep.setBackground(crossFade);
                crossFade.startTransition(0);
                crossFade.reverseTransition(1000);

                // Reset layouts
                FragmentHome.FragHomeLayoutMsg.setVisibility(View.VISIBLE);
                FragmentData.FragDataLayoutMsg.setVisibility(View.VISIBLE);
                FragmentData.FragDataLayoutCont.setVisibility(View.GONE);
                FragmentHome.FragHomeLayoutCont.setVisibility(View.GONE);

                // Close controller activity if active
                if(ConActivityState) {
                    ControlActivity.ConActivity.finish();
                }

                mmProgressDialog.dismiss();

                // Reset graph plotter settings
                FragmentData.GraphMsgLayout.setVisibility(View.VISIBLE);
                FragmentData.Graph_Switch.setChecked(false);
                GraphState = false;

                // Remove Acknowledge handler callback
                //AckHandler.removeCallbacks(AckRunnable);

                Toast.makeText(MainActivity.this, "Device Disconnected", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };





    /*
     * ********************************** ******************* **********************************
     * ********************************** Lifecycle Callbacks **********************************
     * ********************************** ******************* **********************************
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        instance = this;

        try {
            // MainActivity elements initialization
            mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
            mViewPager = findViewById(R.id.container);
            setupViewPager(mViewPager);
            BarSep = findViewById(R.id.BarSep);
            AppTitle = findViewById(R.id.AppTitle);
            tabLayout = findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(mViewPager);
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBTDevices = new ArrayList<>();
            messages = new StringBuilder();
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver5
                    , new IntentFilter("incomingMessage"));


            // Font styling - Ailerons font
            Typeface AileronsFont = Typeface.createFromAsset(getAssets(), "fonts/Ailerons.ttf");
            AppTitle.setTypeface(AileronsFont);
            for(int i = 0; i < tabLayout.getTabCount(); i++) {
                @SuppressLint("InflateParams") TextView tv = (TextView) LayoutInflater.from(this)
                        .inflate(R.layout.custom_tablayout, null);
                tv.setTypeface(AileronsFont);
                Objects.requireNonNull(tabLayout.getTabAt(i)).setCustomView(tv);
            }

            // Drawable transition
            Resources res = getResources();
            Backgrounds[0] = res.getDrawable(R.drawable.gradient_bg2);
            Backgrounds[1] = res.getDrawable(R.drawable.gradient_bg4);

            // Broadcasts when bond state changes (ie:pairing)
            IntentFilter filter_1 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver4, filter_1);
            // Broadcasts when device is connected
            IntentFilter filter_2 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
            registerReceiver(mBroadcastReceiver6, filter_2);
            // Broadcasts when device is disconnected
            IntentFilter filter_3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            registerReceiver(mBroadcastReceiver7, filter_3);
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        finish();
        System.exit(0);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
        unregisterReceiver(mBroadcastReceiver5);
        unregisterReceiver(mBroadcastReceiver6);
        unregisterReceiver(mBroadcastReceiver7);
        instance = null;
    }





    /*
     * ********************************** ********* **********************************
     * ********************************** Viewpager **********************************
     * ********************************** ********* **********************************
     */
    public void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentHome(), "Home");
        adapter.addFragment(new FragmentData(), "Data");
        adapter.addFragment(new FragmentSettings(), "Settings");
        viewPager.setAdapter(adapter);
    }





    /*
     * ********************************** ********* **********************************
     * ********************************** Bluetooth **********************************
     * ********************************** ********* **********************************
     */
    public boolean checkBT() {
        return mBluetoothAdapter.isEnabled();
    }


    public boolean enableDisableBT() {
        if (mBluetoothAdapter == null) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle("Error")
                    .setMessage("Your device does not have bluetooth capabilities.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
            return true;
        }
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
            return false;
        }
        return false;
    }


    public void BluetoothDiscovery() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        // Discovery in seconds
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);
    }


    public void BluetoothScan() {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if(mBluetoothAdapter.isDiscovering()){
            // Clear list adapter
            try {
                if(mDeviceListAdapter != null) {
                    mDeviceListAdapter.clearList();
                    FragmentSettings.lvBLDevices.setAdapter(null);
                }
            }
            catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
            mmProgressDialog = new ProgressDialog(MainActivity.this, R.style.ProgDialigStyle);
            mmProgressDialog.setTitle("Searching for devices");
            mmProgressDialog.setMessage("Please Wait...");
            mmProgressDialog.setCancelable(false);
            mmProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mmProgressDialog.dismiss();
                            mBluetoothAdapter.cancelDiscovery();
                        }
                    });
            mmProgressDialog.show();
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");
            // Check BT permissions in manifest
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){
            // Clear list adapter
            try {
                if(mDeviceListAdapter != null) {
                    mDeviceListAdapter.clearList();
                    FragmentSettings.lvBLDevices.setAdapter(null);
                }
            }
            catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
            mmProgressDialog = new ProgressDialog(MainActivity.this, R.style.ProgDialigStyle);
            mmProgressDialog.setTitle("Searching for devices");
            mmProgressDialog.setMessage("Please Wait...");
            mmProgressDialog.setCancelable(false);
            mmProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mmProgressDialog.dismiss();
                            mBluetoothAdapter.cancelDiscovery();
                        }
                    });
            mmProgressDialog.show();
            // Check BT permissions in manifest
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }


    public void BluetoothConnect(int i) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        // Create the bond.
        // NOTE: Requires API 17+
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(i).createBond();

            mBTDevice = mBTDevices.get(i);
            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
            // Begin the connection
            startConnection();
        }
    }


    /** create method for starting connection
     * Remember the connection will fail and the app will crash if you haven't paired first
     */
    public void startConnection(){
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }


    /**
     * Starting chat service method
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        mBluetoothConnection.startClient(device,uuid);
    }


    public void SendData(EditText etSend) {
        byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }


    public void SendChar(char character) {
        String RXDataString = Character.toString(character) + ".";
        byte[] bytes = RXDataString.getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }


    public void SendProgress(int value) {
        String RXDataString = "Q" + Integer.toString(value) + ".";
        byte[] bytes = RXDataString.getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }


    public void SendState(Switch sSend, char Alpha , char Beta) {
        if(sSend.isChecked()) {
            String RXDataString = Character.toString(Alpha) + ".";
            byte[] bytes = RXDataString.getBytes(Charset.defaultCharset());
            mBluetoothConnection.write(bytes);
        }
        else {
            String RXDataString = Character.toString(Beta) + ".";
            byte[] bytes = RXDataString.getBytes(Charset.defaultCharset());
            mBluetoothConnection.write(bytes);
        }
    }


    public void CancelConnection() {
        mBluetoothConnection.cancelCon();
        // Initialize progress dialog
        mmProgressDialog = new ProgressDialog(MainActivity.this, R.style.ProgDialigStyle);
        mmProgressDialog.setTitle("Disconnecting device");
        mmProgressDialog.setMessage("Please Wait...");
        mmProgressDialog.show();
    }


    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    @SuppressLint("NewApi")
    public void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }





    /*
     * ********************************** ************* **********************************
     * ********************************** Miscellaneous **********************************
     * ********************************** ************* **********************************
     */
    // Switch tabs function
    public void SwitchTabs(int Tab_Num) {
        mViewPager.setCurrentItem(Tab_Num);
    }

    // Start Controller Activity
    public void StartController() {
        Intent intent = new Intent(MainActivity.this, ControlActivity.class);
        startActivityForResult(intent, Control_Code);
    }

    // Graph plotter
    public void PlotPoints(int data) {
        graph2LastXValue += 1d;
        FragmentData.GraphSeries.appendData(new DataPoint(graph2LastXValue, data),
                true, 100);
        FragmentData.GraphSeries.setColor(getResources().getColor(R.color.BloodRed));
    }

    // Check if string is integer
    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }





    /*
     * ********************************** *************** **********************************
     * ********************************** Google Maps Api **********************************
     * ********************************** *************** **********************************
     */
    public boolean checkGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean GPS_Enabled = false;
        boolean Net_Enabled = false;

        try {
            GPS_Enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        }
        catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        try {
            Net_Enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Simplified if-statement to check for GPS_Enabled & Net_Enabled
        return (GPS_Enabled && Net_Enabled);
    }


    /**
     *
     * Map service routine
     *
     * */
    public boolean isMapServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS) {
            // Everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            // An error occurred but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,
                    available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


}