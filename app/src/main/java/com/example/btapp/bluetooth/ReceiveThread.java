package com.example.btapp.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReceiveThread extends Thread{
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] rBuffer;

    public ReceiveThread(BluetoothSocket socket){
        this.socket = socket;
        try {
            inputStream = socket.getInputStream();
        } catch (IOException err){
            Log.d("MyLog", "Error getInputStream: " + String.valueOf(err));
        }
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException err){
            Log.d("MyLog", "Error getOutputStream: " + String.valueOf(err));
        }
    }

    @Override
    public void run() {
        rBuffer = new byte[20];
        while (true){
            try {
                int size = inputStream.read(rBuffer);
                String message = new String(rBuffer, 0, size);
                // Получение сообщения
                Log.d("MyLog", "class ReceiveThread message: " + String.valueOf(message));
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
