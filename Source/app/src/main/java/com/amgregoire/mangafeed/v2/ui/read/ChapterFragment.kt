package com.amgregoire.mangafeed.v2.ui.read

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.widget.ViewPager2
import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.MangaFeed.Companion.app
import com.amgregoire.mangafeed.Models.DbChapter
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.enums.ReaderSettings
import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.read.adapter.ImageAdapter
import com.amgregoire.mangafeed.v2.ui.read.adapter.NovelAdapter
import com.amgregoire.mangafeed.v2.widget.GestureViewPager
import kotlinx.android.synthetic.main.item_fragment_reader_chapter.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Created by Andy Gregoire on 3/21/2018.
 */

class ChapterFragment : BaseFragment(), GestureViewPager.UserGestureListener {

    private val readerViewModel: ReaderViewModel? by lazy {
        val parent = activity ?: return@lazy null
        ViewModelProviders.of(parent).get(ReaderViewModel::class.java)
    }

    private val readerSettings: ReaderSettings
        get() = readerViewModel?.getReaderSetting() ?: ReaderSettings.Max

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        self = inflater.inflate(R.layout.item_fragment_reader_chapter, null)
        return self
    }

    override fun onStart() {
        super.onStart()


        val chapter = readerViewModel?.getChapterByPosition(arguments!![POSITION_KEY] as Int) ?: return

        self.emptyStateReader.setButtonClickListener(View.OnClickListener {
            setup(chapter, refreshing = true)
        })

        activity?.let { parent ->
            readerViewModel?.chapterInfo?.observe(parent, Observer { info ->
                info ?: return@Observer
                if (info.dbChapter.url != chapter.url) return@Observer

                self.viewPagerReaderChapter.currentItem = info.currentPage
            })

            readerViewModel?.readerSettings?.observe(parent, Observer {
                self.viewPagerReaderChapter.offscreenPageLimit = readerSettings.pageCountCache
            })
        }

        setup(chapter)

        initViews()
    }

    fun initViews() {

        self.viewPagerReaderChapter.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
            override fun onPageScrollStateChanged(state: Int) = Unit
            override fun onPageSelected(position: Int) {
                val chapter = readerViewModel?.getChapterByPosition(arguments!![POSITION_KEY] as Int) ?: return
                readerViewModel?.updateChapterInfo(chapter, currentPage = position)
            }
        })

        self.emptyStateReader.userGestureListener = object : GestureViewPager.UserGestureListener {
            override fun onSingleTap() = this@ChapterFragment.onSingleTap()

            override fun onLeft() = Unit

            override fun onRight() = Unit
        }
    }

    override fun onSingleTap() {
        Logger.error("CurrentItem is ${self.viewPagerReaderChapter.currentItem}")
        readerViewModel?.toggleUiState()
    }

    override fun onLeft() {
        readerViewModel?.decrementChapter()
    }

    override fun onRight() {
        readerViewModel?.incrementChapter()
    }

    fun refreshAllPages() {
        val chapter = readerViewModel?.getChapterByPosition(arguments!![POSITION_KEY] as Int) ?: return
        setup(chapter, refreshing = true)
    }

    private fun setup(dbChapter: DbChapter, refreshing: Boolean = false) {
        val parent = activity ?: return

        self.emptyStateReader.showLoader()

        if (self.viewPagerReaderChapter.adapter == null || refreshing) {
            val currentItem = self.viewPagerReaderChapter.currentItem

            readerViewModel?.getChapterContents(dbChapter = dbChapter, chapterContent = { contents, chapter, isManga ->

                if (readerViewModel?.isCurrentChapter(chapter) == true) readerViewModel?.setUIStateShow()

                if (contents.isNullOrEmpty()) {
                    self.emptyStateReader.hideLoader(true)
                    CloudFlareService().getCookies {}
                    return@getChapterContents
                }

                try {
                    self.viewPagerReaderChapter.adapter =
                            if (isManga) ImageAdapter(this.childFragmentManager, this.lifecycle, contents)
                            else NovelAdapter(contents, this)

                    //                    self.viewPagerReaderChapter.setPageTransformer(MarginPageTransformer(125))
                    self.viewPagerReaderChapter.offscreenPageLimit = readerSettings.pageCountCache
                    self.emptyStateReader.hideImmediate()
                    self.viewPagerReaderChapter.currentItem = currentItem
                    self.viewPagerReaderChapter.requestDisallowInterceptTouchEvent(true)
                    setupViewpager()
                } catch (ex: Exception) {
                    Logger.error("Switching chapters too fast")
                }
            })
        } else {
            self.emptyStateReader.hide()
            return
        }

        ioScope.launch {
            delay(8000)
            if (self.viewPagerReaderChapter.adapter == null) {
                uiScope.launch { self.emptyStateReader.hideLoader(true) }
            }
        }
    }

    private fun setupViewpager() {
        if (self.viewPagerReaderChapter.childCount <= 0) return

        self.viewPagerReaderChapter.getChildAt(0).setOnTouchListener(object : View.OnTouchListener {
            private var _horizontalSwipeX: Float = 0f

            private fun checkSwipe(ev: MotionEvent): Int {
                when (ev.action) {
                    MotionEvent.ACTION_MOVE -> if (_horizontalSwipeX == 0f) _horizontalSwipeX = ev.x
                    MotionEvent.ACTION_UP -> if (_horizontalSwipeX > 0) {
                        return if (_horizontalSwipeX < ev.x) -1.also { _horizontalSwipeX = 0f }
                        else 1.also { _horizontalSwipeX = 0f }
                    }
                }
                return 0
            }

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    _horizontalSwipeX = event.x
                    return false
                }

                val currentItem = self.viewPagerReaderChapter.currentItem
                val totalItems = self.viewPagerReaderChapter.adapter?.itemCount ?: 0
                if (currentItem == 0 && checkSwipe(event) == -1) {
                    if (app.currentSourceType == MangaEnums.SourceType.MANGA) onLeft()
                } else if (totalItems - 1 == currentItem && checkSwipe(event) == 1) {
                    if (app.currentSourceType == MangaEnums.SourceType.MANGA) onRight()
                }
                return false
            }

        })
    }

    companion object {
        val TAG = ChapterFragment::class.java.simpleName
        val FOLLOWING_KEY = TAG + "FOLLOWING"
        val POSITION_KEY = TAG + "POSITION"

        fun newInstance(isFollowing: Boolean, position: Int): ChapterFragment {
            val lBundle = Bundle()
            lBundle.putBoolean(FOLLOWING_KEY, isFollowing)
            lBundle.putInt(POSITION_KEY, position)

            val lFragment = ChapterFragment()
            lFragment.arguments = lBundle

            return lFragment
        }
    }
}
