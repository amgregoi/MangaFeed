package com.amgregoire.mangafeed.v2.ui.read

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amgregoire.mangafeed.Models.DbChapter
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.service.ReadService
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.read.adapter.ImageAdapter
import com.amgregoire.mangafeed.v2.ui.read.adapter.ImagePagerAdapter
import com.amgregoire.mangafeed.v2.widget.GestureViewPager
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
            if (info.dbChapter.url != chapter.url) return@Observer

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

        self.emptyStateReader.userGestureListener = object : GestureViewPager.UserGestureListener
        {
            override fun onSingleTap() = this@ChapterFragment.onSingleTap()

            override fun onLeft() = Unit

            override fun onRight() = Unit
        }
    }

    override fun onSingleTap()
    {
        Logger.error("CurrentItem is ${self.viewPagerReaderChapter.currentItem}")
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

    fun refreshAllPages()
    {
        val chapter = readerViewModel?.getChapterByPosition(arguments!![POSITION_KEY] as Int) ?: return
        setup(chapter, refreshing = true)
    }

    private fun setup(dbChapter: DbChapter, refreshing: Boolean = false)
    {
        val parent = activity ?: return

        self.emptyStateReader.showLoader()

        if (self.viewPagerReaderChapter.adapter == null || refreshing)
        {
            val currentItem = self.viewPagerReaderChapter.currentItem

            readerViewModel?.getChapterContents(dbChapter = dbChapter, chapterContent = { contents, chapter, isManga ->

                if (readerViewModel?.isCurrentChapter(chapter) == true) readerViewModel?.setUIStateShow()

                if (contents.isNullOrEmpty())
                {
                    self.emptyStateReader.hideLoader(true)
                    CloudFlareService().getCookies {}
                    return@getChapterContents
                }

                self.viewPagerReaderChapter.adapter =
                        if (isManga) ImageAdapter(this, contents){data ->
                            val bitmaps = data.filterIsInstance(ImageAdapter.Item.Bitmap::class.java).map { it.value }
                            ChapterCache.apply {
                                this.chapter = chapter
                                this.chapterUrls = contents
                                this.chapterBitmap = bitmaps
//                                this.chapterList
                            }
                            chapter.bitmaps = bitmaps
                            self.viewPagerReaderChapter.adapter = ImageAdapter(this, data)
                            readerViewModel?.updateChapterInfo(chapter, chapter.mangaTitle, 0, data.size)
                            self.viewPagerReaderChapter.adapter?.notifyDataSetChanged()
                        }
                        else ImagePagerAdapter(this, parent, contents, this)

                self.viewPagerReaderChapter.pageMargin = 128
                self.viewPagerReaderChapter.offscreenPageLimit = 6
                self.emptyStateReader.hideImmediate()
                self.viewPagerReaderChapter.currentItem = currentItem
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

        fun newInstance(isFollowing: Boolean, position: Int): ChapterFragment
        {
            val lBundle = Bundle()
            lBundle.putBoolean(FOLLOWING_KEY, isFollowing)
            lBundle.putInt(POSITION_KEY, position)

            val lFragment = ChapterFragment()
            lFragment.arguments = lBundle

            return lFragment
        }
    }
}
