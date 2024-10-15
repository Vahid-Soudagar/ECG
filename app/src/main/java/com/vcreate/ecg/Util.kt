package com.vcreate.ecg

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

// Function to show the edit duration dialog and pass the new duration via a lambda callback
fun showEditDurationDialog(context: Context, onDurationChanged: (String) -> Unit) {
    // Inflate the custom layout for the dialog
    val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_duration, null)

    // Build the dialog
    val dialogBuilder = AlertDialog.Builder(context)
        .setView(dialogView)
        .setCancelable(false)

    // Create the AlertDialog
    val alertDialog = dialogBuilder.create()

    // Access the EditText and buttons in the dialog
    val editTextDuration = dialogView.findViewById<EditText>(R.id.et_duration)
    val saveButton = dialogView.findViewById<Button>(R.id.btn_save)
    val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancel)

    // Set click listener for Save button
    saveButton.setOnClickListener {
        // Get the entered duration
        val newDuration = editTextDuration.text.toString()

        // Check if the duration is not empty and pass it to the callback
        if (newDuration.isNotEmpty()) {
            onDurationChanged(newDuration)
        }

        // Close the dialog
        alertDialog.dismiss()
    }

    // Set click listener for Cancel button
    cancelButton.setOnClickListener {
        // Close the dialog without making changes
        alertDialog.dismiss()
    }

    // Show the dialog
    alertDialog.show()
}
