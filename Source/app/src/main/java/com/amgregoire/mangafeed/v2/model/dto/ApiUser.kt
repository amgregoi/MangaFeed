package com.amgregoire.mangafeed.v2.model.dto


import com.google.gson.annotations.SerializedName

data class ApiUser(
        @SerializedName("id") val id: String,
        @SerializedName("createdAt") val createdAt: String,
        @SerializedName("updatedAt") val updatedAt: String,
        @SerializedName("email") val email: String,
        @SerializedName("complete") val complete: List<ApiManga>,
        @SerializedName("mangas") val mangas: List<ApiManga>,
        @SerializedName("onHold") val onHold: List<ApiManga>,
        @SerializedName("planToRead") val planToRead: List<ApiManga>,
        @SerializedName("reading") val reading: List<ApiManga>
)
{
    lateinit var accessToken: String
}