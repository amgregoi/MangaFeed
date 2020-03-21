package com.amgregoire.mangafeed.v2.ui.catalog.vm

import androidx.lifecycle.MutableLiveData
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.ui.base.ViewModelBase

class CatalogVM : ViewModelBase()
{
    val source = MutableLiveData<SourceBase>()
    var lastItem: Manga? = null

    fun setSource(source: SourceBase)
    {
        this.source.value = source
    }
}