package com.amgregoire.mangafeed.v2.ui.catalog.fragment

import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amgregoire.mangafeed.Common.RecyclerViewSpaceDecoration
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.service.ScreenUtil
import com.amgregoire.mangafeed.v2.ui.base.FragmentNavMap
import com.amgregoire.mangafeed.v2.ui.catalog.adapter.MangaAdapter
import com.amgregoire.mangafeed.v2.ui.catalog.vm.RecentVM
import com.amgregoire.mangafeed.v2.ui.info.MangaInfoFragment
import kotlinx.android.synthetic.main.fragment_catalog.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RecentFragment : CatalogBase()
{
    private val recentVM by lazy { ViewModelProviders.of(this).get(RecentVM::class.java) }

    override fun onStart()
    {
        super.onStart()

        self.swipeManga.isEnabled = true
        self.swipeManga.setDistanceToTriggerSync(350)

        recentVM.state.observe(this, Observer { it.render() })

        self.swipeManga.setOnRefreshListener {
            self.swipeManga.isRefreshing = false
            recentVM.retrieveRecentList()
        }
    }

    override fun refresh() = recentVM.refresh()
    override fun forceRefresh() = recentVM.retrieveRecentList()

    override fun updateParentSettings()
    {
        super.updateParentSettings()

        val adapter = (self.rvManga.adapter as? MangaAdapter)
        catalogViewModel?.getUpdateMangaList(adapter?.data)?.let {
            adapter?.updateUnsortedData(it)
            adapter?.notifyDataSetChanged()
            Logger.error("Recent list was updated?")
        }
    }

    private fun RecentVM.State.render(): Job = when (this)
    {
        is RecentVM.State.Loading -> uiScope.launch { renderLoading() }
        is RecentVM.State.Success -> uiScope.launch { renderComplete(mangaList) }
        is RecentVM.State.Fail -> uiScope.launch { renderFailed(error) }
        else -> uiScope.launch { Logger.debug("Done") }
    }

    private fun renderComplete(mangaList: List<Manga>)
    {
        if (self.rvManga.adapter == null) context?.let {
            self.rvManga.addItemDecoration(RecyclerViewSpaceDecoration(ScreenUtil.dpToPx(it, 5)))
        }

        val list = ArrayList(mangaList)

        self.rvManga.itemAnimator?.changeDuration = 0
        self.rvManga.layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 3)
        self.rvManga.adapter = MangaAdapter(
                data = list,
                isLibrary = false,
                source = MangaFeed.app.currentSource,
                itemSelected = { manga ->
                    ioScope.launch {
                        catalogViewModel?.lastItem = manga
                        rvSavedState = self.rvManga.layoutManager?.onSaveInstanceState()

                        val parent = activity ?: return@launch
                        val fragment = MangaInfoFragment.newInstance(manga.link, manga.source, false)
                        (parent as FragmentNavMap).addFragment(fragment, MangaInfoFragment.TAG, R.anim.slide_up_from_bottom, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_down_from_top)
                    }
                }
        )

        // Restores the state of recyclerview if a saved state exists
        rvSavedState?.let { self.rvManga.layoutManager?.onRestoreInstanceState(it) }.also { rvSavedState = null }

        self.emptyStateRecent.hide()
    }

    private fun renderLoading()
    {
        self.emptyStateRecent.visibility = View.VISIBLE
        self.emptyStateRecent.showLoader()
    }

    private fun renderFailed(error: Error)
    {
        self.emptyStateRecent.hideLoader(true)
        self.emptyStateRecent.setSecondaryText(error.message!!)
    }

    companion object
    {
        fun newInstance() = RecentFragment()
    }
}