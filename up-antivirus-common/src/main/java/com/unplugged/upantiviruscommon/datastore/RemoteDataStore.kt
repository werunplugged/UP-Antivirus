package com.unplugged.upantiviruscommon.datastore

import android.util.Log
import com.unplugged.upantiviruscommon.model.DatabaseInfo
import com.unplugged.upantiviruscommon.model.ScannerType
import com.unplugged.upantiviruscommon.model.Version
import com.unplugged.upantiviruscommon.retrofit.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteDataStore {
    suspend fun fetchBlacklistData(currentVersion: Int): DatabaseInfo =
        withContext(Dispatchers.IO) {
            val response = RetrofitInstance.api.getBlacklistDatabase(currentVersion)
            if (response?.isSuccessful == true) {
                if (response.code() == 200) {
                    return@withContext response.body()!!
                } else {
                    return@withContext DatabaseInfo(currentVersion, listOf(), listOf())
                }
            } else {
                val errorMessage = response?.message()
                Log.d("Error", "fetchSignatureListData: $errorMessage")
                return@withContext DatabaseInfo(currentVersion, listOf(), listOf())
            }
        }

    suspend fun getDatabaseVersion(module: ScannerType): Version =
        withContext(Dispatchers.IO) {
            val response = when (module) {
                ScannerType.HYPATIA -> {
                    RetrofitInstance.api.getHypatiaVersion()
                }
                ScannerType.BLACKLIST -> {
                    RetrofitInstance.api.getBlacklistVersion()
                }
                else -> {
                    throw IllegalArgumentException("Invalid module name: $module")
                }
            }

            if (response?.isSuccessful == true) {
                return@withContext response.body()!!
            } else {
                val errorMessage = response?.message()
                Log.d("Error", "getHypatiaVersion: $errorMessage")
                return@withContext Version("", -1)
            }
        }
}

