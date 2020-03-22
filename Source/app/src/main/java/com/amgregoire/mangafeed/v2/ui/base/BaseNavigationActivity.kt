package com.amgregoire.mangafeed.v2.ui.base

import android.graphics.Color
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.extension.gone
import com.amgregoire.mangafeed.v2.extension.visible
import com.amgregoire.mangafeed.v2.service.ScreenUtil
import com.amgregoire.mangafeed.v2.ui.map.ToolbarMap
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_m.*
import kotlinx.android.synthetic.main.widget_toolbar_2.*
import kotlinx.android.synthetic.main.widget_toolbar_2.toolbar
import kotlinx.android.synthetic.main.widget_toolbar_2.toolbarTitle
import kotlinx.android.synthetic.main.widget_toolbar_filter.*

/**
 * Created by amgregoi on 1/24/19.
 */
interface StatusBarMap
{
    fun setStatusBarDefault()

    fun setStatusBarTransparent()

    fun addToolbarPadding()
}

interface FragmentNavMap
{
    fun startMenuFragment(fragment: Fragment, tag: String)

    fun hideMenuCover()

    fun popBackStack()

    fun popBackStack(tag: String, popInclusive: Boolean)

    fun replaceFragment(fragment: Fragment, tag: String, enter: Int = R.anim.slide_in_from_right, exit: Int = R.anim.slide_out_to_left, popEnter: Int = R.anim.slide_in_from_left, popExit: Int = R.anim.slide_out_to_right)

    fun replaceFragmentParent(fragment: Fragment, tag: String, enter: Int = R.anim.slide_in_from_right, exit: Int = R.anim.slide_out_to_left, popEnter: Int = R.anim.slide_in_from_left, popExit: Int = R.anim.slide_out_to_right)

    fun addFragment(fragment: Fragment, tag: String, enter: Int = R.anim.slide_in_from_right, exit: Int = R.anim.slide_out_to_left, popEnter: Int = R.anim.slide_in_from_left, popExit: Int = R.anim.slide_out_to_right)

    fun addFragment(fragment: Fragment, prevFragment: Fragment, tag: String, enter: Int = R.anim.slide_in_from_right, exit: Int = R.anim.slide_out_to_left, popEnter: Int = R.anim.slide_in_from_left, popExit: Int = R.anim.slide_out_to_right)

    fun addFragmentParent(fragment: Fragment, prevFragment: Fragment, tag: String, enter: Int = R.anim.slide_in_from_right, exit: Int = R.anim.slide_out_to_left, popEnter: Int = R.anim.slide_in_from_left, popExit: Int = R.anim.slide_out_to_right)

    fun addFragmentParent(fragment: Fragment, tag: String, enter: Int = R.anim.slide_in_from_right, exit: Int = R.anim.slide_out_to_left, popEnter: Int = R.anim.slide_in_from_left, popExit: Int = R.anim.slide_out_to_right)
}

interface NotificationMap
{
    fun snackbar(message: String)
    fun snackbarError(message: String)
}

abstract class BaseNavigationActivity : AppCompatActivity(), FragmentNavMap, ToolbarMap, StatusBarMap, NotificationMap
{


    protected var mMenuId = 0

    override fun replaceFragment(fragment: Fragment, tag: String, enter: Int, exit: Int, popEnter: Int, popExit: Int)
    {
        supportFragmentManager.beginTransaction()
                .setPrimaryNavigationFragment(fragment)
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .replace(flContainer.id, fragment, tag)
                .addToBackStack(tag)
                .commit()
    }

    override fun replaceFragmentParent(fragment: Fragment, tag: String, enter: Int, exit: Int, popEnter: Int, popExit: Int)
    {
        supportFragmentManager.beginTransaction()
                .setPrimaryNavigationFragment(fragment)
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .replace(flParent.id, fragment, tag)
                .addToBackStack(tag)
                .commit()
    }

    override fun addFragment(fragment: Fragment, tag: String, enter: Int, exit: Int, popEnter: Int, popExit: Int)
    {
        supportFragmentManager.beginTransaction()
                .setPrimaryNavigationFragment(fragment)
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .add(flContainer.id, fragment, tag)
                .addToBackStack(tag)
                .commit()
    }

    override fun addFragment(fragment: Fragment, prevFragment: Fragment, tag: String, enter: Int, exit: Int, popEnter: Int, popExit: Int)
    {
        supportFragmentManager.beginTransaction()
                .setPrimaryNavigationFragment(fragment)
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .add(flContainer.id, fragment, tag)
                .addToBackStack(tag)
                .hide(prevFragment)
                .commit()
    }


    override fun addFragmentParent(fragment: Fragment, prevFragment: Fragment, tag: String, enter: Int, exit: Int, popEnter: Int, popExit: Int)
    {
        supportFragmentManager.beginTransaction()
                .setPrimaryNavigationFragment(fragment)
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .add(flParent.id, fragment, tag)
                .addToBackStack(tag)
                .hide(prevFragment)
                .commit()
    }

