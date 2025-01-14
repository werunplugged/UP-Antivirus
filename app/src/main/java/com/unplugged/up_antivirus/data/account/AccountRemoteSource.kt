package com.unplugged.up_antivirus.data.account

import com.unplugged.upantiviruscommon.model.Resource

interface AccountRemoteSource {

    suspend fun updateScanDone(): Resource<Int>
    suspend fun getRemainingScans(): Resource<Int>
}