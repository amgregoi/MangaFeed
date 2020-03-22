package com.amgregoire.mangafeed.v2.ui.catalog.vm

import androidx.lifecycle.MutableLiveData
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase
import com.amgregoire.mangafeed.Models.DbManga
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.repository.local.LocalMangaRepository
import com.amgregoire.mangafeed.v2.ui.base.ViewModelBase

class CatalogVM(
        private val localMangaRepository: LocalMangaRepository = LocalMangaRepository()
) : ViewModelBase()
{
    val source = MutableLiveData<SourceBase>()
    var lastItem: Manga? = null

    fun setSource(source: SourceBase)
    {
        this.source.value = source
    }

    fun getUpdateMangaList(mangaList: List<Manga>?):List<Manga>?
    {
        return localMangaRepository.getUpdateMangaList(mangaList)
    }
}