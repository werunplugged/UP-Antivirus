package com.unplugged.up_antivirus.data.account

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.unplugged.upantiviruscommon.model.Resource
import java.util.concurrent.TimeUnit

class RetrofitAccountApi(
    private val baseUrl: String,
) : AccountRemoteSource {

    private var accountClient: RetrofitAccountClient? = null

    init {
        val okHttpBuilder = OkHttpClient.Builder()

        okHttpBuilder.readTimeout(40, TimeUnit.SECONDS)
        okHttpBuilder.writeTimeout(40, TimeUnit.SECONDS)

            val builder = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpBuilder.build())

            val retrofit = builder.build()
            accountClient = retrofit.create(RetrofitAccountClient::class.java)

    }

    override suspend fun updateScanDone(): Resource<Int> {
        remainingScans = 0
        return Resource.success(remainingScans)
    }

    private var remainingScans = 1
    override suspend fun getRemainingScans(): Resource<Int> {
        return Resource.success(remainingScans)
    }
}