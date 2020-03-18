package com.amgregoire.mangafeed.v2.model.dto


import com.google.gson.annotations.SerializedName

data class ApiManga(
        @SerializedName("alternateNames") val alternateNames: String,
        @SerializedName("artists") val artists: String,
        @SerializedName("authors") val authors: String,
        @SerializedName("createdAt") val createdAt: String,
        @SerializedName("description") val description: String,
        @SerializedName("genres") val genres: String,
        @SerializedName("id") val id: String,
        @SerializedName("image") val image: String,
        @SerializedName("link") val link: String,
        @SerializedName("name") val name: String,
        @SerializedName("source") val source: String,
        @SerializedName("status") val status: String,
        @SerializedName("updatedAt") val updatedAt: String
)