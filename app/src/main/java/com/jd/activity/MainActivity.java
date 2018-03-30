package com.jd.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jd.R;
import com.jd.constant.TimeConstant;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Intent intent;
    private int i;
    private int j;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        intent = new Intent(this, CheckTimeService.class);
//        startService(intent);

        findViewById(R.id.btn_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });

//        getRandomNum();
    }


    private void getRandomNum() {
        Random random = new Random();
        TimeConstant.s_Punch_Minute = 20 + random.nextInt(40);
        TimeConstant.s_Punch_Second = random.nextInt(60);
    }

    @Override
    protected void onDestroy() {
        stopService(intent);
        super.onDestroy();
    }
}
