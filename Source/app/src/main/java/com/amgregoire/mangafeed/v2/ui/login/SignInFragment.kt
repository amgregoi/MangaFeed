package com.amgregoire.mangafeed.v2.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.NavigationType
import com.amgregoire.mangafeed.v2.ResourceFactory
import com.amgregoire.mangafeed.v2.model.domain.User
import com.amgregoire.mangafeed.v2.ui.MActivity
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.base.FragmentNavMap
import com.amgregoire.mangafeed.v2.ui.base.NotificationMap
import com.amgregoire.mangafeed.v2.ui.login.vm.SignInVM
import com.amgregoire.mangafeed.v2.ui.map.ToolbarMap
import kotlinx.android.synthetic.main.fragment_sign_in.view.*

class SignInFragment : BaseFragment()
{
    private val loginVM by lazy { ViewModelProviders.of(this).get(SignInVM::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        self = inflater.inflate(R.layout.fragment_sign_in, null)
        return self
    }

    override fun onStart()
    {
        super.onStart()

        self.buttonSignIn.setClickListener(View.OnClickListener {
            val email = self.inputEmail.getInputText().trim()
            val password = self.inputPassword.getInputText().trim()
            loginVM.login(email, password)
        })

        loginVM.state.observe(this, Observer {
            when (it)
            {
                is SignInVM.State.Loading -> renderLoading()
                is SignInVM.State.Default -> renderDefault()
                is SignInVM.State.Success -> renderSuccess(it.user)
                is SignInVM.State.Failed -> renderFailure(it.stringId)
            }
        })
    }

    override fun updateParentSettings()
    {
        super.updateParentSettings()
        (activity as? ToolbarMap)?.setNavigationIcon(ResourceFactory.getNavigationIcon(NavigationType.Back))
    }

    private fun renderDefault()
    {
        self.loading.hide()
        self.inputEmail.setInputText("")
        self.inputPassword.setInputText("")
    }

    private fun renderLoading()
    {
        self.loading.show()
    }

    private fun renderFailure(stringId: Int)
    {
        self.loading.hide()
        (activity as? NotificationMap)?.snackbarError(getString(stringId))
    }

    private fun renderSuccess(user: User)
    {
        self.loading.hide()
        MangaFeed.app.user = user

        context?.let {
            startActivity(MActivity.newInstance(it))
        }
    }

    companion object
    {
        val TAG: String = SignInFragment::class.java.simpleName
        fun newInstance() = SignInFragment()
    }
}