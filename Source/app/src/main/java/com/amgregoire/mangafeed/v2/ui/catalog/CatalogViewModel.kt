package com.amgregoire.mangafeed.v2.ui.catalog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.enums.FilterType
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.repository.local.LocalCatalogRepository
import com.amgregoire.mangafeed.v2.usecase.local.GetRecentsUseCase
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CatalogViewModel(
        private val catalogRepository: LocalCatalogRepository = LocalCatalogRepository()
) : ViewModel()
{
    private val subscribers = CompositeDisposable()

    val recent = MutableLiveData<List<Manga>>()
    val library = MutableLiveData<List<Manga>>()
    val all = MutableLiveData<List<Manga>>()

    val lastItem = MutableLiveData<Manga>()
    val queryFilter = MutableLiveData<String>()

    val source = MutableLiveData<SourceBase>()

    var isLastItemComplete = true

    init
    {
        retrieveRecentList()
        retrieveLibrary()
        retrieveAll()
    }

    fun setSource(source: SourceBase)
    {
        this.source.value = source

        ioScope.launch {
            retrieveRecentList()
            retrieveLibrary()
            retrieveAll()
        }
    }

    fun setQuery(query: String) = ioScope.launch {
        val prevQuery = queryFilter.value ?: ""
        if (prevQuery.length > 1 && query.isEmpty()) return@launch

        uiScope.launch { queryFilter.value = query }
    }

    private fun retrieveAll() = ioScope.launch {
        catalogRepository.getCatalogList {
            all.value = it
        }
    }

    private fun retrieveLibrary() = ioScope.launch {
        catalogRepository.getLibrary(FilterType.NONE) {
            library.value = it
        }
    }

    fun retrieveRecentList() = ioScope.launch {
        GetRecentsUseCase().retrieveRecentList { result -> recent.value = result }
    }

    fun setLastItem(manga: Manga) = ioScope.launch {
        isLastItemComplete = false

        uiScope.launch { lastItem.value = manga }

        delay(750)
        isLastItemComplete = true
    }

    override fun onCleared()
    {
        super.onCleared()
        subscribers.clear()
    }
}