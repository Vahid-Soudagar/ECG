package com.vcreate.ecg.ui

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.vcreate.ecg.R
import com.vcreate.ecg.bluetooth.callbacks.BluetoothCallBack
import com.vcreate.ecg.bluetooth.callbacks.DiscoveryCallBack
import com.vcreate.ecg.bluetooth.connection.Bluetooth
import com.vcreate.ecg.databinding.ActivityBluetoothConnectionBinding
import com.vcreate.ecg.util.SensitiveAddressPreferenceManager

class BluetoothConnectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBluetoothConnectionBinding

    private lateinit var bluetooth: Bluetooth
    private var pairedListAdapter: ArrayAdapter<String>? = null
    private lateinit var pairedDevices: List<BluetoothDevice>

    private var isReceiverRegistered:Boolean = false
    private var scanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pairedListAdapter = ArrayAdapter(this, R.layout.device_item, ArrayList())
        binding.activityScanPairedList.adapter = pairedListAdapter
        binding.activityScanPairedList.onItemClickListener = onPairedListItemClick

        bluetooth = Bluetooth.getInstance(this)
        bluetooth.setCallbackOnUI(this)
        bluetooth.setBluetoothCallback(bluetoothCallBack)
        bluetooth.setDiscoveryCallback(discoveryCallback)


    }



    private val onPairedListItemClick =
        AdapterView.OnItemClickListener { adapterView, view, i, l ->
            if (scanning) {
                bluetooth.stopScanning()
            }
            saveDeviceAddress(pairedDevices[i])
        }

    private fun displayPairedDevices() {
        // Check if the permission is already granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If permission is not granted, request the permission
            requestBluetoothConnectPermission()
            return
        }

        // If permission is granted, proceed with fetching paired devices
        pairedDevices = bluetooth.pairedDevices
        for (device in pairedDevices) {
            pairedListAdapter!!.add(device.address + " : " + device.name)
        }
    }

    private fun requestBluetoothConnectPermission() {
        // Check if we should show a rationale
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        ) {
            // Show an explanation to the user
            AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("This permission is needed to access paired Bluetooth devices")
                .setPositiveButton("OK") { dialog, which ->
                    // Request the permission again
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                        BLUETOOTH_CONNECT_PERMISSION_REQUEST_CODE
                    )
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .create()
                .show()
        } else {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                BLUETOOTH_CONNECT_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            BLUETOOTH_CONNECT_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted, proceed with displaying paired devices
                    displayPairedDevices()
                } else {
                    // Permission denied, show rationale and ask again
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.BLUETOOTH_CONNECT
                        )
                    ) {
                        requestBluetoothConnectPermission()
                    } else {
                        // User has denied the permission and checked "Don't ask again"
                        Toast.makeText(this, "Bluetooth connect permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (isReceiverRegistered) {
            bluetooth.onStop()
            isReceiverRegistered = false
        }
    }

    override fun onStart() {
        super.onStart()
        bluetooth.onStart()
        isReceiverRegistered = true
        if (bluetooth.isEnabled) {
            displayPairedDevices()
        } else {
            bluetooth.showEnableDialog(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        bluetooth.onActivityResult(requestCode, resultCode)
    }


    private fun saveDeviceAddress(device: BluetoothDevice) {
        SensitiveAddressPreferenceManager(this).saveBluetoothAddress(device.address)
        Toast.makeText(this, "Address Saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        const val BLUETOOTH_CONNECT_PERMISSION_REQUEST_CODE = 101
    }

    private val bluetoothCallBack = object: BluetoothCallBack {


        override fun onBluetoothTurningOn() {

        }

        override fun onBluetoothOn() {
            displayPairedDevices()
        }

        override fun onBluetoothTurningOff() {
        }

        override fun onBluetoothOff() {

        }

        override fun onUserDeniedActivation() {
            Toast.makeText(this@BluetoothConnectionActivity, "I need to activate bluetooth...", Toast.LENGTH_SHORT)
                .show()

        }
    }

    private val discoveryCallback = object: DiscoveryCallBack {
        override fun onDiscoveryStarted() {
        }

        override fun onDiscoveryFinished() {
        }


        override fun onDeviceFound(device: BluetoothDevice?) {

        }

        override fun onDevicePaired(device: BluetoothDevice?) {
            Toast.makeText(this@BluetoothConnectionActivity, "Paired !", Toast.LENGTH_SHORT).show()
            if (device != null) {
                saveDeviceAddress(device)
            }
        }

        override fun onDeviceUnpaired(device: BluetoothDevice?) {
        }

        override fun onError(errorCode: Int) {
        }

    }
}