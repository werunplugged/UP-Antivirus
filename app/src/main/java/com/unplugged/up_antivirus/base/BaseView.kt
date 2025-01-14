package com.unplugged.up_antivirus.base

interface BaseView {
    fun showMessage(message: String)
    fun showMessage(resId: Int)
    fun showDialog(title: Int, message: Int, onConfirm: () -> Unit)
    fun showDialog(title: String, message: String, onConfirm: () -> Unit)
    fun showDialog(title: String, message: String, onConfirm: () -> Unit, onCanceled: () -> Unit)
    fun showDialogWithIntent(title: String, message: String, clickableText: String, uriToLoad: String, onConfirm: () -> Unit)
    fun showTryAgainDialog(title: String, message: String, onConfirm: () -> Unit, onCanceled: () -> Unit)
}