package com.amgregoire.mangafeed.v2.enums

import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.Common.WebSources.*
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase

/***
 * This enum is for the various sources.
 */
enum class Source(var source: SourceBase)
{
    ReadLight(ReadLight()), FunManga(FunManga()), MangaHere(MangaHere()), MangaEden(MangaEden()), Wuxia(Wuxia());

    val baseUrl: String
        get()
        = source.baseUrl

    override fun toString(): String
    {
        return source.sourceName
    }

    companion object
    {
        fun getPosition(sourceName: String?): Int
        {
            val array: Array<Source> = values()
            for (i in array.indices)
            {
                if (array[i].source.sourceName.equals(sourceName, ignoreCase = true)) return i
            }
            return 0
        }
    }

}