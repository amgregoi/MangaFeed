package com.amgregoire.mangafeed.v2.extension

import com.amgregoire.mangafeed.v2.model.dto.ApiUser
import com.amgregoire.mangafeed.v2.model.UserAuthToken
import com.amgregoire.mangafeed.v2.model.domain.User

fun User.getAuthToken() = UserAuthToken(this.id, this.accessToken)