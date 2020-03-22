package com.amgregoire.mangafeed.v2.ui.filter

import androidx.lifecycle.MutableLiveData
import com.amgregoire.mangafeed.v2.ui.base.ViewModelBase

class FilterVM : ViewModelBase()
{
    val queryState = MutableLiveData<QueryState>()

    sealed class QueryState
    {
        class Query(val query: String) : QueryState()
        object Empty : QueryState()
    }

    fun updateQuery(newText: String?)
    {
        if (newText.isNullOrEmpty())
        {
            queryState.value = QueryState.Empty
            return
        }

        queryState.value = QueryState.Query(newText)
    }
}