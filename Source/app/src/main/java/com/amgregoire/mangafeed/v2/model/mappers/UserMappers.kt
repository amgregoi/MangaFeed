package com.amgregoire.mangafeed.v2.model.mappers

import com.amgregoire.mangafeed.v2.interfaces.Mapper
import com.amgregoire.mangafeed.v2.model.domain.User
import com.amgregoire.mangafeed.v2.model.dto.ApiUser
import com.amgregoire.mangafeed.v2.network.response.LoginResponse

class GetUserDataMapper : Mapper<ApiUser, User>
{
    override fun map(input: ApiUser): User
    {
        return User(input.id, input.name, input.email, input.accessToken)
    }
}

class LoginUserDataMapper : Mapper<LoginResponse, User>
{
    override fun map(input: LoginResponse): User
    {
        return User(input.user.id, input.user.name, input.user.email, input.accessToken)
    }
}