package com.example.btapp.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;

public class ConnectThread extends Thread{
    private Context context;
    private BluetoothAdapter btAdapter;
    private BluetoothDevice device;
    private BluetoothSocket mSocket;
    public static final String UUID = "00001101-0000-1000-8000-00805F9B34FB";
    @SuppressLint("MissingPermission")
    public ConnectThread(Context context, BluetoothAdapter btAdapter, BluetoothDevice device){
        this.context = context;
        this.btAdapter = btAdapter;
        this.device = device;
        try {
            mSocket = device.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
        } catch (IOException e){
            Log.d("MyLog", "Not mSocket");
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        btAdapter.cancelDiscovery();
        try {
            mSocket.connect();
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
}
