package com.amgregoire.mangafeed.v2.service

object ImageUrlService
{
    // Other formatting styles will be accounted for here.
    fun format(url: String): String
    {
        var result =
                if (url.startsWith("//")) "http://${url.substring(2)}"
                else url

        return result
    }
}