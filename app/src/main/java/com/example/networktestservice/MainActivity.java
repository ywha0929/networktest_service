package com.example.networktestservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.net.InetAddress;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainProgram";
    EditText editText0;
    EditText editText1;
    EditText editText2;
    TextView textView;
    Handler UI_handler;
    /*
    what : 0 for send / 1 for receive
     */
    private BroadcastReceiver UIreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"UIreceiver invoked");
            String UImsg = intent.getStringExtra("UImsg");
            Log.d(TAG,"UIreceiver invoked1");
            textView.append(UImsg + "\n");
            Log.d(TAG,"UIreceiver invoked2");
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, MainService.class));
        editText0 = findViewById(R.id.editText0);
        editText1 = findViewById(R.id.editText1);
        textView = findViewById(R.id.textView5);
        Button button = findViewById(R.id.button0);
        Context context = getApplicationContext();
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        editText0.setText(ip);
        editText1.setText("5001");
        editText2 = findViewById(R.id.editText2);
        Button button2 = findViewById(R.id.button2);
        textView.setMovementMethod(new ScrollingMovementMethod());

        registerReceiver(UIreceiver,new IntentFilter("UImsg"));
        Log.d(TAG,"End of onCreate init");
        button2.setOnClickListener(new View.OnClickListener()  //send message
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG,"send message button cliked");
                String tobesent = editText2.getText().toString();
                SendWorkermsg(1,tobesent);
                /*
                Message msg = Message.obtain();
                msg.what = 1; //
                String mail = editText2.getText().toString();
                msg.obj = mail;

                worker_handler.sendMessage(msg);
                */

            }
        });
        button.setOnClickListener(new View.OnClickListener()  //start server
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG,"start server button clicked");
                Intent intent = new Intent(getApplicationContext(),MainService.class);
                startService(intent);
                SendWorkermsg(0,editText1.getText().toString());
                /*
                Message msg1 = Message.obtain();
                msg1.what = 0;
                msg1.obj = editText1.getText().toString();
                worker_handler.sendMessage(msg1);

                 */
            }

        });
        UI_handler = new Handler(Looper.myLooper())
        {
            @Override
            public void handleMessage(@NonNull Message msg)
            {
                Log.d(TAG,"UI handler invoked");
                editText2.getText().append((String)msg.obj + "\n");
            }
        };

    }
    private void SendWorkermsg(int i, String msg)
    {
        Log.d(TAG,"Send Worker msg invoked");
        Intent intent = new Intent("Workermsg");
        intent.putExtra("what",i);
        intent.putExtra("Workermsg",msg);
        sendBroadcast(intent);
        Log.d(TAG,"Sent broadcast message from Main Activity");
    }
}