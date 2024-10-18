package com.vcreate.ecg.util

import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

class EcgPdfService {

    private val TAG = "EcgPdfService"

    val TITLE_FONT = com.itextpdf.text.Font(
        com.itextpdf.text.Font.FontFamily.TIMES_ROMAN,
        16f,
        com.itextpdf.text.Font.BOLD
    )
    val MED_FONT = com.itextpdf.text.Font(
        com.itextpdf.text.Font.FontFamily.TIMES_ROMAN,
        14f,
        com.itextpdf.text.Font.BOLD
    )
    val BODY_FONT = com.itextpdf.text.Font(
        com.itextpdf.text.Font.FontFamily.TIMES_ROMAN,
        12f,
        com.itextpdf.text.Font.NORMAL
    )
    private lateinit var pdf: PdfWriter

    private fun createFile(patientName: String?, filePath: String): File {
        val fileName = "${System.currentTimeMillis()}-$patientName"
        val title = "$fileName.pdf"
        val file = File(filePath, title)
        Log.d("EcgPdfService", file.absolutePath)
        if (!file.exists()) file.createNewFile()
        return file
    }

    private fun createDocument(): Document {
        val document = Document()
        document.setMargins(24f, 24f, 20f, 0f)
        document.pageSize = PageSize.A4.rotate()

        return document
    }

    private fun setupPdfWriter(document: Document, file: File) {
        pdf = PdfWriter.getInstance(document, FileOutputStream(file))
        pdf.setFullCompression()
        //Open the document
        document.open()
    }

    private fun createTable(column: Int, columnWidth: FloatArray): PdfPTable {
        val table = PdfPTable(column)
        table.widthPercentage = 100f
        table.setWidths(columnWidth)
        table.headerRows = 1
        table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        table.defaultCell.horizontalAlignment = Element.ALIGN_CENTER
        return table
    }

    private fun createCell(content: String): PdfPCell {
        val cell = PdfPCell(Phrase(content))
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        //setup padding
        cell.setPadding(8f)
        cell.isUseAscender = true
        cell.paddingLeft = 4f
        cell.paddingRight = 4f
        cell.paddingTop = 8f
        cell.paddingBottom = 8f
        return cell
    }

    private fun addLineSpace(document: Document, number: Int) {
        for (i in 0 until number) {
            document.add(Paragraph(" "))
        }
    }

    private fun createParagraph(content: String): Paragraph{
        val paragraph = Paragraph(content, BODY_FONT)
        paragraph.firstLineIndent = 25f
        paragraph.alignment = Element.ALIGN_JUSTIFIED
        return paragraph
    }


