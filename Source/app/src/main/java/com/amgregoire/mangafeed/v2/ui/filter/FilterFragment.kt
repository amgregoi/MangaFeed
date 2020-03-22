package com.amgregoire.mangafeed.v2.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.amgregoire.mangafeed.Common.RecyclerViewSpaceDecoration
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.currentSource
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.NavigationType
import com.amgregoire.mangafeed.v2.ResourceFactory
import com.amgregoire.mangafeed.v2.extension.gone
import com.amgregoire.mangafeed.v2.extension.visible
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.service.KeyboardUtil
import com.amgregoire.mangafeed.v2.service.ScreenUtil
import com.amgregoire.mangafeed.v2.ui.base.FragmentNavMap
import com.amgregoire.mangafeed.v2.ui.catalog.adapter.MangaAdapter
import com.amgregoire.mangafeed.v2.ui.catalog.fragment.CatalogBase
import com.amgregoire.mangafeed.v2.ui.catalog.vm.AllVM
import com.amgregoire.mangafeed.v2.ui.info.MangaInfoFragment
import com.amgregoire.mangafeed.v2.ui.map.ToolbarMap
import com.amgregoire.mangafeed.v2.ui.map.ToolbarSearchViewMap
import kotlinx.android.synthetic.main.fragment_filter.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FilterFragment : CatalogBase()
{
    private val allVM by lazy { ViewModelProviders.of(this).get(AllVM::class.java) }
    private val filterVM by lazy {
        val parent = activity ?: return@lazy null
        ViewModelProviders.of(parent).get(FilterVM::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        self = inflater.inflate(R.layout.fragment_filter, null)
        return self
    }

    override fun onStart()
    {
        super.onStart()

        setupToolbar()

        allVM.retrieveAll()
        allVM.state.observe(this, Observer { it.render() })

        val parent = activity ?: return
        filterVM?.queryState?.observe(parent, Observer { it.render() })
    }

    private fun FilterVM.QueryState.render(): Job = when (this)
    {
        is FilterVM.QueryState.Query -> uiScope.launch {
            updateQuery(query)
            self.emptyState.hideImmediate()
            self.rvManga.visible()
        }
        is FilterVM.QueryState.Empty -> uiScope.launch {
            updateQuery("")
            renderLoading()
        }
    }

    override fun updateParentSettings()
    {
        (activity as? ToolbarSearchViewMap)?.showSearchView()
        (activity as? ToolbarMap)?.setNavigationIcon(ResourceFactory.getNavigationIcon(NavigationType.Close))

        catalogViewModel?.lastItem?.let {
            localMangaRepository.getManga(it.link, it.source)?.let { manga ->
                (self.rvManga.adapter as? MangaAdapter)?.updateItem(manga)
            }
        }
    }

    override fun refresh() = Unit

    private fun AllVM.State.render(): Job = when (this)
    {
        is AllVM.State.Loading -> uiScope.launch { renderLoading() }
        is AllVM.State.Success -> uiScope.launch { renderComplete(mangaList) }
        is AllVM.State.Fail -> uiScope.launch { renderFailed(error) }
    }

    private fun renderLoading()
    {
        self.emptyState.setPrimaryText(getString(R.string.filter_empty_primary_text))
        self.emptyState.setSecondaryText(getString(R.string.filter_empty_secondary_text))
        self.emptyState.show()
        self.rvManga.gone()
    }

    private fun renderComplete(mangaList: List<Manga>)
    {
        if (self.rvManga.adapter == null) context?.let {
            self.rvManga.addItemDecoration(RecyclerViewSpaceDecoration(ScreenUtil.dpToPx(it, 5)))
        }

        self.rvManga.layoutManager = GridLayoutManager(context, 3)
        self.rvManga.adapter = MangaAdapter(
                data = ArrayList(mangaList),
                source = currentSource,
                isLibrary = false,
                itemSelected = { manga ->
                    ioScope.launch {
                        catalogViewModel?.lastItem = manga
                        rvSavedState = self.rvManga.layoutManager?.onSaveInstanceState()

                        val parent = activity ?: return@launch
                        val fragment = MangaInfoFragment.newInstance(manga.link, manga.source, false)
                        (parent as FragmentNavMap).addFragment(fragment, this@FilterFragment, MangaInfoFragment.TAG, R.anim.slide_up_from_bottom, 0, 0, R.anim.slide_down_from_top)

                        KeyboardUtil.hide(parent)
                    }
                }
        )
    }

    private fun renderFailed(error: Error)
    {
        self.emptyState.setPrimaryText("Something is wrong with the filter")
        self.emptyState.setSecondaryText(error.message ?: "")
        self.emptyState.show()
    }

    private fun updateQuery(query: String)
    {
        (self.rvManga.adapter as? MangaAdapter)?.performTextFilter(query)
    }

    private fun setupToolbar()
    {

    }

    companion object
    {
        val TAG: String = FilterFragment::class.java.simpleName
        fun newInstance() = FilterFragment()
    }
}