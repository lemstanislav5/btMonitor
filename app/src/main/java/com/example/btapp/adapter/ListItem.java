package com.example.btapp.adapter;

import android.bluetooth.BluetoothDevice;

public class ListItem {
    private BluetoothDevice btDevice;
    private String itemType = BtAdapter.DEF_ITEM_TYPE;

    public BluetoothDevice getBtDevice() {
        return btDevice;
    }

    public String getItemType() {
        return itemType;
    }

    public void setBtDevice(BluetoothDevice btDevice) {
        this.btDevice = btDevice;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
}
