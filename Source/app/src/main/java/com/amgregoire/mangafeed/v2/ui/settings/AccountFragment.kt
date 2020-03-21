package com.amgregoire.mangafeed.v2.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.base.FragmentNavMap
import com.amgregoire.mangafeed.v2.ui.map.ToolbarMap
import kotlinx.android.synthetic.main.fragment_more.view.*

class AccountFragment : BaseFragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        self = inflater.inflate(R.layout.fragment_more, null)
        return self
    }

    override fun onStart()
    {
        super.onStart()

        self.tvAccount.text = user()?.email ?: getString(R.string.guest)

        self.itConfigSettings.setOnClickListener {
            val parent = activity ?: return@setOnClickListener
            (parent as FragmentNavMap).addFragment(AccountFragmentSettings.newInstance(), this, AccountFragmentSettings.TAG)
        }

        self.buttonManage.setOnClickListener {
            MangaFeed.app.logout()
        }

        self.itConfigSourceSync.setOnClickListener(View.OnClickListener {
            val parent = activity ?: return@OnClickListener
            (parent as FragmentNavMap).addFragment(SyncSourceFragment.newInstance(), this, SyncSourceFragment.TAG)
        })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean)
    {
        if (isVisibleToUser) updateParentSettings()
    }

    override fun updateParentSettings()
    {
        val parent = activity ?: return
        (parent as ToolbarMap).setTitle(getString(R.string.nav_bottom_title_account))
        (parent as ToolbarMap).setOptionsMenu(R.menu.menu_empty)
    }

    companion object
    {
        fun newInstance() = AccountFragment()
    }
}