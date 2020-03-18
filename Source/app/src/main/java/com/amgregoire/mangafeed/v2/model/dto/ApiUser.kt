package com.amgregoire.mangafeed.v2.model.dto


import com.google.gson.annotations.SerializedName

data class ApiUser(
        @SerializedName("id") val id: String,
        @SerializedName("createdAt") val createdAt: String,
        @SerializedName("updatedAt") val updatedAt: String,
        @SerializedName("name") val name: String,
        @SerializedName("email") val email: String,
        @SerializedName("complete") val complete: List<Any>,
        @SerializedName("mangas") val mangas: List<Any>,
        @SerializedName("onHold") val onHold: List<Any>,
        @SerializedName("planToRead") val planToRead: List<Any>,
        @SerializedName("reading") val reading: List<Any>
)
{
    lateinit var accessToken: String
}