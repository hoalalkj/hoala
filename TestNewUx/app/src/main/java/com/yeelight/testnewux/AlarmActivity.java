package com.yeelight.testnewux;

//import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private static final String CMD_BRIGHTNESS = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[100, \"smooth\", %duration]}\r\n";

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
    private Button saveButton;

//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
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
        saveButton = (Button) findViewById(R.id.save);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Timer alarm = new Timer();
                alarm.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (currentTime.getText().toString().equals(AlarmTime())) {
                            write(parseBrightnessCmd(60000));
                            alarm.cancel();
                        }
                    }
                }, 0, 1000);
            }
        });
        connect();
    }

    private String AlarmTime() {
        Integer alarmHour = alarmTime.getCurrentHour();
        Integer alarmMinute = alarmTime.getCurrentMinute();
        alarmMinute --;

        String stringAlarmTime;
        if (alarmHour > 12) {
            alarmHour = alarmHour - 12;
            stringAlarmTime = alarmHour.toString().concat(":").concat(alarmMinute.toString()).concat(" PM");
        } else {
            stringAlarmTime = alarmHour.toString().concat(":").concat(alarmMinute.toString()).concat(" AM");
        }
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
    private String parseBrightnessCmd(int duration){
        return CMD_BRIGHTNESS.replace("%id",String.valueOf(++mCmdId)).replace("%duration",String.valueOf(duration));
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