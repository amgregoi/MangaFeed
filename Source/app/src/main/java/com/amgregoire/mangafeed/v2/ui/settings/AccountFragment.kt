package com.amgregoire.mangafeed.v2.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.UI.Fragments.AccountFragmentSettings
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.ui.BaseFragment
import com.amgregoire.mangafeed.v2.ui.FragmentNavMap
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

        self.itConfigSettings.setOnClickListener{
            val parent = activity ?: return@setOnClickListener
            (parent as FragmentNavMap).replaceFragment(AccountFragmentSettings.newInstance(), AccountFragmentSettings.TAG)
        }

        self.buttonManage.setClickListener(View.OnClickListener {
            Logger.error("test")
        })
    }

    companion object
    {
        fun newInstance() = AccountFragment()
    }
}