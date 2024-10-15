package com.vcreate.ecg

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.vcreate.ecg.bluetooth.callbacks.DeviceCallBack
import com.vcreate.ecg.bluetooth.connection.Bluetooth
import com.vcreate.ecg.bluetooth.data.DataParser
import com.vcreate.ecg.bluetooth.data.ECG
import com.vcreate.ecg.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var bluetooth: Bluetooth
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var dataParser: DataParser

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val waveData1 = ConcurrentLinkedQueue<Int>()
    private val waveData2 = ConcurrentLinkedQueue<Int>()
    private val waveData3 = ConcurrentLinkedQueue<Int>()

    private lateinit var recordingTimer: CountDownTimer

    private var isTimerRunning = false

    private var isRecording: Boolean = false
    private var recordingStartTime: Long = 0
    private var recordingDuration = ""

    private var recordedFile: File? = null

    private var stringFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetooth = Bluetooth.getInstance(this).apply {
            setCallbackOnUI(this@MainActivity)
            setDeviceCallback(deviceCallBack)
        }

        dataParser = DataParser(onEcgReceivedListener1, onEcgReceivedListener2)
        dataParser.start()

        onClickListener()

    }


    @SuppressLint("SetTextI18n")
    private fun onClickListener() {
        // connect bluetooth
        binding.connectBluetooth.setOnClickListener {
            val intent = Intent(this, BluetoothConnectionActivity::class.java)
            startActivity(intent)
        }

        binding.btnChangeTimer.setOnClickListener {
            showEditDurationDialog(this) { newDuration ->
                binding.timer.text = "$newDuration sec"
                recordingDuration = "${newDuration}000"
            }
        }

        binding.btnRecordEcg.setOnClickListener {
            if (bluetooth.isConnected) {
                if (!isRecording) {
                    Log.d("EcgActivity", "Button Click Recording Started")
                    startRecording()
                }
            } else {
                Toast.makeText(this, "Bluetooth is not Connected", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnStartEcg.setOnClickListener {
            if (binding.btnStartEcg.text == "Start") {
                startECGProcess()
            } else {
                stopEcgProcess()
            }
        }

        binding.btnPrint.setOnClickListener {
            if (!isRecording) {
                recordedFile?.let { file ->
                    val fileUri: Uri = FileProvider.getUriForFile(
                        this,
                        "${applicationContext.packageName}.fileprovider",
                        file
                    )

                    val intent = Intent(this, EcgPrintActivity::class.java).apply {
                        putExtra("RECORDED_FILE_URI", fileUri.toString())
                        putExtra("HEART_RATE", 100)
                        putExtra("REST_RATE", 100)
                        putExtra("ECG_DATA_PATH", stringFile?.absolutePath)
                    }
                    startActivity(intent)
                } ?: run {
                    Toast.makeText(this, "No recorded file available", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Print will not work while recording", Toast.LENGTH_SHORT)
                    .show()
            }
        }


    }

    private fun stopEcgProcess() {
        binding.btnStartEcg.text = "Start"
        binding.ecgStatus.text = "Ecg Stopped"
    }

    private fun startRecording() {
        waveData1.clear()
        waveData2.clear()
        waveData3.clear()
        isRecording = true
        recordingStartTime = System.currentTimeMillis()
        Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
        binding.recordingStatus.text = "Recording Started"
        startRecordingTimer()
    }

    private fun startRecordingTimer() {
        recordingTimer = object : CountDownTimer(recordingDuration.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.timer.text = "00:${if (secondsLeft < 10) "0" else ""}$secondsLeft"
            }

            override fun onFinish() {
                binding.timer.text = "00:00"
                stopRecording()
            }
        }
        recordingTimer.start()
    }

    private fun stopRecording() {
        isRecording = false
        val recordingDuration = System.currentTimeMillis() - recordingStartTime
        Toast.makeText(
            this,
            "Recording stopped. Duration: ${recordingDuration / 1000} seconds",
            Toast.LENGTH_SHORT
        ).show()
        Log.d("TestingSize", "Size of Recorded data of wave 1 for $recordingDuration ${waveData1.size}")
        Log.d("TestingSize", "Size of Recorded data of wave 2 for $recordingDuration ${waveData3.size}")
        Log.d("TestingSize", "Size of Recorded data of wave 3 for $recordingDuration ${waveData3.size}")
        val combinedWaveData = ConcurrentLinkedQueue<Int>()
        while (waveData1.isNotEmpty() || waveData2.isNotEmpty() || waveData3.isNotEmpty()) {
            waveData1.poll()?.let { combinedWaveData.add(it) }
            waveData2.poll()?.let { combinedWaveData.add(it) }
            waveData3.poll()?.let { combinedWaveData.add(it) }
        }
        binding.recordingStatus.text = "Recording Stopped "
        recordedFile = saveDataToFile(combinedWaveData, "Vahid Soudagar")
    }

    private fun saveDataToFile(dataQueue: ConcurrentLinkedQueue<Int>, patientName: String): File? {
        val currentTime = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "${currentTime}_${patientName}_wave1data.txt"
        val directory = File(filesDir, "ECGWaveData")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, filename)
        try {
            FileOutputStream(file).use { fos ->
                dataQueue.forEach { data ->
                    fos.write("$data,".toByteArray())
                }
            }
            Log.d("EcgActivity", "Data saved to file: $filename at ${file.absoluteFile}")

            val downloadDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val destFile = File(downloadDir, filename)
            file.copyTo(destFile, overwrite = true)
            Log.d("EcgActivity", "File copied to download directory: ${destFile.absolutePath}")
            Toast.makeText(
                this@MainActivity,
                "Recorded Text File is created with the name $filename",
                Toast.LENGTH_LONG
            ).show()
            return destFile
        } catch (e: Exception) {
            Log.e("EcgActivity", "TAG saving data to file: $filename", e)
        }

        return null
    }

    private fun connectBluetooth() {
        if (this::bluetooth.isInitialized) {
            if (!bluetooth.isConnected) {
                val deviceAddress = SensitiveAddressPreferenceManager(this).getBluetoothAddress()
                if (deviceAddress != null) {
                    if (deviceAddress.isNotEmpty()) {
                        bluetoothDevice = bluetooth.getRemoteDevice(deviceAddress)
                        if (::bluetoothDevice.isInitialized) {
                            bluetooth.connectToDeviceWithPortTrick(bluetoothDevice)
                        } else {
                            Log.d("TAG", "Bluetooth device is not initialised")
                        }
                    } else {
                        Log.d("TAG", "Connect bluetooth first")
                    }
                }
            }
        }
    }

    private val deviceCallBack = object : DeviceCallBack {
        override fun onDeviceConnected(device: BluetoothDevice?) {
            Log.d("TAG", "Device Connected")
            runOnUiThread {
                binding.status.text = "Connected"
            }
        }


        override fun onDeviceDisconnected(device: BluetoothDevice?, message: String?) {
            Log.d("TAG", "Device disconnected $message")
            runOnUiThread {
                binding.status.text = "Connected"
            }
        }

        override fun onMessage(message: ByteArray?) {
            if (message != null) {
                parseDataInBackground(message)
            }
        }

        override fun onError(errorCode: Int) {
            binding.status.text = errorCode.toString()
        }

        override fun onConnectError(device: BluetoothDevice?, message: String?) {
            Log.d("TAG", "TAG while connecting Trying again")
            binding.status.text = "Error While Connectiong..."

            val handler = Handler()
            handler.postDelayed({
                binding.status.text = "Trying to Reconnect"
                connectBluetooth()
            }, 3000)
        }
    }

    private fun parseDataInBackground(data: ByteArray) {
        Executors.newSingleThreadExecutor().submit {
            dataParser.add(data)
        }
    }

    private val onEcgReceivedListener1 = object : DataParser.OnEcgReceivedListener1 {
        override fun onEcgReceived(ecg: ECG?) {
            coroutineScope.launch {
                ecg?.let {
                    withContext(Dispatchers.Main) {
                        Log.d("receivecheck", "Data receive after parsing $ecg")
                    }
                }
            }
        }

        override fun onEcgWave1Received(dat: Int) {
            coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    Log.d("receivecheck", "Data receive after parsing $dat")
                }
            }
        }

        override fun onEcgWave2Received(dat: Int) {
            coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    Log.d("receivecheck", "Data receive after parsing $dat")
                }
            }
        }

        override fun onEcgWave3Received(dat: Int) {
            coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    Log.d("receivecheck", "Data receive after parsing $dat")
                }
            }
        }
    }

    private val onEcgReceivedListener2 = object : DataParser.OnEcgReceivedListener2 {
        override fun onEcgReceived(ecg: ECG?) {
            coroutineScope.launch {
                ecg?.let {
                }
            }
        }

        override fun onEcgWave1Received(dat: Int) {
            coroutineScope.launch {
                processEcgWave1Data(dat)
            }
        }

        override fun onEcgWave2Received(dat: Int) {
            coroutineScope.launch {
                processEcgWave2Data(dat)
            }
        }

        override fun onEcgWave3Received(dat: Int) {
            coroutineScope.launch {
                processEcgWave3Data(dat)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bluetooth.onStart()
    }

    private fun startECGProcess() {
        if (this::bluetooth.isInitialized && !bluetooth.isConnected) {
            connectBluetooth()
        }
        binding.btnStartEcg.text = "Stop"
        binding.ecgStatus.text = "Ecg Started"
        startTimer()
    }

    private fun startTimer() {
        isTimerRunning = true
        val timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
            }

            override fun onFinish() {
                stopEcgProcess()
            }
        }
        timer.start()
    }

    private fun processEcgWave1Data(dat: Int) {
        if (isRecording) {
            waveData1.add(dat)
        }
    }

    private fun processEcgWave2Data(dat: Int) {
        if (isRecording) {
            waveData2.add(dat)
        }
    }

    private fun processEcgWave3Data(dat: Int) {
        if (isRecording) {
            waveData3.add(dat)
        }
    }
}