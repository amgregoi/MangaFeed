package com.amgregoire.mangafeed.v2.ui.read

import android.graphics.Bitmap
import com.amgregoire.mangafeed.Models.DbChapter

object ChapterCache
{
    var chapter:DbChapter? = null
    var chapterList:List<DbChapter>? = null
    var chapterUrls:List<String>? = null
    var chapterBitmap:List<Bitmap>? = null
}