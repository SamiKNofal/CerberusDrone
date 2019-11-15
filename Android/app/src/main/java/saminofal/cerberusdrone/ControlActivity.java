package saminofal.cerberusdrone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import java.text.DecimalFormat;

import io.github.controlwear.virtual.joystick.android.JoystickView;


public class ControlActivity extends AppCompatActivity {


    /*
     * ********************************** *********** **********************************
     * ********************************** Declaration **********************************
     * ********************************** *********** **********************************
     */
    public JoystickView Joystick1, Joystick2;
    public TextView ControlTitle, JoysticksData1, JoysticksData2;
    public ImageButton ExitBtn;
    public static Activity ConActivity;
    public double Roll = 1500, Pitch = 1500, Yaw = 1500, Throttle = 1500, BaseValue = 1500;



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setContentView(R.layout.activity_control);
            MainActivity.ConActivityState = true;
            ConActivity = this;
            ControlTitle = findViewById(R.id.ControlTitle);
            Joystick1 = findViewById(R.id.Joystick1);
            Joystick2 = findViewById(R.id.Joystick2);
            JoysticksData1 = findViewById(R.id.JoysticksData1);
            JoysticksData2 = findViewById(R.id.JoysticksData2);
            ExitBtn = findViewById(R.id.ExitBtn);

            JoysticksData1.setText("Throttle: " + Throttle + '\n' + "Yaw: " + Yaw);
            JoysticksData2.setText("Pitch: " + Pitch + '\n' + "Roll: " + Roll);
            final DecimalFormat decimalFormat = new DecimalFormat(".##");


            // Joysticks onMove events
            Joystick1.setOnMoveListener(new JoystickView.OnMoveListener() {
                @Override
                public void onMove(int angle, int strength) {

                    Throttle = Double.parseDouble(decimalFormat.format(strength *
                            (Math.sin(Math.toRadians(angle)))
                    ));
                    Throttle = ((1000*Throttle)/200) + BaseValue;
                    Yaw = Double.parseDouble(decimalFormat.format(strength *
                            (Math.cos(Math.toRadians(angle)))
                    ));
                    Yaw = ((1000*Yaw)/200) + BaseValue;
                    JoysticksData1.setText("Throttle: " + Throttle + '\n' +" Yaw: " + Yaw);
                    MainActivity.getInstance().SendProgress(angle);
                }
            });
            Joystick2.setOnMoveListener(new JoystickView.OnMoveListener() {
                @Override
                public void onMove(int angle, int strength) {
                    Pitch = Double.parseDouble(decimalFormat.format(strength *
                            (Math.sin(Math.toRadians(angle)))
                    ));
                    Pitch = ((1000*Pitch)/200) + BaseValue;
                    Roll = Double.parseDouble(decimalFormat.format(strength *
                            (Math.cos(Math.toRadians(angle)))
                    ));
                    Roll = ((1000*Roll)/200) + BaseValue;
                    JoysticksData2.setText("Pitch: " + Pitch + '\n' + "Roll: " + Roll);
                    MainActivity.getInstance().SendProgress(strength);
                }
            });

            // Exit button event
            ExitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim);
                    MainActivity.ConActivityState = false;
                    finish();
                }
            });

            // Font styling - Ailerons font
            Typeface AileronsFont = Typeface.createFromAsset(getAssets(), "fonts/Ailerons.ttf");
            ControlTitle.setTypeface(AileronsFont);
            JoysticksData1.setTypeface(AileronsFont);
            JoysticksData2.setTypeface(AileronsFont);

        }

        catch (Exception e) {
            Toast.makeText(ControlActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim);
        MainActivity.ConActivityState = false;
        finish();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
