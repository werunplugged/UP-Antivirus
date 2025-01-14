package com.unplugged.upantiviruscommon.retrofit

import com.unplugged.upantiviruscommon.model.DatabaseInfo
import com.unplugged.upantiviruscommon.model.Version
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("/blacklist/current.json?")
    suspend fun getBlacklistDatabase(@Query("version") version: Int): Response<DatabaseInfo>?
    @GET("/hypatia/current.json?")
    suspend fun getHypatiaDatabase(@Query("version") version: Int): Response<DatabaseInfo>?
    @GET("/blacklist/version")
    suspend fun getBlacklistVersion(): Response<Version>?
    @GET("/hypatia/version")
    suspend fun getHypatiaVersion(): Response<Version>?
}