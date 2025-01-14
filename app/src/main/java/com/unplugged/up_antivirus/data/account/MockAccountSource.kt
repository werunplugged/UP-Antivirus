package com.unplugged.up_antivirus.data.account

import com.unplugged.upantiviruscommon.model.Resource

class MockAccountSource : AccountRemoteSource {

    private var remainingScans = 1

    override suspend fun updateScanDone(): Resource<Int> {
        remainingScans = 0
        return Resource.success(remainingScans)
    }

    override suspend fun getRemainingScans(): Resource<Int> {
        return Resource.success(remainingScans)
    }
}