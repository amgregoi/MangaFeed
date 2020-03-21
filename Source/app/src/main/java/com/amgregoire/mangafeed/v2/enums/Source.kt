package com.amgregoire.mangafeed.v2.enums

import com.amgregoire.mangafeed.Common.WebSources.*
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase

/***
 * This enum is for the various sources.
 */
enum class Source(var source: SourceBase, val sourceId:String)
{
    ReadLight(ReadLight(), "9b381778-caa4-4263-8e20-007c89066465"),
    FunManga(FunManga(),"d4758a05-ac1a-486c-92c1-537b987fdcea"),
    MangaHere(MangaHere(),"33f3ea26-aeb0-4bab-ac0c-ccfc2333c13b"),
    MangaEden(MangaEden(),"0ed4656e-4377-4df8-bada-81823bbed935"),
    Wuxia(Wuxia(),"97f4fd6e-5348-4127-8bdb-4dbff0f6275f");

    val baseUrl: String
        get() = source.baseUrl

    override fun toString(): String
    {
        return source.sourceName
    }

    companion object
    {
        fun getSourceByName(sourceName:String):Source
        {
            val array: Array<Source> = values()
            for (i in array.indices)
            {
                if (array[i].source.sourceName.equals(sourceName, ignoreCase = true)) return array[i]
            }
            return FunManga
        }

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