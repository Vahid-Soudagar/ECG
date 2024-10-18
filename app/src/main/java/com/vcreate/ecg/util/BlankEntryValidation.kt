package com.vcreate.ecg.util

class BlankEntryValidation {

    fun execute(number:String, entryType: String) : EntryValidationResult {

        if (number.isBlank()){
            return EntryValidationResult(
                false,
                "$entryType can't be blank."
            )
        }

        return EntryValidationResult(true)

    }

}