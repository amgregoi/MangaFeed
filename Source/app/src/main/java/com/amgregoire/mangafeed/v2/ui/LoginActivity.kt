package com.amgregoire.mangafeed.v2.ui

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.v2.extension.lastFragment
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.base.BaseNavigationActivity
import com.amgregoire.mangafeed.v2.ui.login.WelcomeFragment
import com.amgregoire.mangafeed.v2.ui.main.MFragment
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.flContainer
import kotlinx.android.synthetic.main.activity_m.vMenuCover


class LoginActivity : BaseNavigationActivity()
{
    private val user = MangaFeed.app.user
    private val isSignedIn = MangaFeed.app.isSignedIn

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        toolbar.title = getString(R.string.empty)

        if (user != null || isSignedIn)
        {
            startActivity(MainActivity.newInstance(this))
        }
        else setupView()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean
    {
        when (item?.itemId)
        {
            null -> Logger.debug("null menu item")
            android.R.id.home -> onBackPressed()
            else -> Logger.debug("Unknown menu item selected -> $item -> ${item.menuInfo}")
        }

        return false
    }

    override fun onBackPressed()
    {
        val manager = supportFragmentManager

        if (manager.backStackEntryCount > 0)
        {
            if (manager.backStackEntryCount == 1)
            {
                setNavigationIcon(null)
            }

            manager.lastFragment()?.let { fragment ->

                if (fragment is BaseFragment)
                {
                    if (vMenuCover.visibility == View.VISIBLE) vMenuCover.visibility = View.GONE
                    fragment.onChildBackPress()
                    return
                }
            }
        }

        super.onBackPressed()
    }

    override fun getTheme(): Resources.Theme
    {
        val theme = super.getTheme()

        if (SharedPrefs.isLightTheme()) theme.applyStyle(R.style.AppTheme_Light, true)
        else theme.applyStyle(R.style.AppTheme_Dark, true)

        return theme
    }

    private fun setupView()
    {
        setSupportActionBar(toolbar)
        setupBackStackListener()

        val welcomeFragment = WelcomeFragment.newInstance()
        supportFragmentManager.beginTransaction()
                .setPrimaryNavigationFragment(welcomeFragment)
                .add(flContainer.id, welcomeFragment, MFragment.TAG)
                .commit()
    }

    private fun setupBackStackListener()
    {
        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.lastFragment() ?: return@addOnBackStackChangedListener
            (fragment as BaseFragment).updateParentSettings()
        }
    }

    companion object
    {
        fun newInstance(context: Context) = Intent(context, LoginActivity::class.java)
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }

    }
}