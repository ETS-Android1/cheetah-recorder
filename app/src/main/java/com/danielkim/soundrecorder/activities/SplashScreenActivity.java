package com.danielkim.soundrecorder.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.danielkim.soundrecorder.R;

public class SplashScreenActivity extends AppCompatActivity {

    AnimationDrawable rocketAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                runMain();
            }
        }, 1200);   //5 seconds

    }

    public void runMain(){

        // variabes
        Intent myIntent;

        myIntent = new Intent(this, MainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        this.startActivity(myIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        runMain();

    }
}