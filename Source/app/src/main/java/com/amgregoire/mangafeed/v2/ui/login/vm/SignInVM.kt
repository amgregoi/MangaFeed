package com.amgregoire.mangafeed.v2.ui.login.vm

import androidx.lifecycle.MutableLiveData
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.model.domain.User
import com.amgregoire.mangafeed.v2.ui.base.ViewModelBase
import com.amgregoire.mangafeed.v2.usecase.remote.SignInUseCase

class SignInVM : ViewModelBase()
{
    private val signInUserCase = SignInUseCase()
    private val emailRegex = MangaFeed.app.getString(R.string.email_regex).toRegex()
    private val passwordRegex = MangaFeed.app.getString(R.string.password_regex).toRegex()

    val state = MutableLiveData<State>()

    init
    {
        state.value = State.Default
    }


    fun login(email: String, password: String)
    {
        state.value = State.Loading

        if(!emailRegex.containsMatchIn(email))
        {
            state.value = State.Failed(R.string.login_error_invalid_email)
            return
        }

        if(!passwordRegex.containsMatchIn(password))
        {
            state.value = State.Failed(R.string.login_error_invalid_password)
            return
        }

        signInUserCase.login(
                email = email.trim(),
                password = password.trim(),
                result = { user ->
                    state.value = State.Success(user)
                },
                error = { errorCode ->
                    val message = when (errorCode)
                    {
                        404 -> R.string.login_error_404
                        else -> R.string.login_error
                    }

                    state.value = State.Failed(message)
                })
    }

    sealed class State
    {
        object Default : State()
        object Loading : State()
        class Success(val user: User) : State()
        class Failed(val stringId: Int) : State()
    }
}