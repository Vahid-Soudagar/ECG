package com.vcreate.ecg

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.vcreate.ecg.databinding.ActivityEcgPrintBinding
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

class EcgPrintActivity : AppCompatActivity() {

    private lateinit var recordedFileUri: Uri
    private lateinit var stringFileUri: Uri

    private var heartRate = ""
    private var restRate = ""

    private lateinit var binding: ActivityEcgPrintBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEcgPrintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let {
            recordedFileUri = Uri.parse(it.getString("RECORDED_FILE_URI"))
            heartRate = it.getString("HEART_RATE", "")
            restRate = it.getString("REST_RATE", "")
            stringFileUri = Uri.parse(it.getString("ECG_DATA_PATH", ""))
        }

        binding.generatefileandshowecg.setOnClickListener {
            if (::recordedFileUri.isInitialized && recordedFileUri.toString().isNotBlank()) {
                displayECGData(recordedFileUri)
            } else {
                Toast.makeText(this, "File is Empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.pdfBackBtn.setOnClickListener {
            finish()
        }

    }

    private fun displayECGData(filePath: Uri) {
        val allNumbers = parseAllNumbersFromTextFile(filePath)

        val wave1 = getNumbersByIndex(allNumbers, 0)
        val wave2 = getNumbersByIndex(allNumbers, 1)
        val wave3 = getNumbersByIndex(allNumbers, 2)

        Log.d("WavesData", "Wave 1 : $wave1")
        Log.d("WavesData", "Wave 2 : $wave2")
        Log.d("WavesData", "Wave 3 : $wave3")


        val wave1Mv = convertToMilliVolts(wave1)
        val wave2Mv = convertToMilliVolts(wave2)
        val wave3Mv = convertToMilliVolts(wave3)

        Log.d("WavesData", "Wave 1 in mv : $wave1Mv and length is ${wave1Mv.size}")
        Log.d("WavesData", "Wave 2 in mv : $wave2Mv and length is ${wave2Mv.size}")
        Log.d("WavesData", "Wave 3 in mv : $wave3Mv and length is ${wave3Mv.size}")

        binding.ecgGraphView1.addAmp(wave1Mv)
        binding.ecgGraphView2.addAmp(wave2Mv)
        binding.ecgGraphView3.addAmp(wave3Mv)

        binding.ecgGraphView1.invalidate()
        binding.ecgGraphView2.invalidate()
        binding.ecgGraphView3.invalidate()
    }

    private fun parseAllNumbersFromTextFile(path: Uri): List<Int> {
        val numbersList = mutableListOf<Int>()

        try {
            val inputStream = contentResolver.openInputStream(path)
                ?: throw FileNotFoundException("File not found at path: $path")
            val reader = BufferedReader(InputStreamReader(inputStream))

            reader.useLines { lines ->
                lines.forEach { line ->
                    val numbers = line.split(",").map { it.trim().toIntOrNull() }
                    numbers.filterNotNull().forEach { numbersList.add(it) }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }

        return numbersList
    }

    private fun convertToMilliVolts(numbersList: List<Int>): List<Float> {
        return numbersList.map { it.toFloat() / 128 }
    }

    private fun getNumbersByIndex(numbersList: List<Int>, index: Int): List<Int> {
        val result = mutableListOf<Int>()
        for (i in index until numbersList.size step 3) {
            result.add(numbersList[i])
        }
        return result
    }
}