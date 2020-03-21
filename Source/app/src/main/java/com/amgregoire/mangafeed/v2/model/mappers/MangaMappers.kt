package com.amgregoire.mangafeed.v2.model.mappers

import com.amgregoire.mangafeed.Models.DbManga
import com.amgregoire.mangafeed.v2.enums.FollowType
import com.amgregoire.mangafeed.v2.enums.Source
import com.amgregoire.mangafeed.v2.interfaces.Mapper
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.model.dto.ApiManga
import com.amgregoire.mangafeed.v2.network.api.MangaApi
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.full.memberProperties

class DbMangaToMangaMapper : Mapper<DbManga, Manga>
{
    override fun map(input: DbManga): Manga
    {
        var unfilledProperties = 0
        for (prop in DbManga::class.memberProperties)
        {
            val anyProp = prop.get(input)
            if (anyProp is String? && anyProp.isNullOrEmpty()) unfilledProperties++
        }

        return Manga(
                id = null,
                name = input.title,
                description = input.description ?: "",
                link = input.link,
                image = input.image ?: "",
                alternateNames = input.alternate ?: "",
                artists = input.artist ?: "",
                authors = input.author ?: "",
                genres = input.genres ?: "",
                source = input.source, // TODO :: Source to enum -> will help when generating urls
                status = input.status ?: "",
                followType = FollowType.getTypeFromValue(input.following),
                recentChapter = input.recentChapter ?: "",
                requiresUpdate = unfilledProperties > 3
        )
    }
}

class MangaToDbMangaMapper : Mapper<Manga, DbManga>
{
    override fun map(input: Manga): DbManga
    {
        return DbManga(
                id = input.id,
                title = input.name,
                image = input.image,
                link = input.link,
                description = input.description,
                author = input.authors,
                artist = input.artists,
                genres = input.genres,
                status = input.status,
                source = input.source,
                alternate = input.alternateNames,
                following = input.followType.value,
                initialized = 0,
                recentChapter = input.recentChapter
        )
    }
}

class ApiMangaToMangaMapper : Mapper<ApiManga, Manga>
{
    override fun map(input: ApiManga): Manga
    {
        val lastUpdate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(input.updatedAt)
        val updateBy = Date(lastUpdate.time + (30 * 24 * 60 * 60 * 1000L)) // 1 month after lastUpdate

        return Manga(
                id = input.id,
                name = input.name,
                image = input.image,
                link = input.link,
                description = input.description,
                authors = input.authors,
                artists = input.artists,
                genres = input.genres,
                status = input.status,
                source = input.source,
                alternateNames = input.alternateNames,
                followType = FollowType.Unfollow,
                recentChapter = "",
                requiresUpdate = updateBy.before(Date())
        )
    }
}

class MangaToCreateMangaRequestMapper : Mapper<Manga, MangaApi.CreateMangaRequest>
{
    override fun map(input: Manga): MangaApi.CreateMangaRequest
    {
        val authors = input.authors.split(", ")
        val artists = input.artists.split(", ")
        val genres = input.genres.split(", ")

        return MangaApi.CreateMangaRequest(
                name = input.name,
                image = input.image,
                link = input.link,
                description = input.description,
                author = authors,
                artist = artists,
                genres = genres,
                status = input.status,
                source = Source.getSourceByName(input.source).sourceId,
                alternate = input.alternateNames
        )
    }
}
