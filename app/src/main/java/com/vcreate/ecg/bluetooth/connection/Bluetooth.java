package com.vcreate.ecg.bluetooth.connection;

import static com.example.bluetooth.utils.Constants.REQUEST_ENABLE_BT;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;


import com.example.bluetooth.utils.Constants;
import com.vcreate.ecg.bluetooth.callbacks.BluetoothCallBack;
import com.vcreate.ecg.bluetooth.callbacks.DeviceCallBack;
import com.vcreate.ecg.bluetooth.callbacks.DiscoveryCallBack;
import com.vcreate.ecg.bluetooth.data.PersistentCircularBuffer;
import com.vcreate.ecg.bluetooth.utils.ThreadHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Bluetooth {

    private final static String DEFAULT_UUID = "00001101-0000-1000-8000-00805f9b34fb";
    private final String TAG = "bluetoothtag";
    private final String MY_TAG = "debugTag";

    private Activity activity;
    private Context context;
    private UUID uuid;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    private DeviceCallBack deviceCallBack;
    private DiscoveryCallBack discoveryCallBack;
    private BluetoothCallBack bluetoothCallBack;

    private ReceiveThread receiveThread;
    private boolean connected;
    private boolean runOnUi;
    private String pin;

    private static Bluetooth instance;

    private Bluetooth(Context context) {
        initialize(context, UUID.fromString(DEFAULT_UUID));
    }

    private Bluetooth(Context context, UUID uuid) {
        initialize(context, uuid);
    }

    private PersistentCircularBuffer circularBuffer = new PersistentCircularBuffer(1024);

    public static synchronized Bluetooth getInstance(Context context) {
        if (instance == null) {
            instance = new Bluetooth(context, UUID.fromString(DEFAULT_UUID));
        }
        return instance;
    }

    public static synchronized Bluetooth getInstance(Context context, UUID uuid) {
        if (instance == null) {
            instance = new Bluetooth(context, uuid);
        }
        return instance;
    }


    public void initialize(Context context, UUID uuid) {
        this.context = context;
        this.uuid = uuid;
        this.discoveryCallBack = null;
        this.bluetoothCallBack = null;
        this.runOnUi = false;
    }

    public void onStart() {
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        context.registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    public void onStop() {
        if (bluetoothReceiver != null) {
            context.unregisterReceiver(bluetoothReceiver);
        }
    }


    public void showEnableDialog(Activity activity) {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requestBluetoothPermissions();
                    }
                }
                activity.startActivityForResult(intent, REQUEST_ENABLE_BT);
            }
        }
    }

    public void enable() {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requestBluetoothPermissions();
                    }
                    return;
                }
                bluetoothAdapter.enable();
            }
        }
    }


    public void disable() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requestBluetoothPermissions();
                    }
                    return;
                }
                bluetoothAdapter.disable();
            }
        }
    }

    public BluetoothSocket getSocket() {
        return receiveThread.getSocket();
    }

    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public boolean isEnabled() {
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.isEnabled();
        }
        return false;
    }

    public void setCallbackOnUI(Activity activity) {
        this.activity = activity;
        this.runOnUi = true;
    }

    public void onActivityResult(int requestCode, final int resultCode) {
        if (bluetoothCallBack != null) {
            if (requestCode == REQUEST_ENABLE_BT) {
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        if (resultCode == Activity.RESULT_CANCELED) {
                            bluetoothCallBack.onUserDeniedActivation();
                        }
                    }
                });
            }
        }
    }

    public void connectToAddress(String address, boolean insecureConnection, boolean withPortTrick) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        connectToDevice(device, insecureConnection, withPortTrick);
    }

    public void connectToAddress(String address) {
        connectToAddress(address, false, false);
    }

    public void connectToAddressWithPortTrick(String address) {
        connectToAddress(address, false, true);
    }

    public void connectToName(String name, boolean insecureConnection, boolean withPortTrick) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestBluetoothPermissions();
            }
            return;
        }
        for (BluetoothDevice blueDevice : bluetoothAdapter.getBondedDevices()) {
            if (blueDevice.getName().equals(name)) {
                connectToDevice(blueDevice, insecureConnection, withPortTrick);
                return;
            }
        }
    }

    public void connectToName(String name) {
        connectToName(name, false, false);
    }

    public void connectToNameWithPortTrick(String name) {
        connectToName(name, false, true);
    }


    public void connectToDevice(final BluetoothDevice device, boolean insecureConnection, boolean withPortTrick) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestBluetoothPermissions();
            }
            return;
        }
        Log.d("error", "Inside to connect to device" + device.getName());
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        connect(device, insecureConnection, withPortTrick);
    }

    public void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("error", "no permission");
            return;
        }
        Log.d("error", "Before Inside to connect to device" + device.getName());
        connectToDevice(device, false, false);
        Log.d("error", "After Inside to connect to device" + device.getName());
    }


    public void connectToDeviceWithPortTrick(BluetoothDevice device) {
        connectToDevice(device, false, false);
    }

    public void disconnect() {
        try {
            receiveThread.getSocket().close();
        } catch (final IOException e) {
            if (deviceCallBack != null) {
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        Log.w(getClass().getSimpleName(), e.getMessage());
                        deviceCallBack.onError(Constants.FAILED_WHILE_CLOSING);
                    }
                });
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void send(byte[] data) {
        OutputStream out = receiveThread.getOutputStream();
        try {
            out.write(data);
        } catch (final IOException e) {
            connected = false;
            if (deviceCallBack != null) {
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        deviceCallBack.onDeviceDisconnected(receiveThread.getDevice(), e.getMessage());
                    }
                });
            }
        }
    }

    public void send(String msg, Charset charset) {
        if (charset == null) {
            send(msg.getBytes());
        } else {
            send(msg.getBytes(charset));
        }
    }

    public void send(String msg) {
        send(msg, null);
    }


    public List<BluetoothDevice> getPairedDevices() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestBluetoothPermissions();
            }
            return new ArrayList<>();
        }
        return new ArrayList<>(bluetoothAdapter.getBondedDevices());
    }

    public BluetoothDevice getRemoteDevice(String macAddress) {
        if (bluetoothAdapter == null || macAddress.isEmpty()) {
            return null;
        }
        return bluetoothAdapter.getRemoteDevice(macAddress);
    }

    public void startScanning() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

        context.registerReceiver(scanReceiver, filter);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestBluetoothPermissions();
            }
            return;
        }
        bluetoothAdapter.startDiscovery();
    }


    public void stopScanning() {
        context.unregisterReceiver(scanReceiver);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestBluetoothPermissions();
            }
            return;
        }
        bluetoothAdapter.cancelDiscovery();
    }

    public void pair(BluetoothDevice device, String pin) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        if (pin != null) {
            this.pin = pin;
            filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        }

        context.registerReceiver(pairReceiver, filter);
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (final Exception e) {
            if (discoveryCallBack != null) {
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        Log.w(getClass().getSimpleName(), e.getMessage());
                        discoveryCallBack.onError(Constants.FAILED_TO_PAIR);
                    }
                });
            }
        }
    }

    public void pair(BluetoothDevice device) {
        pair(device, null);
    }

    public void unpair(BluetoothDevice device) {
        context.registerReceiver(pairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (final Exception e) {
            if (discoveryCallBack != null) {
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        Log.w(getClass().getSimpleName(), e.getMessage());
                        discoveryCallBack.onError(Constants.FAILED_TO_UNPAIR);
                    }
                });
            }
        }
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device, boolean insecureConnection) {
        BluetoothSocket socket = null;
        try {
            if (insecureConnection) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requestBluetoothPermissions();
                    }
                    return null;
                }
                socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            } else {
                socket = device.createRfcommSocketToServiceRecord(uuid);
            }
        } catch (IOException e) {
            if (deviceCallBack != null) {
                Log.w(getClass().getSimpleName(), Objects.requireNonNull(e.getMessage()));
                deviceCallBack.onError(Constants.FAILED_WHILE_CREATING_SOCKET);
            }
        }
        return socket;
    }

    private BluetoothSocket createBluetoothSocketWithPortTrick(BluetoothDevice device) {
        BluetoothSocket socket = null;
        try {
            socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(getClass().getSimpleName(), e.getMessage());
        }
        return socket;
    }


    private void connect(BluetoothDevice device, boolean insecureConnection, boolean withPortTrick) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestBluetoothPermissions();
            }
            return;
        }
        Log.d("error", "Inside to connect" + device.getName());
        BluetoothSocket socket = null;
        if (withPortTrick) {
            socket = createBluetoothSocketWithPortTrick(device);
        }
        if (socket == null) {
            try {
                if (insecureConnection) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            requestBluetoothPermissions();
                        }
                        return;
                    }
                    socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                } else {
                    socket = device.createRfcommSocketToServiceRecord(uuid);
                }
            } catch (IOException e) {
                if (deviceCallBack != null) {
                    Log.w(getClass().getSimpleName(), e.getMessage());
                    deviceCallBack.onError(Constants.FAILED_WHILE_CREATING_SOCKET);
                }
            }
        }
        connectInThread(socket, device);
    }

    private void connectInThread(final BluetoothSocket socket, final BluetoothDevice device) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            requestBluetoothPermissions();
                        }
                        return;
                    }
                    socket.connect();
                    connected = true;
                    receiveThread = new ReceiveThread(socket, device);
                    if (deviceCallBack != null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                Log.d("BluetoothTag", "Device Connected");
                                deviceCallBack.onDeviceConnected(device);
                            }
                        });
                    }
                    receiveThread.start();
                } catch (final IOException e) {
                    if (deviceCallBack != null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                deviceCallBack.onConnectError(device, e.getMessage());
                            }
                        });
                    }
                }
            }
        }).start();
    }

    private class ReceiveThread extends Thread implements Runnable {

        private BluetoothSocket socket;
        private BluetoothDevice device;
        private InputStream mmInputStream;
        private OutputStream mmOutputStream;

        public ReceiveThread(BluetoothSocket socket, BluetoothDevice bluetoothDevice) {
            this.socket = socket;
            this.device = bluetoothDevice;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpOut = socket.getOutputStream();
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.w(getClass().getSimpleName(), e.getMessage());
            }
            mmInputStream = tmpIn;
            mmOutputStream = tmpOut;
        }


        @Override
        public void run() {
            byte[] readBuffer = new byte[256];
            byte[] dataBuffer = new byte[1024];

            while (isConnected()) {
                try {
                    int bytesRead = mmInputStream.read(readBuffer);
                    if (bytesRead > 0) {
                        circularBuffer.write(readBuffer, bytesRead);
                    }

                    int dataLength = circularBuffer.read(dataBuffer, dataBuffer.length);
                    if (dataLength > 0 && deviceCallBack != null) {
                        byte[] data = Arrays.copyOf(dataBuffer, dataLength);
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "Message Getting from Machine and sending it to activity " + Arrays.toString(data));
                                deviceCallBack.onMessage(data);
                            }
                        });
                    }
                } catch (IOException e) {
                     connected = false;
                    if (deviceCallBack != null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                deviceCallBack.onDeviceDisconnected(device, e.getMessage());
                            }
                        });
                    }
                }
            }
        }


        public BluetoothSocket getSocket() {
            return socket;
        }

        public BluetoothDevice getDevice() {
            return device;
        }

        public OutputStream getOutputStream() {
            return mmOutputStream;
        }
    }

    private BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                        if (state == BluetoothAdapter.STATE_OFF) {
                            if (discoveryCallBack != null) {
                                ThreadHelper.run(runOnUi, activity, new Runnable() {
                                    @Override
                                    public void run() {
                                        discoveryCallBack.onError(Constants.BLUETOOTH_DISABLED);
                                    }
                                });
                            }
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        if (discoveryCallBack != null) {
                            ThreadHelper.run(runOnUi, activity, new Runnable() {
                                @Override
                                public void run() {
                                    discoveryCallBack.onDiscoveryStarted();
                                }
                            });
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        context.unregisterReceiver(scanReceiver);
                        if (discoveryCallBack != null) {
                            ThreadHelper.run(runOnUi, activity, new Runnable() {
                                @Override
                                public void run() {
                                    discoveryCallBack.onDiscoveryFinished();
                                }
                            });
                        }
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (discoveryCallBack != null) {
                            ThreadHelper.run(runOnUi, activity, new Runnable() {
                                @Override
                                public void run() {
                                    discoveryCallBack.onDeviceFound(device);
                                }
                            });
                        }
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver pairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    context.unregisterReceiver(pairReceiver);
                    if (discoveryCallBack != null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                discoveryCallBack.onDevicePaired(device);
                            }
                        });
                    }
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    context.unregisterReceiver(pairReceiver);
                    if (discoveryCallBack != null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                discoveryCallBack.onDeviceUnpaired(device);
                            }
                        });
                    }
                }
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requestBluetoothPermissions();
                    }
                    return;
                }
                device.setPin(pin.getBytes());
                try {
                    device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    if (discoveryCallBack != null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                discoveryCallBack.onError(-1);
                            }
                        });
                    }
                }
            }
        }
    };

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action!=null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if(bluetoothCallBack!=null) {
                    ThreadHelper.run(runOnUi, activity, new Runnable() {
                        @Override
                        public void run() {
                            switch (state) {
                                case BluetoothAdapter.STATE_OFF:
                                    bluetoothCallBack.onBluetoothOff();
                                    break;
                                case BluetoothAdapter.STATE_TURNING_OFF:
                                    bluetoothCallBack.onBluetoothTurningOff();
                                    break;
                                case BluetoothAdapter.STATE_ON:
                                    bluetoothCallBack.onBluetoothOn();
                                    break;
                                case BluetoothAdapter.STATE_TURNING_ON:
                                    bluetoothCallBack.onBluetoothTurningOn();
                                    break;
                            }
                        }
                    });
                }
            }
        }
    };

    public void setDeviceCallback(DeviceCallBack deviceCallback) {
        this.deviceCallBack = deviceCallback;
    }

    /**
     * Remove device callback. No updates will be received anymore.
     */
    public void removeDeviceCallback(){
        this.deviceCallBack = null;
    }

    /**
     * Callback to receive scanning related updates.
     * @param discoveryCallback Non-null callback.
     */
    public void setDiscoveryCallback(DiscoveryCallBack discoveryCallback){
        this.discoveryCallBack = discoveryCallback;
    }


    /**
     * Remove discovery callback. No updates will be received anymore.
     */
    public void removeDiscoveryCallback(){
        this.discoveryCallBack = null;
    }

    /**
     * Callback to receive bluetooth status related updates.
     * @param bluetoothCallback Non-null callback.
     */
    public void setBluetoothCallback(BluetoothCallBack bluetoothCallback){
        this.bluetoothCallBack = bluetoothCallback;
    }

    /**
     * Remove bluetooth callback. No updates will be received anymore.
     */
    public void removeBluetoothCallback(){
        this.bluetoothCallBack = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void requestBluetoothPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.BLUETOOTH_CONNECT) ||
                ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.BLUETOOTH_SCAN)) {
            new AlertDialog.Builder(context)
                    .setTitle("Permissions needed")
                    .setMessage("These permissions are needed to connect and scan for Bluetooth devices")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.S)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(
                                    (Activity) context,
                                    new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                                    101
                            );
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                    101
            );
        }
    }


}
