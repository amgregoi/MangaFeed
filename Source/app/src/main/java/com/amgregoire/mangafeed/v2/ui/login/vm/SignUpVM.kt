package com.amgregoire.mangafeed.v2.ui.login.vm

import androidx.lifecycle.MutableLiveData
import com.amgregoire.mangafeed.MangaFeed.Companion.app
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.model.domain.User
import com.amgregoire.mangafeed.v2.ui.base.ViewModelBase
import com.amgregoire.mangafeed.v2.usecase.LoginUseCase
import com.amgregoire.mangafeed.v2.usecase.SignUpUseCase

class SignUpVM : ViewModelBase()
{
    private val signUpUseCase = SignUpUseCase()
    private val emailRegex = app.getString(R.string.email_regex).toRegex()
    private val passwordRegex = app.getString(R.string.password_regex).toRegex()

    val state = MutableLiveData<State>()

    init
    {
        state.value = State.Default
    }

    fun signUp(email: String, password: String, passwordConfirm:String)
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

        if(password != passwordConfirm)
        {
            state.value = State.Failed(R.string.passwords_must_match)
            return
        }

        signUpUseCase.signUp(
                email = email,
                password = password,
                result = { user ->
                    state.value = State.Success(user)
                },
                error = { errorCode ->
                    state.value = State.Failed(R.string.login_error)
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