package com.amgregoire.mangafeed.Models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.Utils.MangaFeedRest
import com.amgregoire.mangafeed.Utils.MangaLogger
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject

@Parcelize
@Entity(tableName = "Manga")
class DbManga(
        @PrimaryKey(autoGenerate = true)
        var _id: Int = 0,

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
            _id = 0,
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

    fun updateFollowing(value: Int): Int
    {
        following = value
        MangaDB.getInstance().putManga(this)
        updateFollowItem()
        return following
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
        if (_id == 0) return this.hashCode()
        return _id
    }

    /***
     * This function posts a follow update to the server
     */
    private fun updateFollowItem()
    {
        val lUserId = SharedPrefs.getUserId()

        if (lUserId < 0)
        {
            return
        }

        val params = RequestParams()
        params.put("image", image)
        params.put("url", link)
        params.put("followStatus", following)

        MangaFeedRest.postFollowedUpdate(lUserId, params, object : JsonHttpResponseHandler()
        {
            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?)
            {
                super.onSuccess(statusCode, headers, response)
                MangaLogger.logError(TAG, response!!.toString())
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONObject?)
            {
                super.onFailure(statusCode, headers, throwable, errorResponse)
            }
        })
    }

    companion object
    {
        val TAG = "MANGA"
        var LinkRegex = "(http)*s*:\\/\\/www.[a-zA-Z]*.(com|net|org|cc)"

        fun mangaToFormattedUrl(dbManga:DbManga) = dbManga.link.replaceFirst(LinkRegex.toRegex(), "{${dbManga.source}}")
        fun urlSourceToFormattedUrl(url:String, source: String) = url.replaceFirst(LinkRegex.toRegex(), "{${source}}")

        const val UNFOLLOW = 0
        const val FOLLOW_READING = 1
        const val FOLLOW_COMPLETE = 2
        const val FOLLOW_ON_HOLD = 3
        const val FOLLOW_PLAN_TO_READ = 4
    }

}
