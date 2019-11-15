package saminofal.cerberusdrone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Objects;
import static saminofal.cerberusdrone.MainActivity.BluetoothDeviceState;

public class FragmentHome extends Fragment {


    /*
     * ********************************** *********** **********************************
     * ********************************** Declaration **********************************
     * ********************************** *********** **********************************
     */
    public Context context;
    // As per Android Fragment documentation an empty constructor should be defined
    public FragmentHome(){}
    public static FragmentHome newInstance() {
        return new FragmentHome();
    }
    // Layout elements
    public static RelativeLayout FragHomeLayoutMsg, FragHomeLayoutCont;
    public int AltBuffer;
    public TextView FragHomeTxtMsg, Utilities, Propellers, Battery,
            AltSeekBarVal, Miscellaneous, FragHomeTabSwitch, Controller;
    public Switch GPS_Switch, Safety_Switch, Sensors_Switch,
            NightMode_Switch, LowPower_Switch;
    public Button ControllerBtn, SendBtn;
    public EditText SendTxt;
    public SeekBar AltSeekBar;
    public static TextView Bat_Lev_Val;
    public static ProgressBar Battery_Progress;





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


    /**
     *  Inflate layout
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // Do not populate activity related tasks here
        return view;
    }


    /**
     *  The code should be called on onViewCreated method
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            // Define elements here:
            AltSeekBar = view.findViewById(R.id.Altitude_SeekBar);
            Utilities = view.findViewById(R.id.Utilities);
            Propellers = view.findViewById(R.id.Propellers);
            Battery = view.findViewById(R.id.Battery);
            AltSeekBarVal = view.findViewById(R.id.Alt_Sek_Val);
            Miscellaneous = view.findViewById(R.id.Miscellaneous);
            GPS_Switch = view.findViewById(R.id.GPS_Switch);
            Safety_Switch = view.findViewById(R.id.Safety_Switch);
            Sensors_Switch = view.findViewById(R.id.Sensors_Switch);
            NightMode_Switch = view.findViewById(R.id.NightMode_Switch);
            LowPower_Switch = view.findViewById(R.id.LowPower_Switch);
            Controller = view.findViewById(R.id.Controller);
            ControllerBtn = view.findViewById(R.id.ControllerBtn);
            SendBtn = view.findViewById(R.id.SendBtn);
            SendTxt = view.findViewById(R.id.SendTxt);
            Battery_Progress = view.findViewById(R.id.Battery_Progress);
            Bat_Lev_Val = view.findViewById(R.id.Bat_Lev_Val);
            FragHomeTxtMsg = view.findViewById(R.id.FragHomeTxtMsg);
            FragHomeLayoutMsg = view.findViewById(R.id.FragHomeLayoutMsg);
            FragHomeTabSwitch = view.findViewById(R.id.FragHomeTabSwitch);
            FragHomeLayoutCont = view.findViewById(R.id.FragHomeLayoutCont);

            // Set elements values
            AltSeekBar.setProgress(0);
            AltSeekBarVal.setText(0 +" cm");
            Bat_Lev_Val.setText(75 + " %");

            // Check connected device status to remove message
            if(BluetoothDeviceState) {
                FragHomeLayoutMsg.setVisibility(View.GONE);
                FragHomeLayoutCont.setVisibility(View.VISIBLE);
            }
            else {
                FragHomeLayoutMsg.setVisibility(View.VISIBLE);
                FragHomeLayoutCont.setVisibility(View.GONE);
            }

            // Utilities switches onChange events
            GPS_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ((MainActivity)context).SendState(GPS_Switch, 'A', 'B');
                }
            });
            Safety_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ((MainActivity)context).SendState(Safety_Switch, 'C', 'D');
                }
            });
            Sensors_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ((MainActivity)context).SendState(Sensors_Switch, 'E', 'F');
                }
            });
            NightMode_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ((MainActivity)context).SendState(NightMode_Switch, 'G', 'H');
                }
            });
            LowPower_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ((MainActivity)context).SendState(LowPower_Switch, 'I', 'J');
                }
            });
            SendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity)context).SendData(SendTxt);
                }
            });
            AltSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
                @Override
                public void onProgressChanged (SeekBar AltSeekBar,int progress,
                                               boolean fromUser){
                    AltBuffer = (progress * 4095)/100;
                    AltSeekBarVal.setText(String.valueOf(AltBuffer + " cm"));
                    ((MainActivity)context).SendProgress(AltBuffer);
                    Battery_Progress.setProgress(progress);
                    Bat_Lev_Val.setText(progress + " %");
                }

                @Override
                public void onStartTrackingTouch (SeekBar seekBar){
                    // TODO Auto-generated method stub
                }

                @Override
                public void onStopTrackingTouch (SeekBar seekBar){
                    // TODO Auto-generated method stub
                }
            });
            ControllerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity)context).StartController();
                }
            });

            // Settings switch tab
            FragHomeTabSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity)context).SwitchTabs(3);
                }
            });


            // Font styling
            Typeface AileronsFont = Typeface.createFromAsset(Objects.requireNonNull(
                    getActivity()).getAssets(), "fonts/Ailerons.ttf");
            Utilities.setTypeface(AileronsFont);
            Propellers.setTypeface(AileronsFont);
            Battery.setTypeface(AileronsFont);
            Controller.setTypeface(AileronsFont);
            Miscellaneous.setTypeface(AileronsFont);
            Bat_Lev_Val.setTypeface(AileronsFont);
            FragHomeTxtMsg.setTypeface(AileronsFont);
            FragHomeTabSwitch.setTypeface(AileronsFont);

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

}