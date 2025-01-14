package com.unplugged.upantiviruscommon.model

import com.google.gson.annotations.SerializedName

data class Version(
    @SerializedName("module_name")val module: String,
    @SerializedName("version")val version: Int)