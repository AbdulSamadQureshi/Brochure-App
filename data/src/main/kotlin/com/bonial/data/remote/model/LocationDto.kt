package com.bonial.data.remote.model

import com.google.gson.annotations.SerializedName

data class LocationDto(
    @SerializedName("name")
    val name: String?,
)
