package com.amgregoire.mangafeed.v2.model.mappers

import com.amgregoire.mangafeed.Models.DbManga
import com.amgregoire.mangafeed.v2.interfaces.Mapper
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.ui.catalog.enum.FollowType
import java.util.*

class DatabaseMangaMapper : Mapper<DbManga, Manga>
{
    override fun map(input: DbManga): Manga
    {
        return Manga(
                id = UUID.randomUUID().toString(),
                name = input.title,
                description = input.description ?: "",
                link =  input.link,
                alternateNames = input.alternate ?: "",
                artists =  input.artist ?: "",
                authors = input.author ?: "",
                genres = input.genres ?: "",
                source = input.source, // TODO :: Source to enum -> will help when generating urlsn
                followType = FollowType.getTypeFromValue(input.following)
        )
    }
}