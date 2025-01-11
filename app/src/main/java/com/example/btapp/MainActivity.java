package com.example.btapp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.widget.SimpleCursorAdapter;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.btapp.adapter.BtAdapter;
import com.example.btapp.adapter.BtConsts;
import com.example.btapp.bluetooth.BtConnection;
import com.example.btapp.dbHelper.DatabaseHelper;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private MenuItem menuItem;
    private BluetoothAdapter btAdapter;
    private final int ENABLE_REQUEST = 15;
    private SharedPreferences pref;
    private final  int BT_REQUEST_PERM = 111;
    private BtConnection btConnection;
    private Button buttonA, buttonB;
    private BroadcastReceiver myBroadcastReceiver;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor keysCursor;
    SimpleCursorAdapter keysAdapter;
    private ListView listView;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Создаем приемник и его фильтры
        IntentFilter filter = new IntentFilter();
        filter.addAction("MY_ACTION");
        myBroadcastReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("MyLog", "ПРИШЛО УВЕДОМЛЕНИЕ MY_ACTION !!!");
                if (Objects.equals(intent.getAction(), "MY_ACTION")) {
                    String data = intent.getStringExtra("com.example.snippets.DATA");
                    Log.d("MyLog", "данные: " + data);

                    // Создаем экземпляр базы данных
                    databaseHelper = new DatabaseHelper(getApplicationContext());
                    if(databaseHelper.InsertKeyString(data)){
                        Toast.makeText(getApplicationContext(),"Record inserted successfully",Toast.LENGTH_LONG).show();
                        db = databaseHelper.getReadableDatabase();
                        //получаем данные из бд в виде курсора
                        keysCursor =  db.rawQuery("select * from "+ DatabaseHelper.TABLE_NAME, null);
                        // определяем, какие столбцы из курсора будут выводиться в ListView
                        String[] headers = new String[] {DatabaseHelper.COL1, DatabaseHelper.COL2, DatabaseHelper.COL3};
                        // создаем адаптер, передаем в него курсор
//                        keysAdapter = new SimpleCursorAdapter(getApplicationContext(), android.R.layout.two_line_list_item,
//                                keysCursor, headers, new int[]{android.R.id.text1, android.R.id.text2}, 0);
//                        header.setText("Найдено элементов: " +  keysCursor.getCount());
//                    userList.setAdapter(userAdapter);
                    } else {
                        Toast.makeText(getApplicationContext(),"Record not inserted",Toast.LENGTH_LONG).show();
                    }

                }
            }
        };
        registerReceiver(myBroadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        buttonA = findViewById(R.id.sendA);
        buttonB = findViewById(R.id.sendB);
//        остановился здесь
//        listView = findViewById(R.id.listViewKeys);
//        adapter = new BtAdapter(this, R.layout.bt_list_item, list);
//        listView.setAdapter(adapter);

        init();
        //Получаем необходимые разрешения
        getBtPermission();
        //Отправляем сообщения на устройство
        buttonA.setOnClickListener(view -> {
            btConnection.sendMessage("A");
//            String mac = pref.getString(BtConsts.MAC_KEY, "");
//            Log.d("MyLog", "Выбрано устройство: " + mac);
        });
        buttonB.setOnClickListener(view -> {
            btConnection.sendMessage("B");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }

    @SuppressLint("InlinedApi")
    private void getBtPermission(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_ADVERTISE,
                    android.Manifest.permission.BLUETOOTH_SCAN
            }, BT_REQUEST_PERM);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == BT_REQUEST_PERM){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Получены разрешения: *LOCATION & *BLUETOOTH", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Разрешения не предоставлены! Приложение остановлено!", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menuItem = menu.findItem(R.id.id_bt_button);
        setBtIcon();
        return  super.onCreateOptionsMenu(menu);
    }

    private void setBtIcon(){
        if(btAdapter.isEnabled()){
            menuItem.setIcon(R.drawable.baseline_bluetooth_enable_24);
        } else {
            menuItem.setIcon(R.drawable.baseline_bluetooth_disabled_24);
        }
    }
    // Функция инициализации bluetooth адаптера и получение разрешений
    private void init(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        pref = getSharedPreferences(BtConsts.MY_PREF, Context.MODE_PRIVATE);
        btConnection = new BtConnection(this);
        Log.d("MyLog", "Bt MAC " + pref.getString(BtConsts.MAC_KEY, "no bt selected"));
    }

    @Override
    // Функция прослушивает нажатие на элементы меню
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.id_bt_button){
            if(!btAdapter.isEnabled()){
                Toast.makeText(this, "Bluetooth выключен", Toast.LENGTH_SHORT).show();
                enableBt();
            } else {
                // Если bluetooth включен записываем данные в массив
                Toast.makeText(this, "Bluetooth включен", Toast.LENGTH_SHORT).show();
                menuItem.setIcon(R.drawable.baseline_bluetooth_enable_24);
            }
        } else if(item.getItemId() == R.id.id_bt_menu){
            if(btAdapter.isEnabled()){
                Intent intent = new Intent(MainActivity.this, BtListActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Bluetooth выключен", Toast.LENGTH_SHORT).show();
            }

        }else if(item.getItemId() == R.id.id_connect){
            btConnection.connect();
            Toast.makeText(this, "Подключаемся к устройству", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ENABLE_REQUEST){
            if(resultCode == RESULT_OK){
                setBtIcon();
            }
        }
    }

    // Включение bluetooth
    private void enableBt(){
        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You have no permission", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(turnOn);
    }

}