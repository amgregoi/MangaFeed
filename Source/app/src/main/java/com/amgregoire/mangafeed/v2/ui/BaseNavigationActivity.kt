package com.amgregoire.mangafeed.v2.ui

import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.View
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.service.ScreenUtil
import com.amgregoire.mangafeed.v2.ui.map.ToolbarMap
import kotlinx.android.synthetic.main.activity_m.*
import kotlinx.android.synthetic.main.widget_toolbar_2.*

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
}

abstract class BaseNavigationActivity : AppCompatActivity(), FragmentNavMap, ToolbarMap, StatusBarMap
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
        val popFlag = if (popInclusive) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0
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
        toolbar.setTitle(null)

        toolbarLogo.visibility = View.GONE
        toolbarSpinner.visibility = View.GONE
        toolbarTitle.text = title
        toolbarTitle.visibility = View.VISIBLE
    }

    override fun showSpinner()
    {
        toolbar.setTitle(null)

        toolbarTitle.visibility = View.GONE
        toolbarLogo.visibility = View.GONE
        toolbarSpinner.visibility = View.VISIBLE
    }

    fun showToolBarLogo()
    {
        hideToolbarElevation()
        toolbarLogo.visibility = View.VISIBLE
        toolbarTitle.visibility = View.GONE
    }

    fun setNavigationIcon(drawableId: Int)
    {
        if (toolbar == null) return
        if (drawableId == 0)
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

    override fun showToolbarElevation()
    {
        if (toolbar == null) return
        toolbar.elevation = ScreenUtil.convertDpToPixel(4f, this)
    }

    override fun hideToolbarElevation()
    {
        if (toolbar == null) return
        toolbar.elevation = 0f
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
}
/********************************************************
 *
 * Fragment Navigation Map
 *
 */
