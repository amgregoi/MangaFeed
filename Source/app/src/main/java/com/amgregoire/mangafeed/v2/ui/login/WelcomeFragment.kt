package com.amgregoire.mangafeed.v2.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.ui.MainActivity
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.base.FragmentNavMap
import com.amgregoire.mangafeed.v2.ui.login.vm.SignUpVM
import com.amgregoire.mangafeed.v2.ui.map.ToolbarMap
import kotlinx.android.synthetic.main.fragment_welcome.*

class WelcomeFragment : BaseFragment()
{
    private val loginVM by lazy { ViewModelProviders.of(this).get(SignUpVM::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        self = inflater.inflate(R.layout.fragment_welcome, null)
        return self
    }

    override fun onStart()
    {
        super.onStart()

        buttonSignIn.setClickListener(View.OnClickListener {
            val fragment = SignInFragment()
            val tag = SignInFragment.TAG
            (activity as? FragmentNavMap)?.addFragment(fragment, this, tag)
        })

        buttonSignUp.setClickListener(View.OnClickListener {
            val fragment = SignUpFragment()
            val tag = SignUpFragment.TAG
            (activity as? FragmentNavMap)?.addFragment(fragment, this, tag)
        })

        buttonGuest.setClickListener(View.OnClickListener {
            val parent = activity ?: return@OnClickListener
            MangaFeed.app.isSignedIn = true // Manually set sign in status for guests
            startActivity(MainActivity.newInstance(parent))
        })

    }

    override fun updateParentSettings()
    {
        super.updateParentSettings()
        (activity as? ToolbarMap)?.setNavigationIcon(null)
    }

    companion object
    {
        val TAG: String = WelcomeFragment::class.java.simpleName
        fun newInstance() = WelcomeFragment()
    }
}