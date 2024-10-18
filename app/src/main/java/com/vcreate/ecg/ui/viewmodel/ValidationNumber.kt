package com.vcreate.ecg.ui.viewmodel

import android.util.Patterns
import com.vcreate.ecg.util.EntryValidationResult

class ValidationNumber {

    fun execute(Number:String) : EntryValidationResult {

        if (Number.isBlank()){
            return EntryValidationResult(
                false,
                "Phone Number can't be blank."
            )
        }

        if (!Patterns.PHONE.matcher(Number).matches()){
            return EntryValidationResult(
                false,
                "Invalid Phone Number."
            )
        }

        if (Number.length != 10){
            return EntryValidationResult(
                false,
                "Enter a 10 digit number."
            )
        }

        return EntryValidationResult(true)

    }

}