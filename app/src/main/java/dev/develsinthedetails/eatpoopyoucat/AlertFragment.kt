package dev.develsinthedetails.eatpoopyoucat

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class AlertFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.dialog_one_entry_allowed)
                .setNegativeButton(R.string.ok) { _, _ -> }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
