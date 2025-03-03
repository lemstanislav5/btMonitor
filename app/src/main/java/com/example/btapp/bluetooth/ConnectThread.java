package com.example.btapp.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.btapp.MainActivity;

import java.io.IOException;

public class ConnectThread extends Thread{
    private final BluetoothAdapter btAdapter;
    private BluetoothSocket mSocket;
    private ReceiveThread receiveThread;
    public static final String UUID = "00001101-0000-1000-8000-00805F9B34FB";

    Context context;
    @SuppressLint("MissingPermission")
    public ConnectThread(BluetoothAdapter btAdapter, BluetoothDevice device, Context context){
        this.btAdapter = btAdapter;
        try {
            this.mSocket = device.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
        } catch (IOException e){
            Log.d("MyLog", "Not mSocket");
        }
        this.context = context;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        btAdapter.cancelDiscovery();
        try {
            mSocket.connect();
            receiveThread = new ReceiveThread(mSocket, context);
            receiveThread.start();
            Log.d("MyLog", "Connected");
        } catch (IOException err){
            Log.d("MyLog", String.valueOf(err));
            closeConnection();
        }
    }
    public void closeConnection(){
        try {
            mSocket.close();
            Log.d("MyLog", "Close connection");
        } catch (IOException err){
            Log.d("MyLog", "Close connection error");
        }
    }

    public ReceiveThread getReceiveThread() {
        return receiveThread;
    }
}
