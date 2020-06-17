package com.amgregoire.mangafeed.Models

import android.os.Parcelable
import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.amgregoire.mangafeed.Common.MangaEnums
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "Manga", primaryKeys = ["link", "source"])
class DbManga(

        @Nullable
        @ColumnInfo(name = "id")
        var id: String? = null,

        @ColumnInfo(name = "title")
        var title: String,

        @ColumnInfo(name = "image")
        var image: String?,

        @ColumnInfo(name = "link")
        var link: String,

        @ColumnInfo(name = "description")
        var description: String?,

        @ColumnInfo(name = "author")
        var author: String?,

        @ColumnInfo(name = "artist")
        var artist: String?,

        @ColumnInfo(name = "genres")
        var genres: String?,

        @ColumnInfo(name = "status")
        var status: String?,

        @ColumnInfo(name = "source")
        var source: String,

        @ColumnInfo(name = "alternate")
        var alternate: String?,

        @ColumnInfo(name = "following")
        var following: Int,

        @ColumnInfo(name = "initialized")
        var initialized: Int = 0,

        @ColumnInfo(name = "recentChapter")
        var recentChapter: String?
) : Parcelable
{
    val fullUrl: String
        get() = link.replace("{$source}", MangaEnums.Source.valueOf(source).baseUrl)

    val isFollowing: Boolean
        get() = following > 0

    constructor(
            title: String,
            url: String,
            source: String
    ) : this(
            id = null,
            title = title,
            image = "",
            link = url.replaceFirst(LinkRegex.toRegex(), "{$source}"),
            description = "",
            author = "",
            artist = "",
            genres = "",
            status = "",
            source = source,
            alternate = "",
            following = 0,
            initialized = 0,
            recentChapter = ""
    )

    override fun toString(): String
    {
        return title
    }

    override fun equals(obj: Any?): Boolean
    {
        var lCompare = false
        if (obj != null && obj is DbManga)
        {
            val lLink1 = fullUrl.replace("https", "http")
            val lLink2 = obj.fullUrl.replace("https", "http")
            if (lLink1 == lLink2 && source == obj.source)
            {
                lCompare = true
            }
        }

        return lCompare
    }

    override fun hashCode(): Int
    {
        return id.hashCode()
    }

    companion object
    {
        val TAG = "MANGA"
        var LinkRegex = "(http)*s*:\\/\\/www.[a-zA-Z]*.(com|net|org|cc)"
    }

}
