package com.amgregoire.mangafeed.v2.ui.catalog.vm

import androidx.lifecycle.MutableLiveData
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.network.Result
import com.amgregoire.mangafeed.v2.repository.local.LocalMangaRepository
import com.amgregoire.mangafeed.v2.ui.base.ViewModelBase
import com.amgregoire.mangafeed.v2.usecase.GetCatalogUseCase

class AllVM(
        private val getCatalogUseCase: GetCatalogUseCase = GetCatalogUseCase()
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

        getCatalogUseCase.getCatalog {
            when (it)
            {
                is Result.Success ->
                {
                    state.value =
                            if (it.value.isEmpty()) State.Fail(java.lang.Error(context.getString(R.string.recent_no_manga_message)))
                            else State.Success(it.value)
                }
                is Result.Failure ->
                {
                    state.value = State.Fail(Error(it.throwable.message))
                }
            }
        }
    }
}