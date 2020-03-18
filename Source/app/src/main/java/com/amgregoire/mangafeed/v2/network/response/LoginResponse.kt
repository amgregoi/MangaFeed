package com.amgregoire.mangafeed.v2.network.response

import com.amgregoire.mangafeed.v2.model.dto.ApiUser
import com.google.gson.annotations.SerializedName

class LoginResponse(
        @SerializedName("accessToken") val accessToken: String,
        @SerializedName("user") val user: ApiUser
)