package com.vcreate.ecg.bluetooth.callbacks;

public interface BluetoothCallBack {
    void onBluetoothTurningOn();

    void onBluetoothOn();

    void onBluetoothTurningOff();

    void onBluetoothOff();

    void onUserDeniedActivation();
}
