package com.amgregoire.mangafeed.v2.ui.catalog.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amgregoire.mangafeed.Common.RecyclerViewSpaceDecoration
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.service.ScreenUtil
import com.amgregoire.mangafeed.v2.ui.base.FragmentNavMap
import com.amgregoire.mangafeed.v2.ui.catalog.adapter.MangaAdapter
import com.amgregoire.mangafeed.v2.ui.catalog.vm.LibraryVM
import com.amgregoire.mangafeed.v2.ui.info.MangaInfoFragment
import kotlinx.android.synthetic.main.fragment_catalog.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LibraryFragment : CatalogBase()
{
    private val libraryVM by lazy { ViewModelProviders.of(this).get(LibraryVM::class.java) }
    override fun onStart()
    {
        super.onStart()

        self.swipeManga.isEnabled = true

        libraryVM.state.observe(this, Observer { it.render() })

        self.swipeManga.setOnRefreshListener {
            self.swipeManga.isRefreshing = false
            libraryVM.retrieveLibrary()
        }
    }

    override fun refresh()
    {
        libraryVM.retrieveLibrary()
    }

    override fun updateParentSettings()
    {
        catalogViewModel?.lastItem?.let {
            localMangaRepository.getManga(it.link, it.source)?.let { manga ->
                (self.rvManga.adapter as? MangaAdapter)?.updateItem(manga)

                delayedAction {
                    val itemCount = (self.rvManga.adapter as? MangaAdapter)?.itemCount ?: 0
                    if (itemCount <= 0) renderFailed(Error(getString(R.string.recent_no_manga_message)))
                    else self.emptyStateRecent.hide()
                }
            }
        }
    }

    private fun LibraryVM.State.render(): Job = when (this)
    {
        is LibraryVM.State.Loading -> uiScope.launch { renderLoading() }
        is LibraryVM.State.Success -> uiScope.launch { renderComplete(mangaList) }
        is LibraryVM.State.Fail -> uiScope.launch { renderFailed(error) }
    }

    private fun renderComplete(mangas: List<Manga>)
    {
        if (self.rvManga.adapter == null) context?.let {
            self.rvManga.addItemDecoration(RecyclerViewSpaceDecoration(ScreenUtil.dpToPx(it, 5)))
        }

        val list = ArrayList(mangas).apply { sortBy { it.name } }

        self.rvManga.itemAnimator?.changeDuration = 0
        self.rvManga.layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 3)
        self.rvManga.adapter = MangaAdapter(
                data = list,
                isLibrary = true,
                source = MangaFeed.app.currentSource,
                itemSelected = { manga ->
                    ioScope.launch {
                        catalogViewModel?.lastItem = manga
                        rvSavedState = self.rvManga.layoutManager?.onSaveInstanceState()

                        val parent = activity ?: return@launch
                        val fragment = MangaInfoFragment.newInstance(manga.link, manga.source, false)
                        (parent as FragmentNavMap).addFragment(fragment, MangaInfoFragment.TAG, R.anim.slide_out_bottom, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_in_bottom)
                    }
                }
        )

        // Restores the state of recyclerview if a saved state exists
        rvSavedState?.let { self.rvManga.layoutManager?.onRestoreInstanceState(it) }.also { rvSavedState = null }

        if (mangas.isEmpty()) renderFailed(Error(getString(R.string.all_no_manga_message)))
        else self.emptyStateRecent.hide()
    }

    private fun renderFailed(error: Error)
    {
        self.emptyStateRecent.hideLoader(true)
        self.emptyStateRecent.setSecondaryText(error.message!!)
    }

    private fun renderLoading()
    {
        self.emptyStateRecent.showLoader()
    }

    companion object
    {
        fun newInstance() = LibraryFragment()
    }
}