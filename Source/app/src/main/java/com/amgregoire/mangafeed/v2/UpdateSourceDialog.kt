package com.amgregoire.mangafeed.v2

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Models.Manga
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.service.Logger
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_dialog_update_source.*
import kotlinx.coroutines.launch

class UpdateSourceDialog : DialogFragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_dialog_update_source, container)
    }

    val mangaList = arrayListOf<Manga>()
    val compositeDisposable = CompositeDisposable()
    override fun onStart()
    {
        super.onStart()
        val disp =
                MangaFeed.app.currentSource.updateLocalCatalogV2()?.subscribe({ mangas ->
                    mangaList.addAll(mangas)
                    uiScope.launch { tvCounter.setText("" + mangaList.size) }
                }, { Logger.error(it) }, {
                    uiScope.launch { label.setText("Complete") }
                })


    }
}