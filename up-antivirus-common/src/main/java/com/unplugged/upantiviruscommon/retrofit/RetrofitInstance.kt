package com.unplugged.upantiviruscommon.retrofit

import com.unplugged.account.UpAccount
import com.unplugged.upantiviruscommon.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val api: ApiInterface by lazy {
        val okHttpBuilder = OkHttpClient.Builder()
        okHttpBuilder.addInterceptor(UpAccount.getTokenInterceptor())
        Retrofit.Builder()
            .baseUrl(BuildConfig.DEV_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpBuilder.build())
            .build()
            .create(ApiInterface::class.java)
    }
}