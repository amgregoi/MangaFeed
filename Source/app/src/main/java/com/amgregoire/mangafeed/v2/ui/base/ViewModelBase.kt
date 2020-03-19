package com.amgregoire.mangafeed.v2.ui.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class ViewModelBase : ViewModel()
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