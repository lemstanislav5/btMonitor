package com.example.btapp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
// НОВОЕ ПОДКЛЮЧЕНИЕ TOOLBAR
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.btapp.adapter.BtConsts;
import com.example.btapp.bluetooth.BtConnection;

public class MainActivity extends AppCompatActivity {
    private MenuItem menuItem;
    private BluetoothAdapter btAdapter;
    private final int ENABLE_REQUEST = 15;
    private SharedPreferences pref;
    private final  int BT_REQUEST_PERM = 111;
    private BtConnection btConnection;
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
        // ПРИ СТАРТЕ ПЕРВОГО АКТИВИТИ ЗАПРОСИМ ВСЕ НЕОБХОДИМЫЕ РАЗРЕШЕНИЯ

        init();
        getBtPermission();
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