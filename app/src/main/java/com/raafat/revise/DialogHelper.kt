package com.raafat.revise

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Helper class to manage dialog creation and display
 */
class DialogHelper(private val context: Context) {

    private var isDialogShown = false
    private var activeDialog: androidx.appcompat.app.AlertDialog? = null

    fun showCustomDialog(
        message: String,
        onPositiveClick: () -> Unit = {},
    ) {
        // Prevent showing multiple dialogs
        if (isDialogShown) {
            activeDialog?.dismiss()
        }

        val customLayout = LayoutInflater.from(context).inflate(R.layout.custom_dialog_layout, null)
        val messageTextView = customLayout.findViewById<TextView>(R.id.dialog_message)
        val positiveButton = customLayout.findViewById<Button>(R.id.positive_button)
        val negativeButton = customLayout.findViewById<Button>(R.id.negative_button)

        messageTextView.text = message

        val dialog = MaterialAlertDialogBuilder(context).setView(customLayout)
            .setCancelable(false) // Prevent dismissal on back button press
            .create()

        dialog.setCanceledOnTouchOutside(false) // Prevent dismissal on outside touch

        positiveButton.setOnClickListener {
            onPositiveClick()
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            isDialogShown = false // Reset the flag when the dialog is dismissed
            activeDialog = null
        }

        isDialogShown = true
        activeDialog = dialog
        dialog.window?.decorView?.layoutDirection = View.LAYOUT_DIRECTION_RTL

        dialog.show()
    }

    fun dismissActiveDialog() {
        activeDialog?.dismiss()
    }
}