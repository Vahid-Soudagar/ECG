package com.vcreate.ecg.ui

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputLayout
import com.skydoves.powerspinner.PowerSpinnerView
import com.vcreate.ecg.R
import com.vcreate.ecg.data.model.ApiResultDemo
import com.vcreate.ecg.data.model.GuestDetailRequest
import com.vcreate.ecg.ui.viewmodel.MainViewModel
import com.vcreate.ecg.util.FormDialogs
import java.util.Calendar


class EcgDialog(
    private val layout1: Int
) : DialogFragment() {

    private lateinit var timerDialogView: View
    private lateinit var dobCalendar: Calendar

    private var genderList =
        listOf("MALE", "FEMALE")

    private val viewModel: MainViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        timerDialogView = requireActivity().layoutInflater.inflate(layout1, null)
        dialog.setContentView(timerDialogView)

        dobCalendar = Calendar.getInstance()
        val window = dialog.window
        val params = window?.attributes
        params?.width = (resources.displayMetrics.widthPixels * 0.7).toInt()
        window?.attributes = params

        val etPatientGender =
            timerDialogView.findViewById<PowerSpinnerView>(R.id.et_patient_gender_d)
        etPatientGender.setItems(genderList)
        val timerNextBtn = timerDialogView.findViewById<TextView>(R.id.timer_next_btn)
        val timerBackBtn = timerDialogView.findViewById<TextView>(R.id.timer_back_btn)

        val tilPatientDob = timerDialogView.findViewById<TextInputLayout>(R.id.til_patient_dob_d)
        tilPatientDob.setEndIconOnClickListener {
            // Show date picker dialog
            FormDialogs.showDatePickerDialog(requireContext(), dobCalendar, tilPatientDob)
        }

        timerNextBtn.setOnClickListener {
            validateEntryAndApiCall()
        }

        timerBackBtn.setOnClickListener {
            dismiss()
        }

        tilPatientDob.editText?.addTextChangedListener(object : TextWatcher {

            private var current = ""
            private val ddmmyyyy = "DDMMYYYY"
            private val cal = Calendar.getInstance()

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() != current) {
                    var clean = s.toString().replace("[^\\d]".toRegex(), "")
                    val cleanC = current.replace("[^\\d]".toRegex(), "")

                    var sel = clean.length
                    for (i in 2..clean.length step 2) {
                        sel++
                    }

                    // Correct the cursor position
                    if (clean == cleanC) sel--

                    if (clean.length < 8) {
                        clean += ddmmyyyy.substring(clean.length)
                    } else {
                        // Format day, month, year
                        val day = clean.substring(0, 2).toInt()
                        val month = clean.substring(2, 4).toInt()
                        val year = clean.substring(4, 8).toInt()

                        cal.set(Calendar.DAY_OF_MONTH, day)
                        cal.set(Calendar.MONTH, month - 1)
                        cal.set(Calendar.YEAR, year)

                        clean = String.format("%02d%02d%02d", day, month, year)
                    }

                    // Add the separators (-)
                    clean = String.format("%s-%s-%s", clean.substring(0, 2), clean.substring(2, 4), clean.substring(4, 8))

                    sel = Math.max(sel, 0)
                    current = clean
                    tilPatientDob.editText?.setText(current)
                    tilPatientDob.editText?.setSelection(Math.min(sel, current.length))
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        return dialog
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun dismiss() {
        Log.d("myTag", "In dismiss")
        super.dismiss()
    }


    /**
     * Validates the user input fields and makes an API call if all the required fields are valid.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun validateEntryAndApiCall() {

        // Validate user input fields
        validateEntries()

        // Check if all the error messages for input fields are empty
        val etPatientName = timerDialogView.findViewById<EditText>(R.id.et_patient_name_d)
        val etPatientDob = timerDialogView.findViewById<EditText>(R.id.et_patient_dob_d)
        val etPatientGender =
            timerDialogView.findViewById<PowerSpinnerView>(R.id.et_patient_gender_d)
        val etPatientMobile = timerDialogView.findViewById<EditText>(R.id.et_patient_mobile_d)

        if (etPatientName.error.isNullOrEmpty() &&
            etPatientDob.error.isNullOrEmpty() &&
            etPatientGender.error.isNullOrEmpty() &&
            etPatientMobile.error.isNullOrEmpty()
        ) {

            val list = emptyList<String>()
            val machineId = "OPASTI"

            // Create a guest details object with the validated inputs
            val guestDetails =
                GuestDetailRequest(
                    chronicDiseasesList = list,
                    allergiesList = list,
                    countryCode = "+91",
                    number = etPatientMobile.text.toString(),
                    dateOfBirth = etPatientDob.text.toString(),
                    fullName = etPatientName.text.toString(),
                    gender = etPatientGender.text.toString(),
                    machineId = machineId,
                    operatorId = machineId
                )

            Log.d("ApiTestingTag", "Guest details: $guestDetails")
            // Call the API to sign in the guest using the prepared guest details
            guestSignInApi(guestDetails)
        }
    }

    /**
     * This function is responsible for validating the entries filled by the user in the UI form fields.
     * It retrieves the input values from various EditText fields, validates them using ViewModel methods,
     * and observes the LiveData for error messages to display appropriate error messages if validation fails.
     */

    private fun validateEntries() {
        val etPatientName = timerDialogView.findViewById<EditText>(R.id.et_patient_name_d)
        val etPatientDob = timerDialogView.findViewById<EditText>(R.id.et_patient_dob_d)
        val etPatientGender =
            timerDialogView.findViewById<PowerSpinnerView>(R.id.et_patient_gender_d)
        val etPatientMobile = timerDialogView.findViewById<EditText>(R.id.et_patient_mobile_d)

        // Validate patient name
        viewModel.validateName(
            etPatientName.text.toString(),
            "Name"
        )
        // Observe the LiveData for name validation error message and display it if validation fails
        viewModel.nameError.observe(this) { error ->
            etPatientName.error = error
        }

        // Validate patient gender
        viewModel.validateGender(
            etPatientGender.text.toString(), "Gender"
        )
        // Observe the LiveData for gender validation error message and display it if validation fails
        viewModel.genderError.observe(this) { error ->
            etPatientGender.error = error
        }

        // Validate patient date of birth
        viewModel.validateDob(
            etPatientDob.text.toString(), "DOB"
        )
        // Observe the LiveData for date of birth validation error message and display it if validation fails
        viewModel.dobError.observe(this) { error ->
            etPatientDob.error = error
        }

        // Validate patient mobile number
        viewModel.validateNumber(
            etPatientMobile.text.toString()
        )
        // Observe the LiveData for mobile number validation error message and display it if validation fails
        viewModel.numberError.observe(this) { error ->
            etPatientMobile.error = error
        }
    }

    /**
     * Function to make an API call for guest sign-in with the provided guest details.
     * @param guestDetails The details of the guest to sign in.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun guestSignInApi(guestDetails: GuestDetailRequest) {

        // Make API call to sign in the guest using ViewModel
        viewModel.guestSignIn(guestDetails)

        // Observe the result of the API call using LiveData
        viewModel.guestSignInResult.observe(this, Observer { result ->
            // Handle different cases based on the result of the API request
            when (result) {
                // If the request is successful, process the received data
                is ApiResultDemo.Success -> {
                    // Log success message and received data
                    Log.d("ApiTestingTag", "Data request success: ${result.data}")


                    // Display success message to the user
                    Toast.makeText(
                        requireActivity(), "Registration Successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    dismiss()
                    val preferenceManager = com.vcreate.ecg.util.PreferenceManager(requireContext())
                    preferenceManager.saveGuestDetails(guestDetails)
                }

                // If the API request encounters an error, log the error message
                is ApiResultDemo.Error -> {
                    // Log error message when the API request encounters an error
                    val errorResponse = result.error
                    val errorMessages = if (!errorResponse.messages.isNullOrEmpty()) {
                        errorResponse.messages.joinToString(separator = "\n")
                    } else {
                        errorResponse.message
                    }
                    Log.d("ApiTestingTag", "error: $errorMessages")
                    if (errorMessages != null) {
                        Toast.makeText(requireContext(), errorMessages, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
