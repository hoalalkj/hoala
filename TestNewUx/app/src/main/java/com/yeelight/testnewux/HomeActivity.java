package com.yeelight.testnewux;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class HomeActivity extends AppCompatActivity {
    private ImageButton alarmButton;
    private ImageButton lightButton;
    private ImageButton settingsButton;
    private ImageButton temperatureButton;
    private ImageButton moodButton;
    private String mBulbIP;
    private String mBulbPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mBulbIP = getIntent().getStringExtra("ip");
        mBulbPort = getIntent().getStringExtra("port");

        alarmButton = (ImageButton) findViewById(R.id.Alarm);
        ((View) alarmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlarm();
            }
        });

        lightButton = (ImageButton) findViewById(R.id.Light);
        ((View) lightButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLight();
            }
        });

        moodButton = (ImageButton) findViewById(R.id.Mood);
        ((View) moodButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMood();
            }
        });

        settingsButton = (ImageButton) findViewById(R.id.Settings);
        ((View) settingsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });

        temperatureButton = (ImageButton) findViewById(R.id.Temperature);
        ((View) temperatureButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTemperature();
            }
        });
    }

    public void openAlarm() {
        Intent intent = new Intent(this, AlarmActivity.class);
        intent.putExtra("ip", mBulbIP);
        intent.putExtra("port", mBulbPort);
        startActivity(intent);
    }

    public void openLight() {
        Intent intent = new Intent(this, LightActivity.class);
        intent.putExtra("ip", mBulbIP);
        intent.putExtra("port", mBulbPort);
        startActivity(intent);
    }

    public void openMood() {
        Intent intent = new Intent(this, MoodActivity.class);
        intent.putExtra("ip", mBulbIP);
        intent.putExtra("port", mBulbPort);
        startActivity(intent);
    }

    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openTemperature() {
        Intent intent = new Intent(this, TemperatureActivity.class);
        intent.putExtra("ip", mBulbIP);
        intent.putExtra("port", mBulbPort);
        startActivity(intent);
    }
}
