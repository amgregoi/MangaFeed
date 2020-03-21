package com.amgregoire.mangafeed.v2.ui.catalog.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amgregoire.mangafeed.Common.RecyclerViewSpaceDecoration
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.repository.local.LocalMangaRepository
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.service.ScreenUtil
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.base.FragmentNavMap
import com.amgregoire.mangafeed.v2.ui.catalog.CatalogViewModel
import com.amgregoire.mangafeed.v2.ui.catalog.adapter.MangaAdapter
import com.amgregoire.mangafeed.v2.ui.info.MangaInfoFragment
import kotlinx.android.synthetic.main.fragment_catalog.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


abstract class CatalogBase : BaseFragment()
{
    private val localMangaRepository = LocalMangaRepository()

    private var rvSavedState: Parcelable? = null
    protected val catalogViewModel by lazy {
        val parent = activity ?: return@lazy null
        ViewModelProviders.of(parent).get(CatalogViewModel::class.java)
    }

    protected var state by Delegates.observable<State>(State.Loading) { _, _, state ->
        state.render()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        self = inflater.inflate(R.layout.fragment_catalog, null)
        return self
    }

    override fun onStart()
    {
        super.onStart()

        state = State.Loading

        val parent = activity ?: return
        catalogViewModel?.queryFilter?.observe(parent, Observer { query ->
            query ?: return@Observer
            (self.rvManga.adapter as? MangaAdapter)?.performTextFilter(query)
        })

        catalogViewModel?.source?.observe(parent, Observer {
            state = State.Loading
        })
    }

    override fun updateParentSettings()
    {
        super.updateParentSettings()

        catalogViewModel?.lastItem?.let {
            localMangaRepository.getManga(it.link, it.source)?.let {
                Logger.error("#########!!!!!!!!!!!!!!!!!####### Updating item")
                (self.rvManga.adapter as? MangaAdapter)?.updateItem(it)
            }
        }
    }

    /**************************************************************************************
     *
     * Implementation
     *
     *************************************************************************************/
    private fun renderComplete(mangases: List<Manga>)
    {
        if (self.rvManga.adapter == null) context?.let {
            self.rvManga.addItemDecoration(RecyclerViewSpaceDecoration(ScreenUtil.dpToPx(it, 5)))
        }

        val list = ArrayList(mangases)
        if (this !is RecentFragment) list.sortBy { it.name }

        self.rvManga.itemAnimator?.changeDuration = 0
        self.rvManga.layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 3)
        self.rvManga.adapter = MangaAdapter(
                data = list,
                isLibrary = (this is LibraryFragment),
                source = MangaFeed.app.currentSource,
                itemSelected = { manga ->
                    ioScope.launch {
                        if (catalogViewModel?.isLastItemComplete == true)
                        {
                            catalogViewModel?.lastItem = manga
                            rvSavedState = self.rvManga.layoutManager?.onSaveInstanceState()

                            val parent = activity ?: return@launch
                            val fragment = MangaInfoFragment.newInstance(manga.link, manga.source, false)
                            (parent as FragmentNavMap).addFragment(fragment, MangaInfoFragment.TAG, R.anim.slide_out_bottom, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_in_bottom)
                        }
                    }
                }
        )

        // Restores the state of recyclerview if a saved state exists
        rvSavedState?.let { self.rvManga.layoutManager?.onRestoreInstanceState(it) }.also { rvSavedState = null }


        self.emptyStateRecent.hide()
    }

    override fun onPause()
    {
        super.onPause()
        rvSavedState = self.rvManga.layoutManager?.onSaveInstanceState()
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

    /**************************************************************************************
     *
     *
     *
     *************************************************************************************/
    sealed class State
    {
        object Loading : State()
        data class Complete(val dbMangaList: List<Manga>) : State()
        data class Failed(val error: Error) : State()
    }

    private fun State.render(): Job = when (this)
    {
        is State.Loading -> uiScope.launch { renderLoading() }
        is State.Complete -> uiScope.launch { renderComplete(dbMangaList) }
        is State.Failed -> uiScope.launch { renderFailed(error) }
    }
}