    override fun addFragmentParent(fragment: Fragment, tag: String, enter: Int, exit: Int, popEnter: Int, popExit: Int)
    {
        supportFragmentManager.beginTransaction()
                .setPrimaryNavigationFragment(fragment)
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .add(flParent.id, fragment, tag)
                .addToBackStack(tag)
                .commit()
    }

    override fun startMenuFragment(fragment: Fragment, tag: String)
    {
        vMenuCover.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fab_slide_in_from_right, 0, 0, 0) // todo make slide out bottom animation
                .add(flMenuContainer.id, fragment, tag)
                .addToBackStack(tag)
                .commit()
    }

    override fun hideMenuCover()
    {
        vMenuCover.visibility = View.GONE
    }

    override fun popBackStack()
    {
        supportFragmentManager.popBackStack()
    }

    override fun popBackStack(tag: String, popInclusive: Boolean)
    {
        val popFlag = if (popInclusive) androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE else 0
        supportFragmentManager.popBackStack(tag, popFlag)
    }

    /********************************************************
     *
     * Toolbar Map
     *
     */

    override fun setTitle(title: String)
    {
        if (toolbar == null) return
        toolbar.title = null

        searchView?.gone()
        toolbarSpinner?.gone()
        toolbarTitle.text = title
        toolbarTitle.visible()
    }

    override fun showSpinner()
    {
        if (toolbar == null) return

        toolbar.title = null

        toolbarTitle.gone()
        toolbarSpinner?.visible()
    }

    override fun showSearchView()
    {
        toolbar ?: return

        toolbarTitle.gone()
        toolbarSpinner?.gone()
        searchView?.visible()
    }

    override fun setNavigationIcon(drawableId: Int?)
    {
        if (toolbar == null) return
        if (drawableId == null || drawableId == 0)
        {
            toolbar.navigationIcon = null
        }
        else
        {
            toolbar.navigationIcon = resources.getDrawable(drawableId)
        }
    }


    override fun setToolbarColor(color: Int)
    {
        if (toolbar == null) return
        toolbar.setBackgroundColor(getResources().getColor(color))
    }

    override fun hideToolbar()
    {
        if (toolbar == null) return
        toolbar.visibility = View.GONE
    }

    override fun showToolbar()
    {
        if (toolbar == null) return
        toolbar.visibility = View.VISIBLE
    }

    override fun setToolbarVisibility(visibility: Int)
    {
        if (toolbar == null) return
        toolbar!!.visibility = visibility
    }

    override fun setOptionsMenu(menuId: Int?)
    {
        mMenuId = menuId ?: 0
        invalidateOptionsMenu()
    }

    /********************************************************
     *
     * Status Bar Map
     *
     */
    val STATUS_BAR_COLOR = "STATUS_BAR_COLOR"
    val STATUS_BAR_VISIBILITY = "STATUS_BAR_VISIBILITY"

    override fun setStatusBarDefault()
    {
        removeToolbarPadding()
        window.statusBarColor = intent.getIntExtra(STATUS_BAR_COLOR, 0)
        window.decorView.systemUiVisibility = getIntent().getIntExtra(STATUS_BAR_VISIBILITY, 0)
        window.decorView.requestApplyInsets()
    }

    override fun setStatusBarTransparent()
    {
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        addToolbarPadding()
    }

    override fun addToolbarPadding()
    {
        val params = toolbar.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = ScreenUtil.getStatusBarHeight(resources)
        toolbar.layoutParams = params
    }


    private fun removeToolbarPadding()
    {
        val params = toolbar.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = 0
        toolbar.layoutParams = params
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        // If we need to support any future menus
        when (mMenuId)
        {
            0 -> menuInflater.inflate(R.menu.menu_empty, menu)
            else -> menuInflater.inflate(mMenuId, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    /********************************************************
     *
     * Navigation Map
     *
     */
    override fun snackbar(message: String)
    {
        val lSnack = Snackbar.make(flParent, message, Snackbar.LENGTH_SHORT)
        val lSnackText = lSnack.view
                .findViewById<TextView>(R.id.snackbar_text)
        lSnackText.gravity = Gravity.CENTER_HORIZONTAL
        lSnackText.textAlignment = View.TEXT_ALIGNMENT_CENTER

        lSnack.show()
    }

    override fun snackbarError(message: String)
    {
        val lSnack = Snackbar.make(flParent, message, Snackbar.LENGTH_LONG)
        val lSnackText = lSnack.view
                .findViewById<TextView>(R.id.snackbar_text)
        lSnack.view.setBackgroundColor(resources.getColor(R.color.manga_red))
        lSnackText.gravity = Gravity.CENTER_HORIZONTAL
        lSnackText.textAlignment = View.TEXT_ALIGNMENT_CENTER

        lSnack.show()
    }
}

