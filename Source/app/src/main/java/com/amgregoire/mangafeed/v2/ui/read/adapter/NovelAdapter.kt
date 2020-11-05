package com.amgregoire.mangafeed.v2.ui.read.adapter

import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.extension.visible
import com.amgregoire.mangafeed.v2.widget.GestureViewPager
import kotlinx.android.synthetic.main.item_reader_image_adapter.view.*

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

class NovelAdapter(
        var data: List<String>,
        private val listener: GestureViewPager.UserGestureListener
) : RecyclerView.Adapter<NovelAdapter.NovelVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NovelVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reader_image_adapter, parent, false)
        return NovelVH(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: NovelVH, position: Int) {
        holder.onBind(position)
    }
    
    inner class NovelVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(position: Int) {
            itemView.gestureTextViewReaderChapter.visible()
            itemView.scrollViewTextContainer.visible()

            itemView.gestureTextViewReaderChapter.setUserGestureListener(listener)

            val lContent = data[position].replace("</p>", "</p><br>")
            itemView.gestureTextViewReaderChapter.text =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(lContent, Html.FROM_HTML_MODE_COMPACT) as CharSequence?
                    else Html.fromHtml(lContent)


            itemView.gestureTextViewReaderChapter.setUserGestureListener(listener)
        }
    }

    companion object {
        val TAG: String = NovelAdapter::class.java.simpleName
    }
}
