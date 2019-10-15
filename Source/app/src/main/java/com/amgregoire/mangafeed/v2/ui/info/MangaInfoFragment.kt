package com.amgregoire.mangafeed.v2.ui.info

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.ResourceFactory
import com.amgregoire.mangafeed.v2.ui.BaseFragment
import com.amgregoire.mangafeed.v2.ui.FragmentNavMap
import com.amgregoire.mangafeed.v2.ui.catalog.enum.FollowType
import com.amgregoire.mangafeed.v2.ui.map.ToolbarMap
import com.amgregoire.mangafeed.v2.ui.read.ReaderFragment
import com.amgregoire.mangafeed.v2.ui.read.ReaderViewModel
import kotlinx.android.synthetic.main.fragment_manga_info2.view.*
import kotlinx.coroutines.launch


/**
 * Created by Andy Gregoire on 3/12/2018.
 */

class MangaInfoFragment : BaseFragment()
{
    private val readerViewModel: ReaderViewModel? by lazy {
        val parent = activity ?: return@lazy null
        ViewModelProviders.of(parent).get(ReaderViewModel::class.java)
    }

    private val mangaInfoViewModel by lazy {
        val manga = MangaDB.getInstance().getManga(arguments!!.getInt(MANGA_KEY))
        ViewModelProviders.of(this, MangaInfoViewModelFactory(MangaFeed.app, manga)).get(MangaInfoViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        self = inflater.inflate(R.layout.fragment_manga_info2, null)
        return self
    }

    override fun onStart()
    {
        super.onStart()

        updateParentSettings()
        setupBottomNav()

        mangaInfoViewModel.state.observe(this, Observer { state ->
            when (state)
            {
                is MangaInfoViewModel.State.Complete -> uiScope.launch { renderComplete() }
                is MangaInfoViewModel.State.Loading -> uiScope.launch { renderLoading() }
                is MangaInfoViewModel.State.Failed -> uiScope.launch { renderFailed() }
            }
        })

        mangaInfoViewModel.mangaInfo.observe(this, Observer { mangaInfo ->
            mangaInfo ?: return@Observer

            if (self.rvMangaInfo.adapter != null)
            {
                (self.rvMangaInfo.adapter as MangaInfoAdapter).updateInfo(mangaInfo.manga, mangaInfo.chapters)
                return@Observer
            }

            self.rvMangaInfo.layoutManager = LinearLayoutManager(context)
            self.rvMangaInfo.adapter = MangaInfoAdapter(
                    manga = mangaInfo.manga,
                    source = MangaFeed.app.currentSource,
                    chapters = mangaInfo.chapters,
                    chapterSelected = { manga, chapters, chapter ->
                        val parent = activity ?: return@MangaInfoAdapter

                        readerViewModel?.updateReaderInfo(manga, chapters, chapter)
                        (parent as FragmentNavMap).replaceFragmentParent(ReaderFragment.newInstance(), ReaderFragment.TAG)
                    }
            )
        })

        mangaInfoViewModel.mangaInfoBottomNav.observe(this, Observer { bottomNavInfo ->
            bottomNavInfo ?: return@Observer

            val item = self.bottomNavMangaInfo.menu.findItem(R.id.menuMangaInfoBottomContinueReading)
            item.title = getString(bottomNavInfo.startText)

            val followItem = self.bottomNavMangaInfo.menu.findItem(R.id.menuMangaInfoBottomNavFollow)
            followItem.setIcon(bottomNavInfo.followIcon)
            followItem.title = getString(bottomNavInfo.followText)
        })

        self.swipeRefreshMangaInfo.setOnRefreshListener {
            self.swipeRefreshMangaInfo.isRefreshing = false
            mangaInfoViewModel.refresh()
        }
    }

    override fun updateParentSettings()
    {
        val parent = activity ?: return
        (parent as ToolbarMap).setTitle(mangaInfoViewModel.baseManga.title)
        (parent as ToolbarMap).setNavigationIcon(ResourceFactory.getNavigationIcon()) // TODO :: create new theme attr for navigation icon
        (parent as ToolbarMap).setOptionsMenu(R.menu.menu_empty)
    }

    private fun renderLoading()
    {
        self.emptyStateMangaInfo.showLoader()

    }

    private fun renderComplete()
    {
        self.emptyStateMangaInfo.hide()

    }

    private fun renderFailed()
    {
        self.emptyStateMangaInfo.hideLoader(true)

    }

    /***
     * This function sets up the bottom navigation view interactions.
     *
     */
    private fun setupBottomNav()
    {
        self.bottomNavMangaInfo.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId)
            {
                R.id.menuMangaInfoBottomNavFollow ->
                {
                    val context = context ?: return@setOnNavigationItemSelectedListener false
                    val popMenu = PopupMenu(context, self.bottomNavMangaInfo)

                    popMenu.menuInflater.inflate(R.menu.menu_follow_status, popMenu.menu)

                    popMenu.setOnMenuItemClickListener { popupItem ->
                        when (popupItem.itemId)
                        {
                            R.id.menuFollowStatusReading -> mangaInfoViewModel.setFollowStatus(FollowType.Reading)
                            R.id.menuFollowStatusCompleted -> mangaInfoViewModel.setFollowStatus(FollowType.Completed)
                            R.id.menuFollowStatusOnHold -> mangaInfoViewModel.setFollowStatus(FollowType.On_Hold)
                            R.id.menuFollowStatusPlanToRead -> mangaInfoViewModel.setFollowStatus(FollowType.Plan_to_Read)
                            else -> mangaInfoViewModel.setFollowStatus(FollowType.Unfollow)
                        }
                        true
                    }

                    popMenu.show()

                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menuMangaInfoBottomContinueReading ->
                {
                    // Start Reading from latest chapter
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }
    }

    companion object
    {
        val TAG = MangaInfoFragment::class.java.simpleName
        val MANGA_KEY = TAG + "MANGA"
        val OFFLINE_KEY = TAG + "OFFLINE"

        fun newInstance(mangaId: Int, offline: Boolean): Fragment
        {
            val lBundle = Bundle()
            lBundle.putInt(MANGA_KEY, mangaId)
            lBundle.putBoolean(OFFLINE_KEY, offline)

            val lFragment = MangaInfoFragment()
            lFragment.arguments = lBundle

            return lFragment
        }
    }
}
