package com.amgregoire.mangafeed.v2.ui.read

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment

class ChapterGridFragment : BaseFragment()
{

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        self = inflater.inflate(R.layout.fragment_chapter_grid, container)
        return self
    }

    override fun onStart()
    {
        super.onStart()

        // setup adapter?
//        setExitSharedElementCallback(object : SharedElementCallback()
//        {
//            override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?)
//            {
//                names ?: return
//                sharedElements ?: return
//
//                val POSITION = 0 // get position from adapter (by selection or some interaction)
//                val selectedViewHolder = self.rvChapterGrid.findViewHolderForAdapterPosition(POSITION)
//
//                if (selectedViewHolder?.itemView == null) return
//
//                // Map the first shared element name to the child ImageView.
//                sharedElements[names[0]] = selectedViewHolder.itemView.findViewById(R.id.card_image)
//            }
//        })
    }
}