package com.unplugged.upantiviruscommon.datastore

import android.content.Context
import android.util.Log
import com.unplugged.accounthelper.AccountHelper
import com.unplugged.upantiviruscommon.model.DatabaseInfo
import com.unplugged.upantiviruscommon.model.ScannerType
import com.unplugged.upantiviruscommon.model.Version
import com.unplugged.upantiviruscommon.retrofit.ApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RemoteDataStore(private val context: Context, private val accountHelper: AccountHelper) {

    private val api: ApiInterface by lazy {
        val okHttpBuilder = OkHttpClient.Builder()
            .addInterceptor(accountHelper.getAttestationTokenInterceptor())
            .addInterceptor(accountHelper.getTokenInterceptor())
        Retrofit.Builder()
            .baseUrl(context.getString(com.unplugged.accounthelper.R.string.base_url))
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpBuilder.build())
            .build()
            .create(ApiInterface::class.java)
    }

    suspend fun fetchBlacklistData(currentVersion: Int): DatabaseInfo =
        withContext(Dispatchers.IO) {
            val response = api.getBlacklistDatabase(currentVersion)
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
                    api.getHypatiaVersion()
                }
                ScannerType.BLACKLIST -> {
                    api.getBlacklistVersion()
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

