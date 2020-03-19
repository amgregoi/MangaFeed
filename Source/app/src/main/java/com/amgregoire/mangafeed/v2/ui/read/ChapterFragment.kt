package com.amgregoire.mangafeed.v2.ui.read

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.Models.Chapter
import com.amgregoire.mangafeed.Models.Manga
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.UI.Adapters.ImagePagerAdapter
import com.amgregoire.mangafeed.UI.Widgets.GestureViewPager
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import kotlinx.android.synthetic.main.item_fragment_reader_chapter.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Created by Andy Gregoire on 3/21/2018.
 */

class ChapterFragment : BaseFragment(), GestureViewPager.UserGestureListener
{

    private val readerViewModel: ReaderViewModel? by lazy {
        val parent = activity ?: return@lazy null
        ViewModelProviders.of(parent).get(ReaderViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        self = inflater.inflate(R.layout.item_fragment_reader_chapter, null)
        return self
    }

    override fun onStart()
    {
        super.onStart()

        val parent = activity ?: return

        val chapter = readerViewModel?.getChapterByPosition(arguments!![POSITION_KEY] as Int) ?: return

        self.emptyStateReader.setButtonClickListener(View.OnClickListener {
            setup(chapter, refreshing = true)
        })

        readerViewModel?.chapterInfo?.observe(parent, Observer { info ->
            info ?: return@Observer
            if (info.chapter.url != chapter.url) return@Observer

            self.viewPagerReaderChapter.currentItem = info.currentPage
        })

        setup(chapter)

        initViews()
    }

    fun initViews()
    {
        self.viewPagerReaderChapter.setUserGestureListener(this)
        self.viewPagerReaderChapter.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener
        {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
            override fun onPageScrollStateChanged(state: Int) = Unit
            override fun onPageSelected(position: Int)
            {
                val chapter = readerViewModel?.getChapterByPosition(arguments!![POSITION_KEY] as Int) ?: return
                readerViewModel?.updateChapterInfo(chapter, currentPage = position)
            }
        })
    }

    override fun onSingleTap()
    {
        readerViewModel?.toggleUiState()
    }

    override fun onLeft()
    {
        readerViewModel?.decrementChapter()
    }

    override fun onRight()
    {
        readerViewModel?.incrementChapter()
    }

    private fun setup(chapter: Chapter, refreshing: Boolean = false)
    {
        val parent = activity ?: return

        self.emptyStateReader.showLoader()

        if (self.viewPagerReaderChapter.adapter == null || refreshing)
        {
            Logger.error("Getting contents for: ${chapter.chapterTitle}")
            readerViewModel?.getChapterContents(chapter = chapter, chapterContent = { contents, chapter, isManga ->

                if (readerViewModel?.isCurrentChapter(chapter) == true) readerViewModel?.setUIStateShow()

                if (contents.isNullOrEmpty())
                {
                    self.emptyStateReader.hideLoader(true)
                    CloudFlareService().getCookies {}
                    return@getChapterContents
                }

                self.viewPagerReaderChapter.adapter =
                        if (isManga) ImagePagerAdapter(this, parent, contents)
                        else ImagePagerAdapter(this, parent, contents, this)

                self.viewPagerReaderChapter.pageMargin = 128
                self.viewPagerReaderChapter.offscreenPageLimit = 6
                self.emptyStateReader.hideImmediate()
            })
        }
        else
        {
            self.emptyStateReader.hide()
            return
        }

        ioScope.launch {
            delay(8000)
            if (self.viewPagerReaderChapter.adapter == null)
            {
                uiScope.launch { self.emptyStateReader.hideLoader(true) }
            }
        }
    }

    companion object
    {
        val TAG = ChapterFragment::class.java.simpleName
        val FOLLOWING_KEY = TAG + "FOLLOWING"
        val POSITION_KEY = TAG + "POSITION"
        val MANGA_KEY = TAG + "MANGA"

        fun newInstance(isFollowing: Boolean, position: Int, manga: Manga): androidx.fragment.app.Fragment
        {
            val lBundle = Bundle()
            lBundle.putBoolean(FOLLOWING_KEY, isFollowing)
            lBundle.putInt(POSITION_KEY, position)
            lBundle.putParcelable(MANGA_KEY, manga)

            val lFragment = ChapterFragment()
            lFragment.arguments = lBundle

            return lFragment
        }
    }
}
