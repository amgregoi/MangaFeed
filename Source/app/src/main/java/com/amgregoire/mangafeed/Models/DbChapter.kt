package com.amgregoire.mangafeed.Models

import android.os.Parcelable

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "Chapter")
class DbChapter(
        @PrimaryKey(autoGenerate = true)
        var _id: Int? = null,

        @ColumnInfo(name = "url")
        var url: String,

        @ColumnInfo(name = "date")
        var chapterDate: String?,

        @ColumnInfo(name = "mangaTitle")
        var mangaTitle: String,

        @ColumnInfo(name = "mangaUrl")
        var mangaUrl: String?,

        @ColumnInfo(name = "chapterTitle")
        var chapterTitle: String,

        @ColumnInfo(name = "chapterNumber")
        var chapterNumber: Int = 0,

        @ColumnInfo(name = "currentPage")
        var currentPage: Int = 0,

        @ColumnInfo(name = "totalPages")
        var totalPages: Int = 0,

        @ColumnInfo(name = "downloadStatus")
        var downloadStatus: Int = 0,

        @ColumnInfo(name = "source")
        var source: String
) : Parcelable
{
    @Transient
    var mDownloadChecked = false

    constructor() : this(
            _id = null,
            url = "",
            chapterDate = "",
            mangaTitle = "",
            mangaUrl = "",
            chapterTitle = "",
            chapterNumber = 0,
            currentPage = 0,
            totalPages = 0,
            downloadStatus = 0,
            source = "Unknown"
    )

    constructor(title: String, url: String, source: String)
            : this(
            _id = null,
            url = url,
            chapterDate = "",
            mangaTitle = title,
            mangaUrl = "",
            chapterTitle = title,
            chapterNumber = 0,
            currentPage = 0,
            totalPages = 0,
            downloadStatus = 0,
            source = source
    )

    constructor(chUrl: String, mangaTitle: String, chTitle: String, date: String, chNum: Int, mangaUrl: String, source: String)
            : this(
            _id = null,
            url = chUrl,
            chapterDate = date,
            mangaTitle = mangaTitle,
            mangaUrl = mangaUrl,
            chapterTitle = chTitle,
            chapterNumber = chNum,
            currentPage = 0,
            totalPages = 0,
            downloadStatus = 0,
            source = source
    )

    constructor(chUrl: String, mangaTitle: String, chTitle: String, date: String, mangaUrl: String, source: String)
            : this(
            _id = null,
            url = chUrl,
            chapterDate = date,
            mangaTitle = mangaTitle,
            mangaUrl = mangaUrl,
            chapterTitle = chTitle,
            chapterNumber = 0,
            currentPage = 0,
            totalPages = 0,
            downloadStatus = 0,
            source = source
    )

    override fun toString(): String
    {
        return chapterTitle
    }

    companion object
    {
        val TAG = "CHAPTER"

        val DOWNLOAD_STATUS_NONE = 0
        val DOWNLOAD_STATUS_DOWNLOADING = 1
        val DOWNLOAD_STATUS_FINISHED = 2
    }
}
