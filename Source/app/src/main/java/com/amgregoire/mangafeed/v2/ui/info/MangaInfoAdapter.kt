package com.amgregoire.mangafeed.v2.ui.info

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase
import com.amgregoire.mangafeed.Models.Chapter
import com.amgregoire.mangafeed.Models.Manga
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.UI.Adapters.MangaInfoChaptersAdapter
import com.amgregoire.mangafeed.Utils.NetworkService
import com.amgregoire.mangafeed.v2.service.CloudflareService
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
        var chapters: List<Chapter> = listOf(),
        val chapterSelected: (Manga, List<Chapter>, Chapter) -> Unit
) : RecyclerView.Adapter<MangaInfoAdapter.BaseViewHolder>()
{
    private var data = arrayListOf<BaseData>()


    init
    {
        setData()
    }

    private fun setData()
    {
        data = arrayListOf()
        data.add(BaseData.MangaData)
        if (chapters.isEmpty()) data.add(BaseData.EmptyData)
        else
        {
            data.add(BaseData.HeaderData)
            chapters.forEach { chapter -> data.add(BaseData.ChapterData(chapter)) }
        }
    }

    fun updateInfo(manga: Manga, chapters: List<Chapter>)
    {
        this.manga = manga
        this.chapters = chapters
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
    open inner class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        open fun onBind(position: Int)
        {
        }
    }

    inner class ViewHolderHeader(itemView: View) : BaseViewHolder(itemView)
    {
        override fun onBind(position: Int)
        {
            itemView.tvMangaTitle.text = manga.title
            itemView.tvAlternate.text = manga.alternate
            itemView.tvArtist.text = manga.artist
            itemView.tvAuthor.text = manga.author
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

            if(image.isEmpty())
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
                CloudflareService().getCFCookies(image, NetworkService.defaultUserAgent) { cookies ->
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
            if (chapters.isEmpty()) itemView.tvChapterHeader.setText(R.string.manga_info_adapter_chapter_header_none)
            else itemView.tvChapterHeader.setText(R.string.manga_info_adapter_chapter_header)
        }
    }

    inner class ViewHolderChapter(itemView: View) : BaseViewHolder(itemView)
    {
        override fun onBind(position: Int)
        {
            val chapter = (data[position] as BaseData.ChapterData).chapter

            itemView.tvChapterTitle.text = chapter.chapterTitle
            itemView.tvChapterDate.text = chapter.chapterDate

            itemView.clParent.setOnClickListener{
                chapterSelected(manga, chapters, chapter)
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
        data class ChapterData(val chapter: Chapter) : BaseData()
        object HeaderData : BaseData()
        object EmptyData : BaseData()
    }

    companion object
    {
        val TAG = MangaInfoChaptersAdapter::class.java.simpleName
    }
}
