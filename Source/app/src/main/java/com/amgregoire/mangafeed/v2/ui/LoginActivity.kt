package com.amgregoire.mangafeed.v2.ui

import android.content.res.Resources
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.v2.extension.lastFragment
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.base.BaseNavigationActivity
import com.amgregoire.mangafeed.v2.ui.login.SignInFragment
import com.amgregoire.mangafeed.v2.ui.login.SignUpFragment
import com.amgregoire.mangafeed.v2.usecase.GetLocalUserUserCase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_m.vMenuCover


class LoginActivity : BaseNavigationActivity()
{
    val getUser = GetLocalUserUserCase()
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        toolbar.title = getString(R.string.empty)

        if (getUser.user() != null)
        {
            startActivity(MActivity.newInstance(this))
        }
        else setupView()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean
    {
        when (item?.itemId)
        {
            null -> Logger.debug("null menu item")
            android.R.id.home -> onBackPressed()
            R.id.menuHomeSearch -> Logger.debug("Need to implement search + filter fragment")
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

        buttonSignIn.setClickListener(View.OnClickListener {
            val fragment = SignInFragment()
            val tag = SignInFragment.TAG
            addFragment(fragment, tag)
        })

        buttonSignUp.setClickListener(View.OnClickListener {
            val fragment = SignUpFragment()
            val tag = SignUpFragment.TAG
            addFragment(fragment, tag)
        })

        buttonGuest.setClickListener(View.OnClickListener {
            startActivity(MActivity.newInstance(this))
        })
    }

    private fun setupBackStackListener()
    {
        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.lastFragment() ?: return@addOnBackStackChangedListener
            (fragment as BaseFragment).updateParentSettings()
        }
    }
}