    private fun createTitle(content: String) : Paragraph{
        val paragraph = Paragraph(content, TITLE_FONT)
        paragraph.alignment = Element.ALIGN_CENTER
        return paragraph
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun detailsPage(document: Document, companyName: String, patient: Patient, dateTime: LocalDateTime) {
        val table = PdfPTable(2)
        table.widthPercentage = 100f

        // Add the company name in the first column, aligned to the left
        val companyNameCell = PdfPCell(Paragraph(companyName, TITLE_FONT))
        companyNameCell.border = PdfPCell.NO_BORDER
        companyNameCell.horizontalAlignment = Element.ALIGN_LEFT
        table.addCell(companyNameCell)

        // Add the patient name in the second column, aligned to the right
        val patientNameCell = PdfPCell(Paragraph("Patient: ${patient.name}", TITLE_FONT))
        patientNameCell.border = PdfPCell.NO_BORDER
        patientNameCell.horizontalAlignment = Element.ALIGN_RIGHT
        table.addCell(patientNameCell)

        // Add the date and time in the first column, second row aligned to the left just below company name
        val date = dateTime.dayOfMonth
        val month = dateTime.month.name
        val year = dateTime.year
        val hr = dateTime.hour
        val min = dateTime.minute


        val recordDateTime = "Recorded On $month $date, $year time $hr:$min"
        val dateTimeCell = PdfPCell(Paragraph(recordDateTime, BODY_FONT))
        dateTimeCell.border = PdfPCell.NO_BORDER
        dateTimeCell.horizontalAlignment = Element.ALIGN_LEFT
        table.addCell(dateTimeCell)


        // Add the patient age and gender in the second column, second row aligned to the right
        val patientInfoCell = PdfPCell(Paragraph("Age: ${patient.age}    Gender: ${patient.sex}", BODY_FONT))
        patientInfoCell.border = PdfPCell.NO_BORDER
        patientInfoCell.horizontalAlignment = Element.ALIGN_RIGHT
        table.addCell(patientInfoCell)

        // Add the table to the document
        document.add(table)
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }


    private fun parametersDetails(bitmaps: List<Bitmap>, respirationRate: String, heartRate: String) : PdfPTable{
        val parameterTable = PdfPTable(4)
        parameterTable.widthPercentage = 100f
        parameterTable.horizontalAlignment = Element.ALIGN_RIGHT

        // Add the lungs image in the first column with rowspan
        val lungImage = Image.getInstance(bitmapToByteArray(bitmaps[0]))
        lungImage.scaleToFit(20f, 20f)
        val lungsImageCell = PdfPCell(lungImage)
        lungsImageCell.rowspan = 2 // Merge the cell vertically with the next row
        lungsImageCell.border = PdfPCell.NO_BORDER
        lungsImageCell.horizontalAlignment = Element.ALIGN_RIGHT
        lungsImageCell.verticalAlignment = Element.ALIGN_MIDDLE
        parameterTable.addCell(lungsImageCell)

        // Add the respiration rate in the second column
        val respirationCell = PdfPCell(Paragraph("Respiration Rate", MED_FONT))
        respirationCell.horizontalAlignment = Element.ALIGN_CENTER
        respirationCell.border = PdfPCell.NO_BORDER
        parameterTable.addCell(respirationCell)

        // Add the heart image in the third column with rowspan
        val heartImage = Image.getInstance(bitmapToByteArray(bitmaps[1]))
        heartImage.scaleToFit(20f, 20f)
        val heartImageCell = PdfPCell(heartImage)
        heartImageCell.rowspan = 2 // Merge the cell vertically with the next row
        heartImageCell.horizontalAlignment = Element.ALIGN_RIGHT
        heartImageCell.verticalAlignment = Element.ALIGN_MIDDLE
        heartImageCell.border = PdfPCell.NO_BORDER
        parameterTable.addCell(heartImageCell)

        // Add the heart rate in the fourth column
        val heartRateCell = PdfPCell(Paragraph("Heart Rate", MED_FONT))
        heartRateCell.horizontalAlignment = Element.ALIGN_CENTER
        heartRateCell.border = PdfPCell.NO_BORDER
        parameterTable.addCell(heartRateCell)

        // Add the respiration rate value below the image in the second row
        val respirationValueCell = PdfPCell(Paragraph(respirationRate, BODY_FONT))
        respirationValueCell.horizontalAlignment = Element.ALIGN_CENTER
        respirationValueCell.border = PdfPCell.NO_BORDER
        parameterTable.addCell(respirationValueCell)

        // Add the heart rate value below the image in the fourth column
        val heartRateValueCell = PdfPCell(Paragraph(heartRate, BODY_FONT))
        heartRateValueCell.horizontalAlignment = Element.ALIGN_CENTER
        heartRateValueCell.border = PdfPCell.NO_BORDER
        parameterTable.addCell(heartRateValueCell)

        return parameterTable
    }

    private fun addSummary(parameterData: ParameterData) : PdfPTable {
        val parameterTable = PdfPTable(6)
        parameterTable.widthPercentage = 100f
        parameterTable.horizontalAlignment = Element.ALIGN_LEFT

        // Add the lungs image in the first column with rowspan
        val summaryCell = PdfPCell(Paragraph("Summary", MED_FONT))
        summaryCell.horizontalAlignment = Element.ALIGN_CENTER
        summaryCell.border = PdfPCell.NO_BORDER
        summaryCell.horizontalAlignment = Element.ALIGN_RIGHT
        summaryCell.verticalAlignment = Element.ALIGN_MIDDLE
        parameterTable.addCell(summaryCell)

        // Add the respiration rate in the second column
        val hrCell = PdfPCell(Paragraph("HR-", MED_FONT))
        hrCell.horizontalAlignment = Element.ALIGN_CENTER
        hrCell.border = PdfPCell.NO_BORDER
        parameterTable.addCell(hrCell)

        val hrCellValue = PdfPCell(Paragraph(parameterData.hr.toString(), BODY_FONT))
        hrCellValue.horizontalAlignment = Element.ALIGN_CENTER
        hrCellValue.border = PdfPCell.NO_BORDER
        parameterTable.addCell(hrCellValue)


        // Add the heart rate in the fourth column
        val qrCell = PdfPCell(Paragraph("PR", MED_FONT))
        qrCell.horizontalAlignment = Element.ALIGN_CENTER
        qrCell.border = PdfPCell.NO_BORDER
        parameterTable.addCell(qrCell)

        val qrCellValue = PdfPCell(Paragraph(parameterData.pr.toString(), BODY_FONT))
        qrCellValue.horizontalAlignment = Element.ALIGN_CENTER
        qrCellValue.border = PdfPCell.NO_BORDER
        parameterTable.addCell(qrCellValue)

        // Add the heart rate in the fourth column
        val qrsCell = PdfPCell(Paragraph("QRS", MED_FONT))
        qrsCell.horizontalAlignment = Element.ALIGN_CENTER
        qrsCell.border = PdfPCell.NO_BORDER
        parameterTable.addCell(qrsCell)

        val qrsCellValue = PdfPCell(Paragraph(parameterData.qrs.toString(), BODY_FONT))
        qrsCellValue.horizontalAlignment = Element.ALIGN_CENTER
        qrsCellValue.border = PdfPCell.NO_BORDER
        parameterTable.addCell(qrCellValue)

        val stCell = PdfPCell(Paragraph("ST Level", MED_FONT))
        stCell.horizontalAlignment = Element.ALIGN_CENTER
        stCell.border = PdfPCell.NO_BORDER
        parameterTable.addCell(stCell)

        val stCellValue = PdfPCell(Paragraph(parameterData.stLevel.toString(), BODY_FONT))
        stCellValue.horizontalAlignment = Element.ALIGN_CENTER
        stCellValue.border = PdfPCell.NO_BORDER
        parameterTable.addCell(qrCellValue)

        return parameterTable
    }

    private fun addImage(bitmap: Bitmap, document: Document) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val image = Image.getInstance(byteArray)

        // Calculate scaling factor based on document width and image width
        val scalingFactor = (document.pageSize.width / image.width)
        image.scalePercent(scalingFactor * 100) // Scale image to match document width

        image.alignment = Element.ALIGN_CENTER
        document.add(image)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun createEcgReport(
        patient: Patient,
        companyName: String,
        dateTime: LocalDateTime,
        bitmaps: List<Bitmap>,
        parameterBitmaps: List<Bitmap>,
        heartRate: String,
        respirationRate: String,
        data: String?,
        parameterData: ParameterData
    ) : String {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = createFile(patient.name, downloadsDir.toString())
        val document = createDocument()

        setupPdfWriter(document, file)

        Log.d(TAG, "File Created at ${file.absolutePath}")

        // patient details
        detailsPage(document, companyName, patient, dateTime)
        addLineSpace(document, 1)


        // parameters details
        val table = PdfPTable(2)
        table.widthPercentage = 100f

        val cardeogramParagraph = Paragraph("Electrocardiogram", TITLE_FONT)
        cardeogramParagraph.alignment = Element.ALIGN_CENTER

        val cardeogramCell = PdfPCell(cardeogramParagraph)
        cardeogramCell.border = PdfPCell.NO_BORDER
        table.addCell(cardeogramCell)

        val parameterTable = parametersDetails(parameterBitmaps, respirationRate, heartRate)
        val parameterCell = PdfPCell(parameterTable)
        parameterCell.border = PdfPCell.NO_BORDER
        table.addCell(parameterCell)

        document.add(table)
        addLineSpace(document, 1)

        // add graph image
        addImage(bitmaps[0], document)
        document.add(Paragraph("I", BODY_FONT))

        addImage(bitmaps[1], document)
        document.add(Paragraph("II", BODY_FONT))

        addImage(bitmaps[2], document)
        document.add(Paragraph("III", BODY_FONT))

        if (data != null && data == "param") {
            val summarTable = addSummary(parameterData)
            document.add(summarTable)
        }

        document.close()

        try {
            pdf.close()
        } catch (ex: Exception) {
            Log.d("myTag", ex.toString())
        }

        return file.absolutePath
    }


}