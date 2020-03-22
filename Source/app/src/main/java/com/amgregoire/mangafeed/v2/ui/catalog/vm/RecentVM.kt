package com.amgregoire.mangafeed.v2.ui.catalog.vm

import androidx.lifecycle.MutableLiveData
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.ui.base.ViewModelBase
import com.amgregoire.mangafeed.v2.usecase.GetRecentUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class RecentVM(
        private val getRecentUseCase: GetRecentUseCase = GetRecentUseCase()
) : ViewModelBase()
{

    private var lastUpdatedAt: Date? = null

    val state = MutableLiveData<State>()

    sealed class State
    {
        object Loading : State()
        data class Success(val mangaList: List<Manga>) : State()
        data class Fail(val error: Error) : State()
        object Complete : State()
    }

    fun refresh()
    {
        val lastUpdate = lastUpdatedAt ?: kotlin.run {
            retrieveRecentList()
            return
        }

        val updateBy = Date(lastUpdate.time + (15 * 60 * 60 * 1000L))
        if (updateBy.before(Date())) retrieveRecentList()
    }

    fun retrieveRecentList(): Job = run {
        state.value = State.Loading
        getRecentUseCase.retrieveRecentList { result ->
            state.value =
                    if (result.isEmpty()) State.Fail(java.lang.Error(context.getString(R.string.recent_no_manga_message)))
                    else
                    {
                        lastUpdatedAt = Date()
                        State.Success(result)
                    }

            ioScope.launch {
                delay(1000)
                uiScope.launch { state.value = State.Complete }
            }
        }
    }
}