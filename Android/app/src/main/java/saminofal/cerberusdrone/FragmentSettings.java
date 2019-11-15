package saminofal.cerberusdrone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Objects;
import static saminofal.cerberusdrone.MainActivity.BluetoothDeviceState;


public class FragmentSettings extends Fragment implements AdapterView.OnItemClickListener {


    /*
     * ********************************** *********** **********************************
     * ********************************** Declaration **********************************
     * ********************************** *********** **********************************
     */
    public Context context;
    // As per Android Fragment documentation an empty constructor should be defined
    public FragmentSettings(){}
    public static FragmentData newInstance() {
        return new FragmentData();
    }
    // Layout elements
    public static Switch Bluetooth_Switch;
    public static RelativeLayout Bluetooth_SubLayout;
    public TextView Bluetooth_Txt;
    @SuppressLint("StaticFieldLeak")
    public static ListView lvBLDevices;
    public static Button Bluetooth_Discovery, Bluetooth_FindDevices,
            BL_CancelCon;
    public boolean StateVal;





    /*
     * ********************************** ******************* **********************************
     * ********************************** Lifecycle Callbacks **********************************
     * ********************************** ******************* **********************************
     */
    /**
     * Since Fragment is Activity dependent you need Activity context in various cases
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = getActivity();
    }


    /**
     * Your entire fragment uses context instead of getActivity()
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        // Do not populate activity related tasks here
        return view;
    }


    /**
     *
     *  The code should be called on onViewCreated method
     *
     * **/
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            // Define variables and elements
            Bluetooth_Txt = view.findViewById(R.id.Bluetooth_Txt);
            Bluetooth_Switch = view.findViewById(R.id.Bluetooth_Switch);
            Bluetooth_Discovery = view.findViewById(R.id.Bluetooth_Discovery);
            Bluetooth_FindDevices = view.findViewById(R.id.Bluetooth_FindDevices);
            BL_CancelCon = view.findViewById(R.id.BL_CancelCon);
            lvBLDevices = view.findViewById(R.id.lvBLDevices);
            Bluetooth_SubLayout = view.findViewById(R.id.Bluetooth_SubLayout);
            lvBLDevices.setOnItemClickListener(FragmentSettings.this);

            // Test for bluetooth settings
            BluetoothTest();

            // Font styling - Ailerons font
            Typeface AileronsFont = Typeface.createFromAsset(
                    Objects.requireNonNull(getActivity()).getAssets(), "fonts/Ailerons.ttf");
            Bluetooth_Txt.setTypeface(AileronsFont);

            // Enable or disable bluetooth
            Bluetooth_Switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StateVal = ((MainActivity)getActivity()).enableDisableBT();
                    if(StateVal) {
                        // Set state false until permission is granted. State change
                        // is handled in MainActivity
                        Bluetooth_Switch.setChecked(false);
                    }
                    else {
                        Bluetooth_Switch.setChecked(false);
                        Bluetooth_SubLayout.setVisibility(View.GONE);
                    }
                }
            });

            // Enable bluetooth discovery for a short period
            Bluetooth_Discovery.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    ((MainActivity)getActivity()).BluetoothDiscovery();
                }
            });
            // Find bluetooth devices in the area
            Bluetooth_FindDevices.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    ((MainActivity)getActivity()).BluetoothScan();
                }
            });
            // Cancel bluetooth device connection
            BL_CancelCon.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    ((MainActivity)getActivity()).CancelConnection();
                }
            });

            setRetainInstance(true);
        }
        catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }


    /**
     *  Called to resume fragment
     */
    @Override
    public void onResume() {
        super.onResume();
    }





    /*
     * ********************************** ********* **********************************
     * ********************************** Bluetooth **********************************
     * ********************************** ********* **********************************
     */
    public void BluetoothTest() {
        StateVal = ((MainActivity) Objects
                .requireNonNull(getActivity())).checkBT();
        if(StateVal) {
            Bluetooth_Switch.setChecked(true);
            Bluetooth_SubLayout.setVisibility(View.VISIBLE);
        }
        else {
            Bluetooth_Switch.setChecked(false);
            Bluetooth_SubLayout.setVisibility(View.GONE);
        }

        if(BluetoothDeviceState) {
            BL_CancelCon.setVisibility(View.VISIBLE);
        }
        else {
            BL_CancelCon.setVisibility(View.GONE);
        }
    }


    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ((MainActivity) Objects.requireNonNull(getActivity())).BluetoothConnect(i);
    }


}
