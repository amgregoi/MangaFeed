package com.amgregoire.mangafeed.v2.ui.base

import android.content.Context
import androidx.lifecycle.ViewModel
import com.amgregoire.mangafeed.MangaFeed
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class ViewModelBase(
        protected val context: Context = MangaFeed.app
) : ViewModel()
{
    val composite = CompositeDisposable()

    override fun onCleared()
    {
        super.onCleared()
        composite.clear()
    }
}

operator fun CompositeDisposable.plusAssign(disposable: Disposable)
{
    this.add(disposable)
}