package com.amgregoire.mangafeed.v2.ui.read

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.*
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.MangaLogger
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.v2.NavigationType
import com.amgregoire.mangafeed.v2.ResourceFactory
import com.amgregoire.mangafeed.v2.enums.FollowType
import com.amgregoire.mangafeed.v2.enums.ReaderSettings
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.service.ScreenUtil
import com.amgregoire.mangafeed.v2.service.ToolbarTimerService
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.base.FragmentNavMap
import com.amgregoire.mangafeed.v2.ui.read.adapter.ChapterPagerAdapter
import kotlinx.android.synthetic.main.fragment_manga_info2.view.*
import kotlinx.android.synthetic.main.fragment_reader2.*
import kotlinx.android.synthetic.main.fragment_reader2.view.*
import kotlinx.android.synthetic.main.widget_toolbar_2.view.*

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

/***
 *
 * TODO ::
 * Because fitsSystemWindows=false
 * we need to add margin/padding to top and bottom views of reader again
 *
 */
class ReaderFragment : BaseFragment(), ToolbarTimerService.ReaderTimerListener {
    private val readerViewModel: ReaderViewModel? by lazy {
        val parent = activity ?: return@lazy null
        ViewModelProviders.of(parent).get(ReaderViewModel::class.java)
    }

    private var mToolBarService: ToolbarTimerService? = null
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to ToolbarTimerService, cast the IBinder and get ToolbarTimerService instance
            val binder = service as ToolbarTimerService.LocalBinder
            mToolBarService = binder.service
            mToolBarService?.setToolbarListener(this@ReaderFragment)
        }

        override fun onServiceDisconnected(aComponent: ComponentName) {
            MangaLogger.logInfo(TAG, aComponent.flattenToShortString() + " service disconnected.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        self = inflater.inflate(R.layout.fragment_reader2, null)
        return self
    }

    override fun onResume() {
        super.onResume()
        setupToolbarService()
        showToolbar()
        enterAndExitSystemUI()
    }

    override fun onPause() {
        super.onPause()
        showToolbar()
        enterAndExitSystemUI()

        val parent = activity ?: return
        parent.unbindService(mConnection)
        parent.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onStart() {
        super.onStart()

        readerViewModel?.let { readerVm ->
            val parent = activity ?: return
            readerVm.readerInfo.observe(parent, Observer { info ->
                info ?: return@Observer
                activity ?: return@Observer

                initViews()
                setupToolbar(info.manga.name)
                self.vpReader.offscreenPageLimit = 1
                if (self.vpReader.adapter == null) self.vpReader.adapter = ChapterPagerAdapter(childFragmentManager, info.dbChapters, info.manga.isFollowing, info.manga)
                if (MangaFeed.app.currentSourceType == MangaEnums.SourceType.NOVEL) self.vpReader.setPagingEnabled(true)

                val position = readerVm.getCurrentPosition()

                if (self.vpReader.currentItem == position) return@Observer

                self.vpReader.currentItem = position

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

                when (state) {
                    is ReaderUIState.INIT -> showToolbar()
                    is ReaderUIState.SHOW -> {
                        showToolbar()
                        mToolBarService?.startTimer()
                    }
                    else -> {
                        Logger.error("Toggling UI State = $state")
                        hideToolbar()
                    }
                }
            })
        }

        setupListeners()
    }

    private fun setupListeners() {
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

        imageViewReaderRefresh
        imageViewReaderRefresh.setOnClickListener {
            (self.vpReader.adapter as? ChapterPagerAdapter)?.updateItemAt(self.vpReader.currentItem)
        }

        self.ivReaderSetting.setOnClickListener {
            val context = context ?: return@setOnClickListener
            val popMenu = PopupMenu(context, self.ivReaderSetting, Gravity.RIGHT)

            popMenu.menuInflater.inflate(R.menu.menu_reader_setting, popMenu.menu)

            popMenu.setOnMenuItemClickListener { popupItem ->
                when (popupItem.itemId) {
                    R.id.menuReaderSettingLow -> readerViewModel?.updateReaderSettings(ReaderSettings.Low)
                    R.id.menuReaderSettingMedium -> readerViewModel?.updateReaderSettings(ReaderSettings.Medium)
                    R.id.menuReaderSettingMax -> readerViewModel?.updateReaderSettings(ReaderSettings.Max)
                    else -> readerViewModel?.updateReaderSettings(ReaderSettings.Max)
                }
                true
            }

            popMenu.show()
        }
    }

    fun initViews() {
        if (MangaFeed.app.currentSourceType == MangaEnums.SourceType.NOVEL) {
            self.fabNextPage.visibility = View.GONE
            self.fabPrevPage.visibility = View.GONE
        }

        setupViewPager()
    }

    fun hideSystemUi() {
        val parent = activity ?: return
        parent.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        if (SharedPrefs.isLightTheme()) parent.window.decorView.systemUiVisibility = parent.window.decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }

    override fun hideSystemUiService() {
        activity ?: return
        readerViewModel?.setUIStateHide()
        hideSystemUi()
    }

    private fun showSystemUi() {
        val parent = activity ?: return
        parent.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        if (SharedPrefs.isLightTheme()) parent.window.decorView.systemUiVisibility = parent.window.decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }

    private fun enterAndExitSystemUI() {
        val parent = activity ?: return
        parent.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        if (SharedPrefs.isLightTheme()) parent.window.decorView.systemUiVisibility = parent.window.decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        parent.window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        self.bottomContainer.setPadding(0, 0, 0, ScreenUtil.getNavigationBarHeight(resources))
        self.topContainer.setPadding(0, ScreenUtil.getStatusBarHeight(resources), 0, 0)
    }

    /***********************************************************
     *
     * Reader Interactions
     *
     **********************************************************/
    fun onRefreshClicked() {
        MangaFeed.app.makeToastShort("NOT IMPLEMENTED")
        mToolBarService?.stopTimer()
    }

    fun onScreenRotateClicked() {
        MangaFeed.app.makeToastShort("NOT IMPLEMENTED")
        mToolBarService?.startTimer()
    }

    fun onVerticalScrollClicked() {
        MangaFeed.app.makeToastShort("NOT IMPLEMENTED")
        mToolBarService?.startTimer()
    }

    /***
     * This function sets up the activity viewpager.
     *
     */
    private fun setupViewPager() {
        self.vpReader.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
            override fun onPageScrollStateChanged(state: Int) = Unit
            override fun onPageSelected(position: Int) {
                Logger.error("Chapter selected: $position")
                //                showToolbar()
                //                mToolBarService?.startTimer()
                readerViewModel?.updateCurrentChapterByPosition(position)
            }
        })
    }

    /***
     * This function sets up the toolbar service, that hides the toolbar, header, footer, status bar, and nav bar
     * after a set period of time after it has been shown.
     *
     */
    private fun setupToolbarService() {
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
    private fun showToolbar() {
        self.topContainer.animate()
                .translationY(self.topContainer.scrollY.toFloat())
                .setInterpolator(AccelerateInterpolator())
                .start()

        self.bottomContainer.animate()
                .translationY((-self.bottomContainer.scrollY).toFloat())
                .setInterpolator(AccelerateInterpolator())
                .start()

        showSystemUi()
    }

    /***
     * This function hides the status bar, toolbar, and reader header/footers
     *
     */
    fun hideToolbar() {
        val parent = activity ?: return
        self.topContainer.animate()
                .translationY((-self.topContainer.height - ScreenUtil.getStatusBarHeight(parent.resources)).toFloat())
                .setInterpolator(AccelerateInterpolator())
                .start()

        self.bottomContainer.animate()
                .translationY((self.bottomContainer.height + ScreenUtil.getNavigationBarHeight(parent.resources)).toFloat())
                .setInterpolator(AccelerateInterpolator())
                .start()

        hideSystemUi()
    }

    override fun hideToolbarService() {
        activity ?: return
        readerViewModel?.setUIStateHide()
        hideToolbar()
    }

    private fun setupToolbar(title: String) {
        self.toolbar.title = title
        self.toolbar.setNavigationIcon(ResourceFactory.getNavigationIcon(NavigationType.Back))
        self.toolbar.setNavigationOnClickListener {
            val parent = activity ?: return@setNavigationOnClickListener
            (parent as FragmentNavMap).popBackStack()
        }
    }

    companion object {
        val TAG: String = ReaderFragment::class.java.simpleName
        fun newInstance() = ReaderFragment()
    }
}
