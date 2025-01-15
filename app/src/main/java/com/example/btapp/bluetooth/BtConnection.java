package com.example.btapp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import com.example.btapp.adapter.BtConsts;

import java.io.IOException;

public class BtConnection {
    private final SharedPreferences pref;
    private final BluetoothAdapter btAdapter;
    private ConnectThread connectThread;
    Context context;
    public BtConnection(Context context) {
        this.context = context;
        this.pref = context.getSharedPreferences(BtConsts.MY_PREF, Context.MODE_PRIVATE);
        this.btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connect(){
        String mac = pref.getString(BtConsts.MAC_KEY, "");
        if(!btAdapter.isEnabled() || mac.isEmpty()) {
            Log.d("MyLog", "error connection");
            return;
        };
        Log.d("MyLog", "connect() мас: " + mac);

        BluetoothDevice device = btAdapter.getRemoteDevice(mac);
        if(device == null) return;
        connectThread = new ConnectThread(btAdapter, device, context);
        connectThread.start();
    }

    public void sendMessage(String message){
        connectThread.getReceiveThread().sendMessage(message.getBytes());
    }
}
