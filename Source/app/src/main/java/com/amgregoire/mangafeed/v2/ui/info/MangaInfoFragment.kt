package com.amgregoire.mangafeed.v2.ui.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.NavigationType
import com.amgregoire.mangafeed.v2.ResourceFactory
import com.amgregoire.mangafeed.v2.enums.FollowType
import com.amgregoire.mangafeed.v2.repository.local.LocalMangaRepository
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.base.FragmentNavMap
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
    private val localMangaRepository = LocalMangaRepository()

    private val readerViewModel: ReaderViewModel? by lazy {
        val parent = activity ?: return@lazy null
        ViewModelProviders.of(parent).get(ReaderViewModel::class.java)
    }

    private val mangaInfoViewModel by lazy {
        val manga = localMangaRepository.getManga(arguments!!.getString(MANGA_LINK_KEY), arguments!!.getString(MANGA_SOURCE_KEY)) ?: return@lazy null
        ViewModelProviders.of(this, MangaInfoViewModelFactory(MangaFeed.app, manga!!)).get(MangaInfoViewModel::class.java)
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

        mangaInfoViewModel?.state?.observe(this, Observer { state ->
            when (state)
            {
                is MangaInfoViewModel.State.Complete -> uiScope.launch { renderComplete() }
                is MangaInfoViewModel.State.Loading -> uiScope.launch { renderLoading() }
                is MangaInfoViewModel.State.Failed -> uiScope.launch { renderFailed() }
            }
        })

        mangaInfoViewModel?.mangaInfo?.observe(this, Observer { mangaInfo ->
            mangaInfo ?: return@Observer

            if (self.rvMangaInfo.adapter != null)
            {
                (self.rvMangaInfo.adapter as MangaInfoAdapter).updateInfo(mangaInfo.manga, mangaInfo.dbChapters)
                return@Observer
            }

            mangaInfoViewModel?.setFollowStatus(mangaInfo.manga.followType)

            self.rvMangaInfo.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            self.rvMangaInfo.adapter = MangaInfoAdapter(
                    manga = mangaInfo.manga,
                    source = MangaFeed.app.currentSource,
                    dbChapters = mangaInfo.dbChapters,
                    chapterSelected = { manga, chapters, chapter ->
                        val parent = activity ?: return@MangaInfoAdapter
                        readerViewModel?.updateReaderInfo(manga, chapters, chapter, true)
                        (parent as FragmentNavMap).addFragmentParent(ReaderFragment.newInstance(), this, ReaderFragment.TAG)
                    }
            )
        })

        mangaInfoViewModel?.mangaInfoBottomNav?.observe(this, Observer { bottomNavInfo ->
            bottomNavInfo ?: return@Observer

            val item = self.bottomNavMangaInfo.menu.findItem(R.id.menuMangaInfoBottomContinueReading)
            item.title = getString(bottomNavInfo.startText)

            val followItem = self.bottomNavMangaInfo.menu.findItem(R.id.menuMangaInfoBottomNavFollow)
            followItem.setIcon(bottomNavInfo.followIcon)
            followItem.title = getString(bottomNavInfo.followText)
        })

        self.swipeRefreshMangaInfo.setOnRefreshListener {
            self.swipeRefreshMangaInfo.isRefreshing = false
            mangaInfoViewModel?.refresh()
        }

        self.emptyStateMangaInfo.setButtonClickListener(View.OnClickListener {
            self.swipeRefreshMangaInfo.isRefreshing = false
            mangaInfoViewModel?.refresh()
        })
    }

    override fun updateParentSettings()
    {
        val parent = activity ?: return
        mangaInfoViewModel?.manga?.name?.let {  (parent as ToolbarMap).setTitle(it)}
        (parent as ToolbarMap).setNavigationIcon(ResourceFactory.getNavigationIcon(NavigationType.Close))
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
                            R.id.menuFollowStatusReading -> mangaInfoViewModel?.setFollowStatus(FollowType.Reading)
                            R.id.menuFollowStatusCompleted -> mangaInfoViewModel?.setFollowStatus(FollowType.Completed)
                            R.id.menuFollowStatusOnHold -> mangaInfoViewModel?.setFollowStatus(FollowType.On_Hold)
                            R.id.menuFollowStatusPlanToRead -> mangaInfoViewModel?.setFollowStatus(FollowType.Plan_to_Read)
                            else -> mangaInfoViewModel?.setFollowStatus(FollowType.Unfollow)
                        }
                        true
                    }

                    popMenu.show()

                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menuMangaInfoBottomContinueReading ->
                {
                    // Start Reading from latest chapter
                    val parent = activity ?: return@setOnNavigationItemSelectedListener true

                    val adapter = (self.rvMangaInfo.adapter as? MangaInfoAdapter) ?: return@setOnNavigationItemSelectedListener true
                    val manga = adapter.manga
                    val chapters = adapter.dbChapters
                    val chapter = adapter.dbChapters.firstOrNull { chapter -> chapter.url == manga.recentChapter } ?: chapters[chapters.size - 1]

                    readerViewModel?.updateReaderInfo(manga, chapters, chapter, true)

                    (parent as FragmentNavMap).addFragmentParent(ReaderFragment.newInstance(), this, ReaderFragment.TAG)
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }
    }

    companion object
    {
        val TAG = MangaInfoFragment::class.java.simpleName
        val MANGA_LINK_KEY = TAG + "LINK"
        val MANGA_SOURCE_KEY = TAG + "SOURCE"
        val OFFLINE_KEY = TAG + "OFFLINE"

        fun newInstance(link: String, source: String, offline: Boolean): androidx.fragment.app.Fragment
        {
            val lBundle = Bundle()
            lBundle.putString(MANGA_LINK_KEY, link)
            lBundle.putString(MANGA_SOURCE_KEY, source)
            lBundle.putBoolean(OFFLINE_KEY, offline)

            val lFragment = MangaInfoFragment()
            lFragment.arguments = lBundle

            return lFragment
        }
    }
}
