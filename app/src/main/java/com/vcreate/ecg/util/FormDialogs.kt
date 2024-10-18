package com.vcreate.ecg.util

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object FormDialogs {
    /**
     * Function to show a DatePickerDialog to select a date and display it in a TextInputLayout.
     *
     * @param context The context in which the dialog should be shown.
     * @param dobCalendar The Calendar object representing the current selected date.
     * @param textInputLayout The TextInputLayout where the selected date will be displayed.
     */
    fun showDatePickerDialog(context: Context, dobCalendar: Calendar, textInputLayout: TextInputLayout) {
        // Create a DatePickerDialog with a listener for date selection
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, day ->
                // Update the Calendar with the selected date
                dobCalendar.set(year, month, day)

                // Format the date and display it in a desired format
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formattedDate = sdf.format(dobCalendar.time)

                // Display the selected date in a TextInputLayout
                textInputLayout.editText?.setText(formattedDate)
            },
            dobCalendar.get(Calendar.YEAR), // Initial year to display in the dialog
            dobCalendar.get(Calendar.MONTH), // Initial month to display in the dialog
            dobCalendar.get(Calendar.DAY_OF_MONTH) // Initial day to display in the dialog
        )

        // Set a maximum date to restrict future dates
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        // Show the date picker dialog
        datePickerDialog.show()
    }



}