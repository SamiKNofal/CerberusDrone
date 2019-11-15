package saminofal.cerberusdrone;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;


public class SplashActivity extends AppCompatActivity {


    /*
     * ********************************** *********** **********************************
     * ********************************** Declaration **********************************
     * ********************************** *********** **********************************
     */
    public ImageView SplashLogo;
    public TextView SplashLogLayoutTitle, SplashTitle, SafetyMessageMainTxt, Bullet_1, Bullet_2,
            Bullet_3, Bullet_4, Bullet_5, Bullet_6;
    public Button SplashButton;
    public RelativeLayout SplashLogLayout, SplashContLayout;
    public byte AnimPointer = 0;
    public Drawable Backgrounds[] = new Drawable[2];
    public TransitionDrawable crossFade;
    public Handler handlerFade = new Handler(),
            handlerSlider = new Handler();
    // Image slider elements
    int Count = 1;
    public int SliderDelay = 9000;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.activity_splash);
            Bullet_1 = findViewById(R.id.Bullet_1);
            Bullet_2 = findViewById(R.id.Bullet_2);
            Bullet_3 = findViewById(R.id.Bullet_3);
            Bullet_4 = findViewById(R.id.Bullet_4);
            Bullet_5 = findViewById(R.id.Bullet_5);
            Bullet_6 = findViewById(R.id.Bullet_6);
            SplashLogo =  findViewById(R.id.SplashLogo);
            SplashTitle = findViewById(R.id.SplashTitle);
            SplashButton = findViewById(R.id.SplashButton);
            SplashLogLayout = findViewById(R.id.SplashLogLayout);
            SplashContLayout = findViewById(R.id.SplashContLayout);
            SplashLogLayoutTitle = findViewById(R.id.SplashLogLayoutTitle);
            SafetyMessageMainTxt = findViewById(R.id.SafetyMessageMainTxt);
            final Intent intent = new Intent(getApplicationContext(), MainActivity.class);

            // Font styling - Ailerons font
            Typeface AileronsFont = Typeface.createFromAsset(getAssets(), "fonts/Ailerons.ttf");
            SplashTitle.setTypeface(AileronsFont);
            SplashButton.setTypeface(AileronsFont);
            SafetyMessageMainTxt.setTypeface(AileronsFont);
            SplashLogLayoutTitle.setTypeface(AileronsFont);

            // Drawable transition
            Resources res = getResources();
            Backgrounds[0] = res.getDrawable(R.drawable.bullet_inactive);
            Backgrounds[1] = res.getDrawable(R.drawable.bullet_active);

            // Set MainActivity start button
            SplashButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fadeAnimationLayout(SplashContLayout, true, 300);
                    handlerFade.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(intent);
                            finish();
                        }
                    }, 300);
                }
            });


            handlerFade.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(AnimPointer == 0) {
                        fadeAnimationLayout(SplashLogLayout, true, 400);
                        AnimPointer = 1;
                        handlerFade.postDelayed(this, 700);
                    }
                    else if (AnimPointer == 1) {
                        fadeAnimationLayout(SplashContLayout, false, 400);
                        SplashContLayout.setVisibility(View.VISIBLE);
                        AnimPointer = 2;
                        handlerFade.postDelayed(this, 10000);
                    }
                    else if (AnimPointer == 2) {
                        switch (Count) {
                            case 0:
                                crossFade = new TransitionDrawable(Backgrounds);
                                Bullet_1.setBackground(crossFade);
                                crossFade.startTransition(700);
                                crossFade = new TransitionDrawable(Backgrounds);
                                Bullet_6.setBackground(crossFade);
                                crossFade.startTransition(0);
                                crossFade.reverseTransition(700);
                                fadeAnimationTxtView(SafetyMessageMainTxt, Count, 200);
                                SliderDelay = 9000;
                                break;
                            case 1:
                                crossFade = new TransitionDrawable(Backgrounds);
                                Bullet_2.setBackground(crossFade);
                                crossFade.startTransition(700);
                                crossFade = new TransitionDrawable(Backgrounds);
                                Bullet_1.setBackground(crossFade);
                                crossFade.startTransition(0);
                                crossFade.reverseTransition(700);
                                fadeAnimationTxtView(SafetyMessageMainTxt, Count, 200);
                                SliderDelay = 6000;
                                break;
                            case 2:
                                crossFade = new TransitionDrawable(Backgrounds);
                                Bullet_3.setBackground(crossFade);
                                crossFade.startTransition(700);
                                crossFade = new TransitionDrawable(Backgrounds);
                                Bullet_2.setBackground(crossFade);
                                crossFade.startTransition(0);
                                crossFade.reverseTransition(700);
                                fadeAnimationTxtView(SafetyMessageMainTxt, Count, 200);
                                SliderDelay = 6000;
                                break;
                            case 3:
                                crossFade = new TransitionDrawable(Backgrounds);
                                Bullet_4.setBackground(crossFade);
                                crossFade.startTransition(700);
                                crossFade = new TransitionDrawable(Backgrounds);
                                Bullet_3.setBackground(crossFade);
                                crossFade.startTransition(0);
                                crossFade.reverseTransition(700);
                                fadeAnimationTxtView(SafetyMessageMainTxt, Count, 200);
                                SliderDelay = 12000;
                                break;
                            case 4:
                                crossFade = new TransitionDrawable(Backgrounds);
                                Bullet_5.setBackground(crossFade);
                                crossFade.startTransition(700);
                                crossFade = new TransitionDrawable(Backgrounds);
                                Bullet_4.setBackground(crossFade);
                                crossFade.startTransition(0);
                                crossFade.reverseTransition(700);
                                fadeAnimationTxtView(SafetyMessageMainTxt, Count, 200);
                                SliderDelay = 6000;
                                break;
                            case 5:
                                crossFade = new TransitionDrawable(Backgrounds);
                                Bullet_6.setBackground(crossFade);
                                crossFade.startTransition(700);
                                crossFade = new TransitionDrawable(Backgrounds);
                                Bullet_5.setBackground(crossFade);
                                crossFade.startTransition(0);
                                crossFade.reverseTransition(700);
                                fadeAnimationTxtView(SafetyMessageMainTxt, Count, 500);
                                SliderDelay = 6000;
                                break;
                        }
                        Count++;
                        if(Count > 5) {
                            Count = 0;
                        }
                        handlerSlider.postDelayed(this, SliderDelay);
                    }

                }
            }, 800);
        }

        catch (Exception e) {
            Toast.makeText(SplashActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }



    public void fadeAnimationLayout(final RelativeLayout relativeLayout, boolean State,
                                    int duration) {

        if(State) {
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setDuration(duration);

            fadeOut.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationEnd(Animation animation) {
                    relativeLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // Do nothing
                }

                @Override
                public void onAnimationStart(Animation animation) {
                    // Do nothing
                }

            });

            relativeLayout.startAnimation(fadeOut);
        }

        else {
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new AccelerateInterpolator());
            fadeIn.setDuration(duration);

            fadeIn.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationEnd(Animation animation) {
                    relativeLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // Do nothing
                }

                @Override
                public void onAnimationStart(Animation animation) {
                    // Do nothing
                }

            });

            relativeLayout.startAnimation(fadeIn);
        }
    }

    public void fadeAnimationTxtView(final TextView textView, final int Msg,
                                     final int duration) {

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(duration);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                textView.setVisibility(View.GONE);

                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new AccelerateInterpolator());
                fadeIn.setDuration(duration);

                fadeIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        switch (Msg) {
                            case 0:
                                textView.setText(getResources().getString(R.string.SafetyMessageMain));
                                break;
                            case 1:
                                textView.setText(getResources().getString(R.string.SafetyMessageSub1));
                                break;
                            case 2:
                                textView.setText(getResources().getString(R.string.SafetyMessageSub2));
                                break;
                            case 3:
                                textView.setText(getResources().getString(R.string.SafetyMessageSub3));
                                break;
                            case 4:
                                textView.setText(getResources().getString(R.string.SafetyMessageSub4));
                                break;
                            case 5:
                                textView.setText(getResources().getString(R.string.SafetyMessageSub5));
                                break;
                        }
                        textView.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // Do nothing
                    }
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // Do nothing
                    }

                });
                textView.startAnimation(fadeIn);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing
            }
            @Override
            public void onAnimationStart(Animation animation) {
                // Do nothing
            }
        });
        textView.startAnimation(fadeOut);

    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim);
    }
}