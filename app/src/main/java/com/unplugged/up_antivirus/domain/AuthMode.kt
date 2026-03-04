package com.unplugged.up_antivirus.domain

sealed class AuthMode {
    object Account : AuthMode()
    object Attestation : AuthMode()
}
