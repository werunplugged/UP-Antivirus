package com.unplugged.up_antivirus.ui.splash

data class TokenState(
    val isLoading: Boolean = false,
    val tokenExist: Boolean = false,
    val error: String? = null
)
