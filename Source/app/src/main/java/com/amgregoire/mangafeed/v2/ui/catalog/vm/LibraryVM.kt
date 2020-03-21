package com.amgregoire.mangafeed.v2.ui.catalog.vm

import androidx.lifecycle.MutableLiveData
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.enums.FilterType
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.repository.local.LocalMangaRepository
import com.amgregoire.mangafeed.v2.ui.base.ViewModelBase

class LibraryVM(
        private val catalogRepository: LocalMangaRepository = LocalMangaRepository()
) : ViewModelBase()
{
    val state = MutableLiveData<State>()

    sealed class State
    {
        object Loading : State()
        data class Success(val mangaList: List<Manga>) : State()
        data class Fail(val error: Error) : State()
    }

    fun retrieveLibrary() = run {
        state.value = State.Loading
        catalogRepository.getLibrary(FilterType.NONE) { result ->
            state.value =
                    if (result.isEmpty()) State.Fail(java.lang.Error(context.getString(R.string.library_no_manga_message)))
                    else State.Success(result)

        }
    }
}