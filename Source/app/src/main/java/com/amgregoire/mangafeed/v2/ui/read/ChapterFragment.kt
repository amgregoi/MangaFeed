package com.amgregoire.mangafeed.v2.ui.read

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.Models.Manga
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.UI.Adapters.ImagePagerAdapter
import com.amgregoire.mangafeed.UI.Widgets.GestureViewPager
import com.amgregoire.mangafeed.v2.ui.BaseFragment
import com.amgregoire.mangafeed.v2.ui.Logger
import kotlinx.android.synthetic.main.item_fragment_reader_chapter.view.*

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

        readerViewModel?.chapterInfo?.observe(parent, Observer { info ->
            info ?: return@Observer
            self.viewPagerReaderChapter.currentItem = info.currentPage
        })

        val chapter = readerViewModel?.getChapterByPosition(arguments!![POSITION_KEY] as Int) ?: return

        if(self.viewPagerReaderChapter.adapter == null)
        {
            Logger.error("Getting contents for: ${chapter.chapterTitle}")
            readerViewModel?.getChapterContents(chapter = chapter, chapterContent = { contents, chapter, isManga ->

                self.viewPagerReaderChapter.adapter =
                        if (isManga) ImagePagerAdapter(this, parent, contents)
                        else ImagePagerAdapter(this, parent, contents, this)

                self.viewPagerReaderChapter.pageMargin = 128
                self.viewPagerReaderChapter.offscreenPageLimit = 6
            })
        }

        initViews()
    }

    fun initViews()
    {
        self.viewPagerReaderChapter.setUserGestureListener(this)
        self.viewPagerReaderChapter.addOnPageChangeListener(object : ViewPager.OnPageChangeListener
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

    companion object
    {
        val TAG = ChapterFragment::class.java.simpleName
        val FOLLOWING_KEY = TAG + "FOLLOWING"
        val POSITION_KEY = TAG + "POSITION"
        val MANGA_KEY = TAG + "MANGA"

        fun newInstance(isFollowing: Boolean, position: Int, manga: Manga): Fragment
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
