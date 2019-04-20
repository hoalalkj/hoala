package com.yeelight.testnewux;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class MoodActivity extends AppCompatActivity {
    private String TAG = "Control";

    private static final int MSG_CONNECT_SUCCESS = 0;
    private static final int MSG_CONNECT_FAILURE = 1;
    private static final String CMD_BRIGHTNESS = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", %duration]}\r\n";
    private static final String CMD_STOP2 = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"off\", \"smooth\", 1000]}\r\n";
    private static final String CMD_CT = "{\"id\":%id,\"method\":\"set_ct_abx\",\"params\":[%value, \"smooth\", 500]}\r\n";
    private static final String CMD_PARTY = "{\"id\":%id,\"method\":\"start_cf\",\"params\":[0, 0, \"1000, 2, 2700, 100, 1000, 2, 6500, 100\"]}\r\n";
    private static final String CMD_STOP = "{\"id\":%id,\"method\":\"stop_cf\",\"params\":[]}\r\n";

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

    private ImageButton happyMood;
    private ImageButton sadMood;
    private ImageButton sleepyMood;
    private ImageButton partyMood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);

        mBulbIP = getIntent().getStringExtra("ip");
        mBulbPort = Integer.parseInt(getIntent().getStringExtra("port"));
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Connecting...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        happyMood = (ImageButton) findViewById(R.id.Happy);
        ((View) happyMood).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write(parseCTCmd(5000));
                write(parseBrightnessCmd(80,10000));
            }
        });

        sadMood = (ImageButton) findViewById(R.id.Sad);
        ((View) sadMood).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write(parseCTCmd(6000));
                write(parseBrightnessCmd(20,20000));
            }
        });

        sleepyMood = (ImageButton) findViewById(R.id.Sleepy);
        ((View) sleepyMood).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write(parseCTCmd(2700));
                write(parseBrightnessCmd(1,10000));
                customDialog("Stop Sleep Mood", "Click 'Stop' to turn off light bulb.", "cancelMethod2");
            }
        });

        partyMood = (ImageButton) findViewById(R.id.Party);
        ((View) partyMood).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write(parsePartyCmd());
                customDialog("Stop Party Mood", "Click 'Stop' to stop light bulb.", "cancelMethod");
            }
        });
        connect();
    }

    private void cancelMethod(){
        write(parsePartyStopCmd());
        Log.d(TAG, "Party Mood Stopped");
        toastMessage("Party Mood Stopped");
    }

    private void cancelMethod2(){
        write(parseSleepStopCmd());
        Log.d(TAG, "Sleepy Mood Stopped");
        toastMessage("Sleepy Mood Stopped");
    }

    public void customDialog(String title, String message, final String cancelMethod){
        final android.support.v7.app.AlertDialog.Builder builderSingle = new android.support.v7.app.AlertDialog.Builder(this);
        builderSingle.setTitle(title);
        builderSingle.setMessage(message);

        builderSingle.setNegativeButton(
                "Stop",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Cancel Called.");
                        if(cancelMethod.equals("cancelMethod")) {
                            cancelMethod();
                        } else {
                            cancelMethod2();
                        }
                    }
                });
        builderSingle.show();
    }

    public void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
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

    private String parseCTCmd(int ct){
        return CMD_CT.replace("%id",String.valueOf(++mCmdId)).replace("%value", String.valueOf(ct));
    }
    private String parseBrightnessCmd(int brightness, int duration){
        return CMD_BRIGHTNESS.replace("%id",String.valueOf(++mCmdId)).replace("%value",String.valueOf(brightness)).replace("%duration",String.valueOf(duration));
    }
    private String parseSleepStopCmd(){
        return CMD_STOP2.replace("%id",String.valueOf(++mCmdId));
    }
    private String parsePartyCmd(){
        return CMD_PARTY.replace("%id",String.valueOf(++mCmdId));
    }

    private String parsePartyStopCmd(){
        return CMD_STOP.replace("%id",String.valueOf(++mCmdId));
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
