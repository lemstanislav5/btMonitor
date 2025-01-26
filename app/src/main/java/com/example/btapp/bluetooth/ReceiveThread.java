package com.example.btapp.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

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

    public  void sendNotification(String filter, String str){
        Intent intent = new Intent();
        intent.putExtra("com.example.snippets.DATA", String.valueOf(str));
        intent.setPackage("com.example.btapp");
        intent.setAction(filter);
        context.sendBroadcast(intent);
    }

    @Override
    public void run() {
        sendNotification("MY_CONNECTION", "opened");
        Log.d("MyLog", "class ReceiveThread run()");
        //Объявляем поле, которое будет хранить предыдущее время срабатывания
        long previousSystemTime = System.currentTimeMillis();
        byte[] rBuffer = new byte[40];
        int size;
        String key = "";
        while (true){
            try {
                size = inputStream.read(rBuffer);
                String newKey = new String(rBuffer, 0, size);
                // Входящее сообщение должно соответствовать формату [1,d3,c8,a7,1,0,0,38]
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
                        int[] array;
                        StringBuilder keyToHex = new StringBuilder();
                        try{
                            array = gson.fromJson(key, int[].class);
                            for (int i = 0; i < array.length; i++){
                                if(i < array.length - 1){
                                    keyToHex.append(Integer.toHexString(array[i])).append(":");
                                } else {
                                    keyToHex.append(Integer.toHexString(array[i]));
                                }

                            }

                            // Отправляем строку в приемник
                            sendNotification("MY_ACTION", String.valueOf(keyToHex));
                        }catch(JsonSyntaxException e){
                            Log.d("MyLog", "Сотрока не соответствует формату JSON!");
                        }
                    }
                } else if (newKey.equals("#")) {
                    // Отправляем строку в приемник
                    sendNotification("MY_NOTIFICATION", String.valueOf("Режим записи болванки!"));
                    Log.d("MyLog", "Режим записи болванки!");
                } else if (newKey.equals("&")) {
                    sendNotification("MY_NOTIFICATION", String.valueOf("Запись болванки закончена!"));
                    Log.d("MyLog", "Запись болванки закончена!");
                } else {
                    Log.d("MyLog", "Сотрока не соответствует критериям! ");
                }
            } catch (IOException err){
                sendNotification("MY_CONNECTION", "closed");
                Log.d("MyLog", "run: " + String.valueOf(err));
                break;
            }
            if((System.currentTimeMillis() - previousSystemTime) > 5000) { // если текущее время минус предыдущее больше 1000 миллисекунд
                key = ""; // возвращаем исходные данные ключа для возможности повторного чтения
                previousSystemTime = System.currentTimeMillis(); // и записываем новое значение предыдущего времени
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
