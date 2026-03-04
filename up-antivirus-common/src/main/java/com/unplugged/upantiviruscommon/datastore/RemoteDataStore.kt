package com.unplugged.upantiviruscommon.datastore

import android.util.Log
import com.unplugged.upantiviruscommon.BuildConfig
import com.unplugged.upantiviruscommon.model.DatabaseInfo
import com.unplugged.upantiviruscommon.model.ScannerType
import com.unplugged.upantiviruscommon.model.Version
import com.unplugged.upantiviruscommon.retrofit.ApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RemoteDataStore(private val tokenInterceptor: Interceptor) {

    private val api: ApiInterface by lazy {
        val okHttpBuilder = OkHttpClient.Builder()
        okHttpBuilder.addInterceptor(tokenInterceptor)
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
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

