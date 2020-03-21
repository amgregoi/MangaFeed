package com.amgregoire.mangafeed.v2.ui.info

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase
import com.amgregoire.mangafeed.Models.DbChapter
import com.amgregoire.mangafeed.Models.DbManga
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.Utils.NetworkService
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.repository.local.LocalChapterRepository
import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_manga_info_adapter_chapter2.view.*
import kotlinx.android.synthetic.main.item_manga_info_adapter_chapter_header2.view.*
import kotlinx.android.synthetic.main.item_manga_info_adapter_header2.view.*

/**
 * Created by Andy Gregoire on 3/12/2018.
 */

class MangaInfoAdapter(
        var manga: Manga,
        val source: SourceBase,
        var dbChapters: List<DbChapter> = listOf(),
        val chapterSelected: (Manga, List<DbChapter>, DbChapter) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<MangaInfoAdapter.BaseViewHolder>()
{
    private var data = arrayListOf<BaseData>()
    private val localChapterRepository = LocalChapterRepository()

    init
    {
        setData()
    }

    private fun setData()
    {
        data = arrayListOf()
        data.add(BaseData.MangaData)
        if (dbChapters.isEmpty()) data.add(BaseData.EmptyData)
        else
        {
            data.add(BaseData.HeaderData)
            dbChapters.forEach { chapter -> data.add(BaseData.ChapterData(chapter)) }
        }
    }

    fun updateInfo(manga: Manga, dbChapters: List<DbChapter>)
    {
        this.manga = manga
        this.dbChapters = dbChapters
        setData()
        notifyDataSetChanged()
    }

    /**************************************************************************************
     *
     * Implementation
     *
     *************************************************************************************/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder
    {
        val lInflater = LayoutInflater.from(parent.context)
        val lHolder: BaseViewHolder
        val lView: View

        when (viewType)
        {
            BaseData.ViewType.MangaData.type ->
            {
                lView = lInflater.inflate(R.layout.item_manga_info_adapter_header2, parent, false)
                lHolder = ViewHolderHeader(lView)
            }
            BaseData.ViewType.ChapterData.type ->
            {
                lView = lInflater.inflate(R.layout.item_manga_info_adapter_chapter2, parent, false)
                lHolder = ViewHolderChapter(lView)
            }
            else ->
            {
                lView = lInflater.inflate(R.layout.item_manga_info_adapter_chapter_header2, parent, false)
                lHolder = ViewHolderChapterHeader(lView)
            }
        }

        return lHolder
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int)
    {
        holder.onBind(position)
    }

    override fun getItemCount(): Int
    {
        return data.size
    }

    override fun getItemViewType(position: Int): Int
    {
        return when (data[position])
        {
            is BaseData.MangaData -> BaseData.ViewType.MangaData.type
            is BaseData.ChapterData -> BaseData.ViewType.ChapterData.type
            is BaseData.HeaderData -> BaseData.ViewType.HeaderData.type
            is BaseData.EmptyData -> BaseData.ViewType.EmptyData.type
        }
    }

    /**************************************************************************************
     *
     * View Holders
     *
     *************************************************************************************/
    open inner class BaseViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
    {
        open fun onBind(position: Int)
        {
        }
    }

    inner class ViewHolderHeader(itemView: View) : BaseViewHolder(itemView)
    {
        override fun onBind(position: Int)
        {
            itemView.tvMangaTitle.text = manga.name
            itemView.tvAlternate.text = manga.alternateNames
            itemView.tvArtist.text = manga.artists
            itemView.tvAuthor.text = manga.authors
            itemView.tvGenre.text = manga.genres
            itemView.tvDescription.text = manga.description
            itemView.tvStatus.text = manga.status

            setupImages()
        }

        private fun setupImages()
        {
            val image = manga.image ?: run {
                itemView.ivMangaInfoBackground.setBackgroundResource(R.color.colorAccent)
                itemView.ivMangaInfoBackground.setImageResource(R.drawable.manga_error)
                return
            }

            if (image.isEmpty())
            {
                itemView.ivMangaInfoBackground.setBackgroundResource(R.color.colorAccent)
                itemView.ivMangaInfoBackground.setImageResource(R.drawable.manga_error)
                return
            }

            val lOptions = RequestOptions()
            lOptions.fitCenter()
                    .placeholder(itemView.context.resources.getDrawable(R.drawable.manga_loading_image))
                    .error(itemView.context.resources.getDrawable(R.drawable.manga_error))
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)


            val builder = LazyHeaders.Builder().addHeader("User-Agent", NetworkService.defaultUserAgent)

            if (source.requiresCloudFlare())
            {
                CloudFlareService().getCFCookies(image, NetworkService.defaultUserAgent) { cookies ->
                    for (cookie in cookies) builder.addHeader("Cookie", cookie)
                }
            }

            val glideUrl = GlideUrl(image, builder.build())

            Glide.with(itemView.context)
                    .asBitmap()
                    .load(glideUrl)
                    .apply(lOptions)
                    .transition(GenericTransitionOptions<Any>().transition(android.R.anim.fade_in))
                    .into(itemView.ivMangaInfoBackground)

            Glide.with(itemView.context)
                    .asBitmap()
                    .load(glideUrl)
                    .apply(lOptions)
                    .transition(GenericTransitionOptions<Any>().transition(android.R.anim.fade_in))
                    .into(itemView.ivMangaInfo)

        }
    }

    inner class ViewHolderChapterHeader(itemView: View) : BaseViewHolder(itemView)
    {
        override fun onBind(position: Int)
        {
            if (dbChapters.isEmpty()) itemView.tvChapterHeader.setText(R.string.manga_info_adapter_chapter_header_none)
            else itemView.tvChapterHeader.setText(R.string.manga_info_adapter_chapter_header)
        }
    }

    inner class ViewHolderChapter(itemView: View) : BaseViewHolder(itemView)
    {
        override fun onBind(position: Int)
        {
            val chapter = (data[position] as BaseData.ChapterData).dbChapter

            itemView.tvChapterTitle.text = chapter.chapterTitle
            itemView.tvChapterDate.text = chapter.chapterDate


            val hasRead = localChapterRepository.getChapter(chapter) != null
            if (hasRead) itemView.ivReadIndicator.visibility = View.VISIBLE
            else itemView.ivReadIndicator.visibility = View.GONE

            itemView.clParent.setOnClickListener {
                chapterSelected(manga, dbChapters, chapter)
            }
        }
    }

    /**************************************************************************************
     *
     * Data Classes
     *
     *************************************************************************************/

    sealed class BaseData
    {
        enum class ViewType(val type: Int)
        { MangaData(0), ChapterData(1), HeaderData(2), EmptyData(3) }

        object MangaData : BaseData()
        data class ChapterData(val dbChapter: DbChapter) : BaseData()
        object HeaderData : BaseData()
        object EmptyData : BaseData()
    }

    companion object
    {
        val TAG: String = MangaInfoAdapter::class.java.simpleName
    }
}
