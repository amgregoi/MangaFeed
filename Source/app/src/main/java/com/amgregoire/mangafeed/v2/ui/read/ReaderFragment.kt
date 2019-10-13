package com.amgregoire.mangafeed.v2.ui.read

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.UI.Services.ToolbarTimerService
import com.amgregoire.mangafeed.Utils.MangaLogger
import com.amgregoire.mangafeed.v2.service.ScreenUtil
import com.amgregoire.mangafeed.v2.ui.BaseFragment
import com.amgregoire.mangafeed.v2.ui.FragmentNavMap
import kotlinx.android.synthetic.main.fragment_reader2.*
import kotlinx.android.synthetic.main.fragment_reader2.view.*
import kotlinx.android.synthetic.main.widget_toolbar_reader.view.*

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

class ReaderFragment : BaseFragment(), ToolbarTimerService.ReaderTimerListener
{
    private val readerViewModel: ReaderViewModel? by lazy {
        val parent = activity ?: return@lazy null
        ViewModelProviders.of(parent).get(ReaderViewModel::class.java)
    }

    private var mToolBarService: ToolbarTimerService? = null
    private var mConnection: ServiceConnection? = null

    /***
     * This function retrieves the height of the android onscreen bottom navigation bar.
     *
     * @return
     */

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        self = inflater.inflate(R.layout.fragment_reader2, null)
        return self
    }

    override fun onStart()
    {
        super.onStart()

        readerViewModel?.let { readerVm ->
            val parent = activity ?: return
            readerVm.readerInfo.observe(parent, Observer { info ->
                info ?: return@Observer
                activity ?: return@Observer

                initViews()
                setupToolbar(info.manga.title)
                self.vpReader.offscreenPageLimit = 1
                if (self.vpReader.adapter == null) self.vpReader.adapter = ChapterPagerAdapter(childFragmentManager, info.chapters, info.manga.isFollowing, info.manga)
                self.vpReader.currentItem = readerVm.getCurrentPosition()

                if (MangaFeed.app.currentSourceType == MangaEnums.SourceType.NOVEL) self.vpReader.setPagingEnabled(true)
            })

            readerVm.chapterInfo.observe(parent, Observer { info ->
                info ?: return@Observer
                activity ?: return@Observer

                self.tvChapterTitle.text = info.title
                self.tvCurrentPage.text = info.currentPage.inc().toString()
                self.tvPageCount.text = info.totalPages.toString()
            })

            readerVm.uiState.observe(parent, Observer { state ->
                state ?: return@Observer

                if (state is ReaderUIState.SHOW) showToolbar()
                else hideToolbar()

                mToolBarService?.startTimer()
            })
        }

        setupListeners()
    }

    private fun setupListeners()
    {

        fabNextPage.setOnClickListener {
            val chapter = readerViewModel?.getChapterByPosition(self.vpReader.currentItem) ?: return@setOnClickListener
            readerViewModel?.incrementPage(chapter)
            mToolBarService?.restartTimer()
        }

        fabPrevPage.setOnClickListener {
            val chapter = readerViewModel?.getChapterByPosition(self.vpReader.currentItem) ?: return@setOnClickListener
            readerViewModel?.decrementPage(chapter)
            mToolBarService?.restartTimer()
        }

        fabNextChapter.setOnClickListener {
            readerViewModel?.incrementChapter()
            mToolBarService?.restartTimer()
        }

        fabPrevChapter.setOnClickListener {
            readerViewModel?.decrementChapter()
            mToolBarService?.restartTimer()
        }
    }

    override fun onDestroyView()
    {
        super.onDestroyView()

        val parent = activity ?: return
        parent.unbindService(mConnection)
    }

    fun initViews()
    {
        if (MangaFeed.app.currentSourceType == MangaEnums.SourceType.NOVEL)
        {
            self.fabNextPage.visibility = View.GONE
            self.fabPrevPage.visibility = View.GONE
        }

        setupViewPager()
        setupToolbarService()
    }

    /***
     * This function hides the status bar, toolbar, and reader header/footers
     *
     */
    override fun hideToolbar()
    {
        val parent = activity ?: return
        self.topContainer.animate()
                .translationY((-self.topContainer.height - ScreenUtil.getStatusBarHeight(parent.resources)).toFloat())
                .setInterpolator(AccelerateInterpolator())
                .start()

        self.bottomContainer.animate()
                .translationY((self.bottomContainer.height + ScreenUtil.getNavigationBarHeight(parent.resources)).toFloat())
                .setInterpolator(AccelerateInterpolator())
                .start()

        parent.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

    }

    override fun hideSystemUi()
    {
        val parent = activity ?: return
        if (parent.window.decorView.systemUiVisibility == 0)
        {
            parent.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    fun onSingleTap()
    {
        val parent = activity ?: return

        if (parent.window.decorView.systemUiVisibility == 0) hideToolbar()
        else
        {
            showToolbar()
            mToolBarService?.startTimer()
        }

        mToolBarService?.startTimer()
    }

    /***********************************************************
     *
     * Reader Interactions
     *
     **********************************************************/
    fun onRefreshClicked()
    {
        MangaFeed.app.makeToastShort("NOT IMPLEMENTED")
        mToolBarService?.stopTimer()
    }

    fun onScreenRotateClicked()
    {
        MangaFeed.app.makeToastShort("NOT IMPLEMENTED")
        mToolBarService?.startTimer()
    }

    fun onVerticalScrollClicked()
    {
        MangaFeed.app.makeToastShort("NOT IMPLEMENTED")
        mToolBarService?.startTimer()
    }

    /***
     * This function sets up the activity viewpager.
     *
     */
    private fun setupViewPager()
    {
        self.vpReader.addOnPageChangeListener(object : ViewPager.OnPageChangeListener
        {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
            override fun onPageScrollStateChanged(state: Int) = Unit
            override fun onPageSelected(position: Int)
            {
                showToolbar()
                readerViewModel?.updateCurrentChapterByPosition(position)
            }
        })
    }

    /***
     * This function sets up the toolbar service, that hides the toolbar, header, footer, status bar, and nav bar
     * after a set period of time after it has been shown.
     *
     */
    private fun setupToolbarService()
    {
        mConnection = object : ServiceConnection
        {
            override fun onServiceConnected(className: ComponentName, service: IBinder)
            {
                // We've bound to ToolbarTimerService, cast the IBinder and get ToolbarTimerService instance
                val binder = service as ToolbarTimerService.LocalBinder
                mToolBarService = binder.service
                mToolBarService?.setToolbarListener(this@ReaderFragment)
                delayedAction(2000) { mToolBarService?.startTimer() }
            }

            override fun onServiceDisconnected(aComponent: ComponentName)
            {
                MangaLogger.logInfo(TAG, aComponent.flattenToShortString() + " service disconnected.")
            }
        }

        mToolBarService = ToolbarTimerService()
        mToolBarService?.setToolbarListener(this)

        val parent = activity ?: return
        val intent = Intent(parent, ToolbarTimerService::class.java)
        parent.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    /***
     * This function makes the status bar, toolbar, and reader header/footers visible
     *
     */
    private fun showToolbar()
    {
        self.topContainer.animate()
                .translationY(self.topContainer.scrollY.toFloat())
                .setInterpolator(AccelerateInterpolator())
                .start()

        self.bottomContainer.animate()
                .translationY((-self.bottomContainer.scrollY).toFloat())
                .setInterpolator(AccelerateInterpolator())
                .start()

        val parent = activity ?: return
        parent.window.decorView.systemUiVisibility = 0
    }

    private fun setupToolbar(title: String)
    {
        self.toolbar.title = title
        self.toolbar.setNavigationIcon(R.drawable.navigation_back)
        self.toolbar.setNavigationOnClickListener {
            val parent = activity ?: return@setNavigationOnClickListener
            (parent as FragmentNavMap).popBackStack()
        }
    }

    companion object
    {
        val TAG = ReaderFragment::class.java.simpleName
        fun newInstance() = ReaderFragment()
    }
}
