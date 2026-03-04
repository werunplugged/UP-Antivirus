package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.attestation.auth.AttestationAuthManager

class GetOrRefreshAttestationTokenUseCase(private val attestationAuthManager: AttestationAuthManager) {
    suspend fun execute(): String = attestationAuthManager.getOrRefreshToken()
}
