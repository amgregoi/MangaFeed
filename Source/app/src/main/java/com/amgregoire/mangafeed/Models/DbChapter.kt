package com.amgregoire.mangafeed.Models

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "Chapter", primaryKeys = ["url", "source", "mangaUrl"])
class DbChapter(
        @ColumnInfo(name = "id")
        var id: String?,

        @ColumnInfo(name = "url")
        var url: String,

        @ColumnInfo(name = "date")
        var chapterDate: String?,

        @ColumnInfo(name = "mangaTitle")
        var mangaTitle: String,

        @ColumnInfo(name = "mangaUrl")
        var mangaUrl: String,

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

    @Transient
    var bitmaps:List<Bitmap> = listOf()

    constructor() : this(
            id = null,
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

    constructor(title: String, url: String, mangaUrl: String, source: String)
            : this(
            id = null,
            url = url,
            chapterDate = "",
            mangaTitle = title,
            mangaUrl = mangaUrl,
            chapterTitle = title,
            chapterNumber = 0,
            currentPage = 0,
            totalPages = 0,
            downloadStatus = 0,
            source = source
    )

    constructor(chUrl: String, mangaTitle: String, chTitle: String, date: String, chNum: Int, mangaUrl: String, source: String)
            : this(
            id = null,
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
            id = null,
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
    }
}
