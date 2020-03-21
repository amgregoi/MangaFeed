package com.amgregoire.mangafeed.v2.ui.catalog.vm

import androidx.lifecycle.MutableLiveData
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.repository.local.LocalMangaRepository
import com.amgregoire.mangafeed.v2.ui.base.ViewModelBase
import com.amgregoire.mangafeed.v2.usecase.GetRecentUseCase

class AllVM(
        private val catalogRepository: LocalMangaRepository = LocalMangaRepository(),
        private val getRecentsUseCase: GetRecentUseCase = GetRecentUseCase()
) : ViewModelBase()
{
    val state = MutableLiveData<State>()

    sealed class State
    {
        object Loading : State()
        data class Success(val mangaList: List<Manga>) : State()
        data class Fail(val error: Error) : State()
    }

    fun retrieveAll() = run {
        state.value = State.Loading
        catalogRepository.getCatalogList { result ->
            state.value =
                    if (result.isEmpty()) State.Fail(java.lang.Error(context.getString(R.string.recent_no_manga_message)))
                    else State.Success(result)

        }
    }
}