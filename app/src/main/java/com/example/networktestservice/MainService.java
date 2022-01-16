package com.example.networktestservice;

import static java.lang.Integer.parseInt;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MainService extends Service {
    private static final String TAG = "MainProgram";
    String port_str;
    Handler workerHandler;
    Handler socketHandler;

    Handler socketHandler2;

    worker_Thd worker_thd;
    Socket sock;
    InetAddress clientHost;
    int clientPort;
    outsocket_Thd outsocket_thd;
    insocket_Thd insocket_thd;
    InputStream instream;
    OutputStream outstream;

    public MainService() {
    }

    @Override
    public void onCreate() {
        worker_thd = new worker_Thd();
        worker_thd.start();
        Log.d(TAG, "Service onCreate() : done");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String port_str = intent.getStringExtra("port");
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service onDestroty() done");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private BroadcastReceiver Workerreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message m = Message.obtain();
            m.what = intent.getIntExtra("what", -1);
            m.obj = intent.getStringExtra("Workermsg");
            workerHandler.sendMessage(m);
            Log.d(TAG, "received Worker message");
        }
    };

    class worker_Thd extends Thread {
        public void run() {
            Log.d(TAG, "worker thread running");

            registerReceiver(Workerreceiver, new IntentFilter("Workermsg"));
            Looper.prepare();

            workerHandler = new Handler(Looper.myLooper()) {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                public void handleMessage(@NonNull Message msg) {
                    int type = msg.what;
                    switch (type) {
                        case 0:
                            Log.d(TAG, "worker thread : case 0");
                            outsocket_thd = new outsocket_Thd();
                            outsocket_thd.start();
                            insocket_thd = new insocket_Thd();
                            insocket_thd.start();
                            break;

                        case 1: //send message
                            Log.d(TAG, "worker thread : case 1");
                            String mail = (String) msg.obj;

                            byte[] buffer;
                            buffer = mail.getBytes(StandardCharsets.US_ASCII);
                            Message sock_msg = Message.obtain();
                            sock_msg.obj = buffer;
                            Message sock_msg2 = Message.obtain();
                            sock_msg2.obj = buffer;
                            socketHandler.sendMessage(sock_msg);
                            socketHandler2.sendMessage(sock_msg2);
                            SendUImsg("Sent Message from server : " + msg.obj);
                            break;
                        case 2: //read message
                            Log.d(TAG, "worker thread : case 2");
                            SendUImsg("Received Message from client : " + (String) msg.obj);

                            break;
                    }
                }
            };
            Looper.loop();
        }
    }

    class outsocket_Thd extends Thread {

        String input = "";

        public void run() {
            Log.d(TAG, "insocket thread running");
            Looper.prepare();

            Log.d(TAG, "asdf");

            socketHandler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    try {
                        Log.d(TAG, "Socket handler invoked");
                        outstream.write((byte[]) msg.obj);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            Looper.loop();

        }
    }

    class insocket_Thd extends Thread {


        String input = "";

        public void run() {

            Log.d(TAG, "outsocket thread running");


            int portnumber = 5001;
            Log.d(TAG, "portnumber = " + portnumber);
            try {
                Log.d(TAG, "socket thread running 1");
                ServerSocket server = new ServerSocket(portnumber);
                Log.d(TAG, "socket thread running 2");
                SendUImsg("Server has started at port : " + portnumber);
                sock = server.accept();
                Log.d(TAG, "socket thread running 3");
                clientHost = sock.getInetAddress();
                clientPort = sock.getPort();
                SendUImsg("Client accessed : " + clientHost + "/" + clientPort);
                outstream = sock.getOutputStream();
                instream = sock.getInputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                try {

                    byte[] buffer = new byte[100];
                    int check = instream.read(buffer);
                    Log.d(TAG, "socket thread check : " + check);
                    if (check != 0) {
                        Log.d(TAG, "input stream detected");
                        input = "";
                        for (int i = 0; buffer[i] != 0; i++) {
                            input = input.concat(Character.toString((char) buffer[i]));

                        }
                        Message inputmsg = Message.obtain();
                        inputmsg.what = 2;
                        inputmsg.obj = input;
                        workerHandler.sendMessage(inputmsg);
                        Log.d(TAG, "Message sent to worker handler from socket thread");
                        check = 0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }


    }


    public void SendUImsg(String msg) {
        Log.d(TAG, "Send UI msg invoked");
        Intent intent = new Intent("UImsg");
        intent.putExtra("UImsg", msg);
        Log.d(TAG, "UImsg : " + msg);
        sendBroadcast(intent);
        //Message UImsg = Message.obtain();
        //UImsg.obj = msg;
        //UI_handler.sendMessage(UImsg);
    }
}