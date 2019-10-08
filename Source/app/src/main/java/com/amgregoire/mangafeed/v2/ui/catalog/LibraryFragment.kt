package com.amgregoire.mangafeed.v2.ui.catalog

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.Common.RecyclerViewSpaceDecoration
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Models.Manga
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.UI.Adapters.SearchRecyclerAdapter
import com.amgregoire.mangafeed.v2.service.ScreenUtil
import com.amgregoire.mangafeed.v2.ui.AppViewModelFactory
import com.amgregoire.mangafeed.v2.ui.BaseFragment
import com.amgregoire.mangafeed.v2.ui.catalog.vm.LibraryViewModel
import com.amgregoire.mangafeed.v2.ui.catalog.vm.RecentViewModel
import kotlinx.android.synthetic.main.fragment_catalog.view.*

class LibraryFragment : BaseFragment()
{
    private val libraryViewModel by lazy {
        ViewModelProviders.of(this, AppViewModelFactory(MangaFeed.app)).get(LibraryViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        self = inflater.inflate(R.layout.fragment_catalog, null)
        return self
    }

    override fun onStart()
    {
        super.onStart()

        self.swipeManga.isEnabled = false

        libraryViewModel.state.observe(this, Observer { state ->
            when (state)
            {
                is LibraryViewModel.State.Complete -> renderComplete(state.mangaList)
                is LibraryViewModel.State.Failed -> renderFailed(state.error, state.canRefresh)
                else -> renderLoading()
            }
        })
    }

    private fun renderComplete(mangas: List<Manga>)
    {
        if (self.rvManga.adapter == null) context?.let {
            self.rvManga.addItemDecoration(RecyclerViewSpaceDecoration(ScreenUtil.dpToPx(it, 10)))
        }

        self.rvManga.itemAnimator?.changeDuration = 0
        self.rvManga.layoutManager = GridLayoutManager(context, 3)
        self.rvManga.adapter = SearchRecyclerAdapter(mangas)
        self.emptyStateRecent.hide()
    }

    private fun renderLoading()
    {
        self.emptyStateRecent.showLoader()
    }

    private fun renderFailed(error: Error, canRefresh:Boolean)
    {
        if(canRefresh) self.emptyStateRecent.showButton()
        else self.emptyStateRecent.hideButton()

        self.emptyStateRecent.hideLoader(true)
        self.emptyStateRecent.setSecondaryText(error.message!!)
    }

    companion object
    {
        fun newInstance() = LibraryFragment()
    }
}