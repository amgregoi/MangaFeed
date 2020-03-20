package com.amgregoire.mangafeed.v2.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.Models.DbManga
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.service.Logger
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.item_sync_sources.view.*
import kotlinx.coroutines.launch

class SyncAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<SyncAdapter.SyncViewHolder>()
{
    val sources = MangaEnums.Source.values()

    private val compositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): SyncViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sync_sources, parent, false)
        return SyncViewHolder(view)
    }

    override fun getItemCount(): Int
    {
        return sources.size
    }

    override fun onBindViewHolder(holder: SyncViewHolder, position: Int)
    {
        holder.onBind(position)
    }

    fun stopSync()
    {
        compositeDisposable.clear()
        notifyDataSetChanged()
    }

    inner class SyncViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
    {
        fun onBind(position: Int)
        {
            val source = sources[position]
            itemView.tvLabel.text = source.name

            if(position == 0) itemView.separator1.visibility = View.VISIBLE
            else itemView.separator1.visibility = View.GONE

            itemView.setOnClickListener {
                itemView.checkbox.toggle()
            }
        }

        fun startSync(completeCallback: () -> Unit)
        {
            if (itemView.checkbox.isChecked)
            {
                val source = sources[adapterPosition].source
                itemView.tvCount.visibility = View.VISIBLE
                val mangaList = arrayListOf<DbManga>()

                ioScope.launch {
                    val disp = source.updateLocalCatalogV2()?.subscribe(
                            { status ->
                                mangaList.addAll(status.items)
                                uiScope.launch {
                                    val percent = status.current.toDouble() / status.total.toDouble() * 100
                                    itemView.tvCount.text = "New items: ${mangaList.size} • ${percent.toInt()}% complete"
                                }
                            },
                            {
                                Logger.error(it)
                                uiScope.launch {
                                    itemView.tvCount.text = "Oops, something went wrong"
                                    completeCallback.invoke()
                                }
                            },
                            {
                                uiScope.launch {
                                    completeCallback.invoke()
                                    itemView.tvCount.text = "Complete: Added ${mangaList.size} new items"
                                }
                            }
                    )

                    disp ?: return@launch
                    compositeDisposable.add(disp)
                }
            }
            else
            {
                completeCallback.invoke()
                itemView.tvCount.visibility = View.GONE
            }

        }
    }
}