package com.yeelight.testnewux;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TimePicker;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmActivity extends AppCompatActivity {
    private String TAG = "Control";

    private static final int MSG_CONNECT_SUCCESS = 0;
    private static final int MSG_CONNECT_FAILURE = 1;
    private static final String CMD_ON = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"on\",\"smooth\",500]}\r\n" ;
    private static final String CMD_OFF = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"off\",\"smooth\",500]}\r\n" ;
    private static final String CMD_CT = "{\"id\":%id,\"method\":\"set_ct_abx\",\"params\":[%value, \"smooth\", 500]}\r\n";
    private static final String CMD_BRIGHTNESS = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", 200]}\r\n";

    private int mCmdId;
    private Socket mSocket;
    private String mBulbIP;
    private int mBulbPort;
    private ProgressDialog mProgressDialog;
    private BufferedOutputStream mBos;
    private BufferedReader mReader;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_CONNECT_FAILURE:
                    mProgressDialog.dismiss();
                    break;
                case MSG_CONNECT_SUCCESS:
                    mProgressDialog.dismiss();
                    break;
            }
        }
    };

    private TimePicker alarmTime;
    private TextClock currentTime;
    private Integer countdownTime;
    private Integer incrementValue;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        mBulbIP = getIntent().getStringExtra("ip");
        mBulbPort = Integer.parseInt(getIntent().getStringExtra("port"));
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Connecting...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        alarmTime = (TimePicker) findViewById(R.id.timePicker);
        currentTime = (TextClock) findViewById(R.id.textClock);

//        Spinner alarmCountdown = (Spinner) findViewById(R.id.alarmCountdown);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
//                this,
//                R.array.countdownTimes,
//                android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        alarmCountdown.setAdapter(adapter);
//
//        alarmCountdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
//                String selectedTime = (String) adapterView.getItemAtPosition(pos);
//                String intValue = selectedTime.replaceAll("[^0-9]", "");
//                countdownTime = Integer.parseInt(intValue);
// //               incrementValue = (100 / (countdownTime*60));
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

        Timer alarm = new Timer();
        alarm.scheduleAtFixedRate(new TimerTask() {
            Integer i = 1;
            @Override
            public void run() {
 //               if (countdownTime == 0) {
                    if (currentTime.getText().toString().equals(AlarmTime())) {
                        write(parseBrightnessCmd(i));
                        if (i < 101) {
                            i++;
                        }
                    }
 //               } else {
 //                   if (currentTime.getText().toString().equals(AlarmTime())) {
 //                       lightIncrement();
 //                   }
 //               }
            }
        }, 0, 1000);
        connect();
    }

//    private void lightIncrement() {
//        Timer lightIncrement = new Timer();
//        lightIncrement.scheduleAtFixedRate(new TimerTask() {
//
//            @Override
//            public void run() {
//                write(parseBrightnessCmd(1));
//            }
//        }, 0, incrementValue*1000);
//    }

    private String AlarmTime() {
        Integer alarmHour = alarmTime.getCurrentHour();
        Integer alarmMinute = alarmTime.getCurrentMinute();

        String stringAlarmTime;

 //       if (countdownTime == 0) {
            if (alarmHour > 12) {
                alarmHour = alarmHour - 12;
                stringAlarmTime = alarmHour.toString().concat(":").concat(alarmMinute.toString()).concat(" PM");
            } else {
                stringAlarmTime = alarmHour.toString().concat(":").concat(alarmMinute.toString()).concat(" AM");
            }
//        } else {
//            alarmMinute = alarmMinute - countdownTime;
//            if (alarmHour > 12) {
//                alarmHour = alarmHour - 12;
//                stringAlarmTime = alarmHour.toString().concat(":").concat(alarmMinute.toString()).concat(" PM");
//            } else {
//                stringAlarmTime = alarmHour.toString().concat(":").concat(alarmMinute.toString()).concat(" AM");
//            }
//        }
        return stringAlarmTime;
    }

    private boolean cmd_run = true;
    private void connect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cmd_run = true;
                    mSocket = new Socket(mBulbIP, mBulbPort);
                    mSocket.setKeepAlive(true);
                    mBos= new BufferedOutputStream(mSocket.getOutputStream());
                    mHandler.sendEmptyMessage(MSG_CONNECT_SUCCESS);
                    mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    while (cmd_run){
                        try {
                            String value = mReader.readLine();
                            Log.d(TAG, "value = "+value);
                        }catch (Exception e){

                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(MSG_CONNECT_FAILURE);
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            cmd_run = false;
            if (mSocket!=null)
                mSocket.close();
        }catch (Exception e){

        }

    }
    private String parseSwitch(boolean on){
        return CMD_ON.replace("%id", String.valueOf(++mCmdId));
    }
    private String parseBrightnessCmd(int brightness){
        return CMD_BRIGHTNESS.replace("%id",String.valueOf(++mCmdId)).replace("%value",String.valueOf(brightness)).replace("%effect",String.valueOf("smooth").replace("%duration",String.valueOf(1000)));
    }
    private void write(String cmd){
        if (mBos != null && mSocket.isConnected()){
            try {
                mBos.write(cmd.getBytes());
                mBos.flush();
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            Log.d(TAG,"mBos = null or mSocket is closed");
        }
    }
}