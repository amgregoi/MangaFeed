package com.amgregoire.mangafeed.v2.ui.catalog.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.currentSource
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.repository.local.LocalMangaRepository
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.catalog.vm.CatalogVM
import kotlinx.android.synthetic.main.fragment_catalog.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


abstract class CatalogBase : BaseFragment()
{
    protected val localMangaRepository = LocalMangaRepository()

    protected var rvSavedState: Parcelable? = null

    protected val catalogViewModel by lazy {
        val parent = activity ?: return@lazy null
        ViewModelProviders.of(parent).get(CatalogVM::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        self = inflater.inflate(R.layout.fragment_catalog, null)
        return self
    }

    abstract fun refresh()
    open fun forceRefresh(): Job = uiScope.launch { refresh() }

    override fun onStart()
    {
        super.onStart()

        val parent = activity ?: return
        catalogViewModel?.source?.observe(parent, Observer {
            if(it == currentSource) refresh()
            else
            {
                SharedPrefs.setSavedSource(it.sourceName)
                forceRefresh()
            }
        })
    }

    override fun onPause()
    {
        super.onPause()
        rvSavedState = self.rvManga.layoutManager?.onSaveInstanceState()
    }

}