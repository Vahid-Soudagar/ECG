package com.vcreate.ecg.bluetooth.callbacks;

import android.bluetooth.BluetoothDevice;

public interface DiscoveryCallBack {
    void onDiscoveryStarted();
    void onDiscoveryFinished();
    void onDeviceFound(BluetoothDevice device);
    void onDevicePaired(BluetoothDevice device);
    void onDeviceUnpaired(BluetoothDevice device);
    void onError(int errorCode);
}
