package com.unplugged.up_antivirus.base

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.text.HtmlCompat
import com.unplugged.antivirus.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity(), BaseView {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showMessage(resId: Int) {
        val message = getStringInternal(resId)
        showMessage(message)
    }

    private fun getStringInternal(resId: Int): String {
        return try {
            getString(resId)
        } catch (e: Resources.NotFoundException) {
            ""
        }
    }

    override fun showDialog(title: Int, message: Int, onConfirm: () -> Unit) {
        showDialog(getStringInternal(title), getStringInternal(message), onConfirm)
    }

    override fun showDialog(title: String, message: String, onConfirm: () -> Unit) {
        val spannedTitle = HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val spannedMessage = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY)

        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setTitle(spannedTitle)
        builder.setMessage(spannedMessage)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.up_av_ok) { dialog, _ ->
            onConfirm()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        dialog.window?.setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.background_dialog))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(this.getColor(R.color.main_text_color))

        // Enable clicking on links in the dialog message
        dialog.findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun showDialog(title: String, message: String, onConfirm: () -> Unit, onCanceled: ()-> Unit) {
        val spannedTitle = HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val spannedMessage = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY)

        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setTitle(spannedTitle)
        builder.setMessage(spannedMessage)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.up_av_yes) { dialog, _ ->
            onConfirm()
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.up_av_no){ dialog, _ ->
            onCanceled()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        dialog.window?.setBackgroundDrawable(this.getDrawable(R.drawable.background_dialog))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(this.getColor(R.color.main_text_color))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(this.getColor(R.color.main_text_color))
    }

    override fun showDialogWithIntent(title: String, message: String, clickableText: String, uriToLoad: String, onConfirm: () -> Unit) {
        val spannableMessage = SpannableString(
            HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY)
        )

        // Find the start and end indices of the clickableText
        val startIndex = spannableMessage.indexOf(clickableText)
        val endIndex = startIndex + clickableText.length

        // Create a ClickableSpan for the clickableText
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Handle the click event by opening a web browser with the uriToLoad
                openUrlInBrowser(uriToLoad)
            }
        }

        // Apply the ClickableSpan to the clickableText
        spannableMessage.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val dialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle(title)
            .setMessage(spannableMessage)
            .setPositiveButton(R.string.up_av_ok) { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .create()

        dialog.show()
        dialog.window?.setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.background_dialog))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(this.getColor(R.color.main_text_color))

        // Enable clicking on the clickableText
        dialog.findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance()
    }

    fun openUrlInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        // Explicitly set the type to avoid other apps handling the intent
        intent.setDataAndType(Uri.parse(url), "text/html")

        // Get all apps that can handle this intent
        val browsers = intent.resolveActivityInfo(this.packageManager, 0)

        // If there is a browser available, launch it
        if (browsers != null) {
            this.startActivity(intent)
        } else {
            // Handle the case where no browser is available
            Toast.makeText(this, "No browser found to open the link", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showTryAgainDialog(title: String, message: String, onConfirm: () -> Unit, onCanceled: ()-> Unit) {
        val spannedTitle = HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val spannedMessage = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY)

        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setTitle(spannedTitle)
        builder.setMessage(spannedMessage)
        builder.setCancelable(false)
        builder.setPositiveButton(getString(R.string.up_av_try_again)) { dialog, _ ->
            onConfirm()
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.up_av_no){ dialog, _ ->
            onCanceled()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        dialog.window?.setBackgroundDrawable(this.getDrawable(R.drawable.background_dialog))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(this.getColor(R.color.main_text_color))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(this.getColor(R.color.main_text_color))
    }
}