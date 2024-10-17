package com.vcreate.ecg

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.vcreate.ecg.databinding.ActivityEcgPrintBinding
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.time.LocalDateTime

class EcgPrintActivity : AppCompatActivity() {

    private lateinit var recordedFileUri: Uri
    private lateinit var stringFileUri: Uri

    private var heartRate = ""
    private var restRate = ""

    private lateinit var binding: ActivityEcgPrintBinding
    @RequiresApi(Build.VERSION_CODES.O)
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

        binding.printPdfBtn.setOnClickListener {
            lifecycleScope.launch {
                createPdf()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPdf() {
        // Create a patient object with specified details.
        val patient = Patient(0, "Rahul Soni", "21", "Male")

        // Initialize an EcgPdfService object to handle PDF generation.
        val ecgPdfService = EcgPdfService()

        // Get the current date and time.
        val currentDateTime = LocalDateTime.now()

        // Generate snapshots (bitmap images) of the ECG.
        val bitmapList = generateSnapshots()

        // Get bitmap images from drawable resources for parameters.
        val parameterBitmapList = getBitmapFromDrawable()

        // Create the ECG report PDF and obtain the file path.
        val path = ecgPdfService.createEcgReport(
            patient,
            "Bridge Healthcare",
            currentDateTime,
            bitmapList,
            parameterBitmapList,
            heartRate,
            restRate
        )


        // Get the system print manager service.
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager

        try {
            // Create a print document adapter for the PDF.
            val printDocumentAdapter =
                PdfDocumentAdapter(
                    this@EcgPrintActivity,
                    path
                )

            // Print the PDF document with a specified name and print attributes.
            printManager.print(
                "Test Report",
                printDocumentAdapter,
                PrintAttributes.Builder().build()
            )
        } catch (e: Exception) {
            // Log any exceptions that occur during printing.
            Log.d("error", e.toString())
        }
    }

    private fun getBitmapFromDrawable(): List<Bitmap> {
        val lungsBitmap =
            BitmapFactory.decodeResource(resources, R.drawable.icon_lungs)
        val heartBitmap =
            BitmapFactory.decodeResource(resources, R.drawable.icon_heart_)
        return listOf(lungsBitmap, heartBitmap)
    }

    private fun generateSnapshots(): List<Bitmap> {
        val bitmap1 = getBitmapFromView(binding.ecgGraphView1)
        val bitmap2 = getBitmapFromView(binding.ecgGraphView2)
        val bitmap3 = getBitmapFromView(binding.ecgGraphView3)
        return listOf(bitmap1, bitmap2, bitmap3)
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(
            view.width, view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
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