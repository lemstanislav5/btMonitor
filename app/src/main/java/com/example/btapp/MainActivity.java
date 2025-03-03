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
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import com.example.btapp.adapter.BtConsts;
import com.example.btapp.bluetooth.BtConnection;
import com.example.btapp.dbHelper.DatabaseHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private MenuItem menuItem;
    private BluetoothAdapter btAdapter;
    private final int ENABLE_REQUEST = 15;
    private SharedPreferences pref;
    private final  int BT_REQUEST_PERM = 111;
    private BtConnection btConnection;
    private Button send, update;
    private ImageButton delete;
    private BroadcastReceiver myBroadcastReceiver;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private Cursor keysCursor;
    private SimpleCursorAdapter keysAdapter;
    private ListView keysListView;
    private String selectedKey = "";
    private TextView selectedKeyTextView;
    private EditText editText;
    private Cursor selectedKeyCursor;
    private Cursor checkKeyCursor;
    private TextView readKey;
    private int position;
    private boolean isConnected = false;

    private boolean isDeviceSelected = false;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
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
        filter.addAction("MY_NOTIFICATION");
        filter.addAction("MY_CONNECTION");
        myBroadcastReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Objects.equals(intent.getAction(), "MY_ACTION")) {
                    String data = intent.getStringExtra("com.example.snippets.DATA");
                    readKey.setText(data);
                    Log.d("MyLog", "данные: " + data);
                    //----------------------------------------------------------- база данных ----------------------------------------------------------
                    // Создаем экземпляр базы данных
                    databaseHelper = new DatabaseHelper(getApplicationContext());
                    db = databaseHelper.getReadableDatabase();
                    // Проверяем наличие ключа в базе данных
                    checkKeyCursor =  db.rawQuery("select * from "+ DatabaseHelper.TABLE_NAME + " where " + DatabaseHelper.COLUMN_KEY_STRING + "=?", new String [] {data});
                    int count = checkKeyCursor.getCount();
                    if(count == 0){
                        if(databaseHelper.InsertKeyString(data)){
                            db = databaseHelper.getReadableDatabase();
                            // получаем данные из бд в виде курсора
                            keysCursor =  db.rawQuery("select * from "+ DatabaseHelper.TABLE_NAME, null);
                            // определяем, какие столбцы из курсора будут выводиться в ListView
                            String[] headers = new String[] {DatabaseHelper.COLUMN_KEY_STRING, DatabaseHelper.COLUMN_ADDRESS};
                            keysAdapter = new SimpleCursorAdapter(getApplicationContext(), android.R.layout.two_line_list_item,
                                    keysCursor, headers, new int[]{android.R.id.text1, android.R.id.text2}, 0);
                            keysListView.setAdapter(keysAdapter);
                            //----------------------------------------------------------- база данных ----------------------------------------------------------
                        } else {
                            Toast.makeText(getApplicationContext(),"Ошибка записи базы данных!",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(),"Ключь уже записан!",Toast.LENGTH_LONG).show();
                    }
                    checkKeyCursor.close();
                } else if (Objects.equals(intent.getAction(), "MY_NOTIFICATION")) {
                    String notification = intent.getStringExtra("com.example.snippets.DATA");
                    Toast.makeText(getApplicationContext(),notification,Toast.LENGTH_LONG).show();
                } else if (Objects.equals(intent.getAction(), "MY_CONNECTION")) {
                    String notification = intent.getStringExtra("com.example.snippets.DATA");
                    if(Objects.equals(notification, "opened")){
                        isConnected = true;
                        Toast.makeText(getApplicationContext(),"Соединение установлено!",Toast.LENGTH_LONG).show();
                    } else if(Objects.equals(notification, "closed")){
                        isConnected = false;
                        Toast.makeText(getApplicationContext(),"Соединение закрыто!",Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
        registerReceiver(myBroadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        update = findViewById(R.id.update);
        send = findViewById(R.id.send);
        delete = findViewById(R.id.del);
        keysListView = findViewById(R.id.listKeys);
        selectedKeyTextView = findViewById(R.id.selectedKey);
        editText = findViewById(R.id.editText);
        readKey = findViewById(R.id.readKey);

        init();
        //Получаем необходимые разрешения
        getBtPermission();

        update.setOnClickListener(view -> {
            String address = editText.getText().toString();
            editText.setText("");

            if(!Objects.equals(selectedKey, "")){
                editText.setText("");
                databaseHelper = new DatabaseHelper(getApplicationContext());
                databaseHelper.update(address, selectedKey);
                updateView(position, address);
            }
        });

        //Отправляем сообщения на устройство
        send.setOnClickListener(view -> {
            if(isConnected){
                if(!Objects.equals(selectedKey, "")){
                    String[] data = selectedKey.split(":");
                    String message = "[" + selectedKey.replace(':', ',') + "]";
                    btConnection.sendMessage(message);
                } else {
                    Toast.makeText(getApplicationContext(),"Ключ не выбран!",Toast.LENGTH_SHORT).show();
                }
                editText.setText("");
            } else {
                Toast.makeText(getApplicationContext(),"Устройство не подключено!",Toast.LENGTH_SHORT).show();
            }

        });

        delete.setOnClickListener(view -> {
            editText.setText("");
            databaseHelper = new DatabaseHelper(getApplicationContext());
            databaseHelper.delete(selectedKey);
            deleteView(position);
            Toast.makeText(getApplicationContext(),"Ключь удален!",Toast.LENGTH_SHORT).show();
        });
    }

    // Функция обновляет поле TextView android.R.id.text2 из ListView
    private void updateView(int index, String address){
        View v = keysListView.getChildAt(index - keysListView.getFirstVisiblePosition());
        if(v == null)
            return;
        TextView someText = (TextView) v.findViewById(android.R.id.text2);
        someText.setText(address);
        // Меняем цвет измененного текста
        someText.setTextColor(Color.parseColor("#0F9D58"));
    }
    // Функция обновляет адаптер при удалении элемента
    private void deleteView(int index){
        View v = keysListView.getChildAt(index - keysListView.getFirstVisiblePosition());
        if(v == null)
            return;
        keysCursor =  db.rawQuery("select * from "+ DatabaseHelper.TABLE_NAME, null);
        String[] headers = new String[] {DatabaseHelper.COLUMN_KEY_STRING, DatabaseHelper.COLUMN_ADDRESS};
        keysAdapter = new SimpleCursorAdapter(getApplicationContext(), android.R.layout.two_line_list_item,
                keysCursor, headers, new int[]{android.R.id.text1, android.R.id.text2}, 0);
        keysAdapter.changeCursor(keysCursor);
        keysListView.setAdapter(keysAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //----------------------------------------------------------- база данных ----------------------------------------------------------
        databaseHelper = new DatabaseHelper(getApplicationContext());
        db = databaseHelper.getReadableDatabase();
        keysCursor =  db.rawQuery("select * from "+ DatabaseHelper.TABLE_NAME, null);
        String[] headers = new String[] {DatabaseHelper.COLUMN_KEY_STRING, DatabaseHelper.COLUMN_ADDRESS};
        keysAdapter = new SimpleCursorAdapter(getApplicationContext(), android.R.layout.two_line_list_item,
                keysCursor, headers, new int[]{android.R.id.text1, android.R.id.text2}, 0);
        keysListView.setAdapter(keysAdapter);

        keysListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("Range")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                editText.setText("");
                Log.d("MyLog", "itemClick: position = " + i + ", id = "  + l);
                String key = keysCursor.getString(1);
                selectedKey = key;
                selectedKeyTextView.setText(selectedKey);
                position = i;

                databaseHelper = new DatabaseHelper(getApplicationContext());
                db = databaseHelper.getReadableDatabase();
                selectedKeyCursor =  db.rawQuery("select * from "+ DatabaseHelper.TABLE_NAME + " where " + DatabaseHelper.COLUMN_KEY_STRING + "=?", new String [] {selectedKey});
                if (selectedKeyCursor != null) {
                    if (selectedKeyCursor.moveToFirst()) {
                        String str;
                        do {
                            str = "";
                            for (String cn : selectedKeyCursor.getColumnNames()) {
                                str = str.concat(cn + " = " + selectedKeyCursor.getString(selectedKeyCursor.getColumnIndex(cn)) + "; ");
                            }
                            Log.d("MyLog", str);
                        } while (selectedKeyCursor.moveToNext());
                    }
                } else{
                    Log.d("MyLog", "Cursor is null");
                }

                // не забывайте закрыть курсор
                selectedKeyCursor.close();
                Log.d("MyLog", "key = " + key);
            }
        });
        //----------------------------------------------------------- база данных ----------------------------------------------------------
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        keysCursor.close();
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
                Toast.makeText(this, "Получены разрешения: *LOCATION & *BLUETOOTH.", Toast.LENGTH_SHORT).show();
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
        if(!pref.getString(BtConsts.MAC_KEY, "").isEmpty()){
            isDeviceSelected = true;
        } else {
            isDeviceSelected = false;
        }
        Log.d("MyLog", "Bt MAC " + pref.getString(BtConsts.MAC_KEY, ""));
    }

    @Override
    // Функция прослушивает нажатие на элементы меню
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.id_bt_button){
            if(!btAdapter.isEnabled()){
                Toast.makeText(this, "Bluetooth выключен.", Toast.LENGTH_SHORT).show();
                enableBt();
            } else {
                // Если bluetooth включен записываем данные в массив
                Toast.makeText(this, "Bluetooth включен.", Toast.LENGTH_SHORT).show();
                menuItem.setIcon(R.drawable.baseline_bluetooth_enable_24);
            }
        } else if(item.getItemId() == R.id.id_bt_menu){
            if(btAdapter.isEnabled()){
                Intent intent = new Intent(MainActivity.this, BtListActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Bluetooth выключен.", Toast.LENGTH_SHORT).show();
            }

        }else if(item.getItemId() == R.id.id_connect){
            if(isDeviceSelected){
                btConnection.connect();
                Toast.makeText(this, "Подключаемся к устройству.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Устройство не выбрано!", Toast.LENGTH_SHORT).show();
            }

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