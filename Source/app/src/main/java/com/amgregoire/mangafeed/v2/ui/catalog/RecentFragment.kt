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
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.service.ScreenUtil
import com.amgregoire.mangafeed.v2.ui.BaseFragment
import com.amgregoire.mangafeed.v2.ui.FragmentNavMap
import com.amgregoire.mangafeed.v2.ui.catalog.vm.CatalogViewModel
import com.amgregoire.mangafeed.v2.ui.info.MangaInfoFragment
import kotlinx.android.synthetic.main.fragment_catalog.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class RecentFragment : BaseFragment()
{
    private val catalogViewModel by lazy {
        ViewModelProviders.of(this).get(CatalogViewModel::class.java)
    }

    private var state by Delegates.observable<State>(State.Loading) { _, _, state ->
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
        self.swipeManga.isEnabled = true
        self.swipeManga.setDistanceToTriggerSync(1200)

        catalogViewModel.recent.observe(this, Observer { mangaList ->
            mangaList ?: return@Observer

            state =
                    if (mangaList.isEmpty()) State.Failed(Error(getString(R.string.all_no_manga_message)))
                    else State.Complete(mangaList)
        })

        self.swipeManga.setOnRefreshListener {
            self.swipeManga.isRefreshing = false
            state = State.Loading
        }
    }

    /**************************************************************************************
     *
     * Implementation
     *
     *************************************************************************************/
    private fun renderComplete(mangas: List<Manga>)
    {
        if (self.rvManga.adapter == null) context?.let {
            self.rvManga.addItemDecoration(RecyclerViewSpaceDecoration(ScreenUtil.dpToPx(it, 10)))
        }

        self.rvManga.itemAnimator?.changeDuration = 0
        self.rvManga.layoutManager = GridLayoutManager(context, 3)
        self.rvManga.adapter = MangaAdapter(
                data = ArrayList(mangas),
                source = MangaFeed.app.currentSource,
                itemSelected = { manga ->
                    val parent = activity ?: return@MangaAdapter
                    val fragment = MangaInfoFragment.newInstance(manga._id, false)
                    (parent as FragmentNavMap).replaceFragment(fragment, MangaInfoFragment.TAG)
                }
        )
        self.emptyStateRecent.hide()
    }

    private fun renderLoading()
    {
        self.emptyStateRecent.showLoader()
        catalogViewModel.retrieveRecentList()
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
        data class Complete(val mangaList: List<Manga>) : State()
        data class Failed(val error: Error) : State()
    }

    private fun State.render(): Job = when (this)
    {
        is State.Loading -> uiScope.launch { renderLoading() }
        is State.Complete -> uiScope.launch { renderComplete(mangaList) }
        is State.Failed -> uiScope.launch { renderFailed(error) }
    }

    companion object
    {
        fun newInstance() = RecentFragment()
    }
}