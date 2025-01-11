package com.example.btapp.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

                        // Парсим json стороку в массив int[]
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();

                        // Приводим массив к строке с данными HEX
                        StringBuilder keyToHex = new StringBuilder();
                        int[] array = gson.fromJson(key, int[].class);
                        for (int i = 0; i < array. length; i++){
                            keyToHex.append(Integer.toHexString(array[i])).append(":");
                        }

                        // Отправляем строку в приемник
                        Intent intent = new Intent();
                        intent.putExtra("com.example.snippets.DATA", String.valueOf(keyToHex));
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
    class Test {
        private static final int sizeOfIntInHalfBytes = 8;
        private static final int numberOfBitsInAHalfByte = 4;
        private static final int halfByte = 0x0F;
        private  final char[] hexDigits = {
                '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };

        public  String decToHex(int dec) {
            StringBuilder hexBuilder = new StringBuilder(sizeOfIntInHalfBytes);
            hexBuilder.setLength(sizeOfIntInHalfBytes);
            for (int i = sizeOfIntInHalfBytes - 1; i >= 0; --i)
            {
                int j = dec & halfByte;
                hexBuilder.setCharAt(i, hexDigits[j]);
                dec >>= numberOfBitsInAHalfByte;
            }
            return hexBuilder.toString();
        }

        public  void main(String[] args) {
            int dec = 305445566;
            String hex = decToHex(dec);
            System.out.println(hex);
        }
    }
}
