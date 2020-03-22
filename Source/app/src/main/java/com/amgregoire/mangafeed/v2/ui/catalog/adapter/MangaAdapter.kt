package com.amgregoire.mangafeed.v2.ui.catalog.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.NetworkService
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.ScaleImageViewTarget
import com.amgregoire.mangafeed.v2.enums.FollowType
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.amgregoire.mangafeed.v2.service.ScreenUtil
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_manga.view.*
import kotlinx.coroutines.launch


/**
 * Created by Andy Gregoire on 3/8/2018.
 */

class MangaAdapter(
        var data: ArrayList<Manga>,
        private var source: SourceBase,
        private val isLibrary: Boolean,
        private var itemSelected: (Manga) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<MangaAdapter.MangaViewHolder>()
{
    private var filteredData: ArrayList<Manga> = ArrayList(data)
    private val filter = TextFilter()

    init
    {
        setHasStableIds(true)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaViewHolder
    {
        val lView = LayoutInflater.from(parent.context).inflate(R.layout.item_manga, parent, false)
        return MangaViewHolder(lView)
    }

    override fun onBindViewHolder(holder: MangaViewHolder, position: Int)
    {
        holder.onBind(position)
    }

    override fun getItemCount(): Int
    {
        return filteredData.size
    }

    override fun getItemId(position: Int): Long
    {
        val item = filteredData[position]
        return "${item.source}_${item.link}".hashCode().toLong()
    }

    override fun onViewRecycled(holder: MangaViewHolder)
    {
        super.onViewRecycled(holder)
        //        Glide.with(holder.itemView.context).clear(holder.itemView.ivManga)
        //        holder.recycleImage()
    }

    /***
     * This function returns the adapter item specified by its position.
     *
     * @param position
     * @return
     */
    fun getItem(position: Int): Manga
    {
        return filteredData[position]
    }

    /***
     * This function updates the adapter original data, and notifies the adapter it needs to update views.
     *
     * @param mangaList
     */
    fun replaceOriginalData(mangaList: List<Manga>)
    {
        data = ArrayList(mangaList)
        filteredData = ArrayList(mangaList)
        filter.filter(filter.queryFilter)
        notifyDataSetChanged()
    }

    fun updateUnsortedData(mangaList: List<Manga>)
    {
        mangaList.forEach {
            val index = filteredData.indexOf(it)
            val old = filteredData[index]
            if (old.followType != it.followType)
            {
                filteredData[index] = it
                notifyItemChanged(index)
            }
        }

        data = filteredData
    }

    /***
     * This function notifies the adapter that a manga object has been interacted with and needs to be updated in case its state has changed.
     *
     * @param manga
     */
    fun updateItem(manga: Manga) = ioScope.launch {
        val filterPosition = filteredData.indexOf(manga)
        val dataPosition = data.indexOf(manga)

        if (dataPosition >= 0)
        {
            data[dataPosition] = manga
        }

        if (filterPosition >= 0)
        {
            uiScope.launch {
                filteredData[filterPosition] = manga
                notifyItemChanged(filterPosition)
            }
        }

        if (isLibrary && dataPosition >= 0 && manga.followType == FollowType.Unfollow)
        {
            if (dataPosition < 0 && dataPosition > data.size) return@launch
            uiScope.launch {
                data.removeAt(dataPosition)
                filteredData.removeAt(filterPosition)
                notifyItemRemoved(filterPosition)
            }
        }
        else if (isLibrary && dataPosition < 0 && manga.followType != FollowType.Unfollow)
        {
            uiScope.launch {
                data.add(manga)
                data.sortBy { it.name }
                filteredData = ArrayList(data)
                notifyDataSetChanged()
            }
        }
    }

    /***
     * This function notifies the adapter that a manga object has been interacted with and needs to be updated, this function is used for items
     * in the Library fragment.
     *
     * @param manga
     * @param isAddingFlag
     */
    fun updateItem(manga: Manga, isAddingFlag: Boolean) = ioScope.launch {
        if (isAddingFlag && !data.contains(manga))
        {
            data.add(manga)
            filteredData.add(manga)
            filteredData.sortedBy { manga -> manga.name }

        }
        else if (!isAddingFlag && data.contains(manga))
        {
            data.remove(manga)
            filteredData.remove(manga)
        }

        uiScope.launch { notifyDataSetChanged() }
    }

    /***
     * This function causes the adapter filter to initiate.
     *
     * @param aQuery the text query used as a filter.
     */
    fun performTextFilter(aQuery: String) = ioScope.launch {
        filter.filter(aQuery)
    }

    /***
     * This function causes the adapter filter to initiate.
     *
     * @param filterType item status used as a filter.
     */
    fun filterByStatus(filterType: MangaEnums.FilterType) = ioScope.launch {
        filter.filterByStatus(filterType)
    }

    inner class MangaViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)
    {
        fun onBind(position: Int)
        {
            val manga = filteredData[position]
            val status = manga.followType.value

            val bgColor = itemView.context.resources.getColor(backGroundFactory(status))
            val textColor = itemView.context.resources.getColor(textColorFactory(status))

            itemView.tvMangaTitle.setBackgroundColor(bgColor)
            itemView.tvMangaTitle.setTextColor(textColor)
            itemView.tvMangaTitle.text = manga.name

            loadImage(manga)

            itemView.clParent.setOnClickListener {
                itemSelected.invoke(filteredData[position])
            }

            // Add margin to bottom elements
            val params = itemView.cvParent.layoutParams as androidx.recyclerview.widget.GridLayoutManager.LayoutParams

            var rows = data.size / 3
            if (data.size % 3 == 0) rows--
            if (position >= rows * 3) params.bottomMargin = ScreenUtil.dpToPx(itemView.context, 40)
            else params.bottomMargin = 0

            itemView.cvParent.layoutParams = params
        }

        private fun loadImage(manga: Manga)
        {
            itemView.ivManga.scaleType = ImageView.ScaleType.CENTER

            val imageUrl = manga.image

            if (imageUrl.isEmpty())
            {
                itemView.ivManga.setImageResource(R.drawable.manga_error)
                Glide.with(itemView.context).clear(itemView.ivManga)
                return
            }

            val lOptions = RequestOptions()
                    .fitCenter()
                    .placeholder(itemView.context.resources.getDrawable(R.drawable.manga_loading_image))
                    .error(itemView.context.resources.getDrawable(R.drawable.manga_error))
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .timeout(8000)


            val builder = LazyHeaders.Builder().addHeader("User-Agent", NetworkService.defaultUserAgent)

            CloudFlareService().getCFCookies(imageUrl, NetworkService.defaultUserAgent) { cookies ->
                for (cookie in cookies) builder.addHeader("Cookie", cookie)
                val glideUrl = GlideUrl(imageUrl, builder.build())

                uiScope.launch {
                    Glide.with(itemView.context)
                            .asBitmap()
                            .load(glideUrl)
                            .apply(lOptions)
                            .transition(GenericTransitionOptions<Any>().transition(android.R.anim.fade_in))
                            .into(ScaleImageViewTarget(itemView.ivManga))
                }

            }
        }

        private fun backGroundFactory(status: Int): Int = when (FollowType.getTypeFromValue(status))
        {
            FollowType.Reading -> R.color.colorPrimary
            FollowType.Completed -> R.color.manga_green
            FollowType.On_Hold -> R.color.manga_red
            FollowType.Plan_to_Read -> R.color.manga_gray
            else ->
            {
                val attrs = intArrayOf(R.attr.background_color)
                val ta = itemView.context.obtainStyledAttributes(attrs)
                val color = ta.getResourceId(0, android.R.color.black)
                ta.recycle()
                color
            }
        }

        private fun textColorFactory(status: Int): Int = when (status)
        {
            1, 2, 3, 4 -> R.color.manga_white
            else ->
            {
                val attrs = intArrayOf(R.attr.text_color)
                val ta = itemView.context.obtainStyledAttributes(attrs)
                val color = ta.getResourceId(0, android.R.color.black)
                ta.recycle()
                color
            }
        }


        //        fun recycleImage()
        //        {
        //
        //            //            Glide.with(itemView.getContext()).clear(mBackgroundImage);
        //            mImage!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
        //        }
    }

    /***
     * This private class filters the adapter items based on a text query search, as well as user selected item status filter.
     *
     */
    inner class TextFilter : Filter()
    {
        var queryFilter: CharSequence = ""
        var statusFilter: MangaEnums.FilterType = MangaEnums.FilterType.NONE

        override fun performFiltering(query: CharSequence): FilterResults
        {
            queryFilter = query.toString()

            val result = arrayListOf<Manga>()

            data.forEach { manga ->
                if (manga.name.contains(queryFilter, true)) result.add(manga)
                else if (manga.alternateNames.contains(queryFilter, true)) result.add(manga)
            }

            if (statusFilter != MangaEnums.FilterType.NONE)
            {
                // Remove items that do not fit the status set status filter
                result.forEach { manga ->
                    when (statusFilter)
                    {
                        MangaEnums.FilterType.FOLLOWING -> if (manga.followType.value > 0) result.remove(manga)
                        else -> if (manga.followType.value != statusFilter.value) result.remove(manga)
                    }
                }
            }

            return FilterResults().apply {
                values = result
                count = result.size
            }
        }

        override fun publishResults(aFilterText: CharSequence, aFilterResult: FilterResults)
        {
            val newData = aFilterResult.values as ArrayList<Manga>
            if (newData != filteredData)
            {
                filteredData = newData
                uiScope.launch { notifyDataSetChanged() }
            }
        }

        /***
         * This functions sets the item status the filter will use.
         *
         * @param aFilterType
         */
        fun filterByStatus(aFilterType: MangaEnums.FilterType)
        {
            statusFilter = aFilterType
            filter(queryFilter)
        }


    }

    companion object
    {
        val TAG = MangaAdapter::class.java.simpleName
    }
}
