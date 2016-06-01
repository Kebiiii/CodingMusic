package com.example.liukebing.codingkeplayer;


import android.app.Activity;
import android.content.Intent;
import android.os.Message;

import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends Activity {

    private static final int START_ACTIVITY = 0x1;
    private static final int SPLASH_2 = 0x2;
    private static final int SPLASH_3 = 0x3;

    private ImageView splash;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //去标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_splash);
        splash = (ImageView) findViewById(R.id.splash);

        Intent intent = new Intent(this, PlayService.class);
        startService(intent);

        handler.sendEmptyMessageDelayed(SPLASH_2, 2500);
        handler.sendEmptyMessageDelayed(SPLASH_3, 5500);
        handler.sendEmptyMessageDelayed(START_ACTIVITY, 8500);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_ACTIVITY:
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                    break;
                case SPLASH_2:
                    splash.setImageResource(R.mipmap.p2);
                    break;
                case SPLASH_3:
                    splash.setImageResource(R.mipmap.p3);
                    break;
            }
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

}
