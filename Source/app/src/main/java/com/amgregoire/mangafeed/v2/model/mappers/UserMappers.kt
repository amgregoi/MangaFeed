package com.amgregoire.mangafeed.v2.model.mappers

import com.amgregoire.mangafeed.Models.DbManga
import com.amgregoire.mangafeed.v2.enums.FollowType
import com.amgregoire.mangafeed.v2.interfaces.Mapper
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.model.domain.User
import com.amgregoire.mangafeed.v2.model.domain.UserLibrary
import com.amgregoire.mangafeed.v2.model.dto.ApiManga
import com.amgregoire.mangafeed.v2.model.dto.ApiUser
import com.amgregoire.mangafeed.v2.network.response.LoginResponse
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.full.memberProperties

class GetUserDataMapper : Mapper<ApiUser, User>
{
    override fun map(input: ApiUser): User
    {
        return User(input.id, input.email, input.accessToken)
    }
}

class LoginUserDataMapper : Mapper<LoginResponse, User>
{
    override fun map(input: LoginResponse): User
    {
        return User(input.user.id, input.user.email, input.accessToken)
    }
}

class UserLibraryMapper : Mapper<ApiUser, UserLibrary>
{
    override fun map(input: ApiUser): UserLibrary
    {
        val library = arrayListOf<Manga>()

        input.reading.forEach{
            library.add(map(it, FollowType.Reading))
        }

        input.complete.forEach{
            library.add(map(it, FollowType.Completed))
        }

        input.onHold.forEach{
            library.add(map(it, FollowType.On_Hold))
        }

        input.planToRead.forEach{
            library.add(map(it, FollowType.Plan_to_Read))
        }

        return UserLibrary(library)
    }

    private fun map(input: ApiManga, followType: FollowType): Manga
    {
        val lastUpdate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(input.updatedAt)
        val updateBy = Date(lastUpdate.time + (30 * 24 * 60 * 60 * 1000L)) // 1 month after lastUpdate

        return Manga(
                id = input.id,
                name = input.name,
                image = input.image,
                description = input.description,
                link = input.link,
                alternateNames = input.alternateNames,
                artists = input.artists,
                authors = input.authors,
                genres = input.genres,
                source = input.source,
                followType = followType,
                recentChapter = "",
                status = input.status,
                requiresUpdate = updateBy.before(Date())
        )
    }
}