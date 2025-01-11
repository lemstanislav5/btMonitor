package com.example.btapp.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReceiveThread extends Thread{
    private InputStream inputStream;
    private OutputStream outputStream;
    Context context;

    public ReceiveThread(BluetoothSocket socket, Context context){
        try {
            this.inputStream = socket.getInputStream();
        } catch (IOException err){
            Log.d("MyLog", "Error getInputStream: " + String.valueOf(err));
        }
        try {
            this.outputStream = socket.getOutputStream();
        } catch (IOException err){
            Log.d("MyLog", "Error getOutputStream: " + String.valueOf(err));
        }
        this.context = context;
    }

    @Override
    public void run() {
        Log.d("MyLog", "class ReceiveThread run()");
        byte[] rBuffer = new byte[40];
        int size;
        String key = "";
        while (true){
            try {
                size = inputStream.read(rBuffer);
                String newKey = new String(rBuffer, 0, size);
                boolean hookStart = newKey.contains("[");
                boolean hookEnd = newKey.contains("]");
                if(hookStart && hookEnd){
                    if(!key.equals(newKey)){
                        key = newKey;
                        Log.d("MyLog", "class ReceiveThread key: " + key);

                        Intent intent = new Intent();
                        intent.putExtra("com.example.snippets.DATA", key);
                        intent.setPackage("com.example.btapp");
                        intent.setAction("MY_ACTION");
                        context.sendBroadcast(intent);
                    }
                }
            } catch (IOException err){
                Log.d("MyLog", "run: " + String.valueOf(err));
                break;
            }
        }
    }

    public void sendMessage(byte[] byteArray){
        try {
            outputStream.write(byteArray);
        } catch (IOException err){
            Log.d("MyLog", "Error sendMessage: " + String.valueOf(err));
        }
    }
}
