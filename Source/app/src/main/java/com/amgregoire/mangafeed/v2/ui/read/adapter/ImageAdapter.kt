package com.amgregoire.mangafeed.v2.ui.read.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.amgregoire.mangafeed.v2.ui.read.ImageFragment

class ImageAdapter(manager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(manager, lifecycle) {

    var items: ArrayList<Item> = arrayListOf()
    var urlFragments = hashMapOf<Int, ImageFragment>()

    constructor(manager: FragmentManager, lifecycle: Lifecycle, urls: List<String>) : this(manager, lifecycle) {
        urls.forEach { items.add(Item.Url(it)) }
    }




    //    override fun getItem(position: Int): Fragment {
    //        return when (val item = items[position]) {
    //            is Item.Url -> {
    //                val frag = urlFragments.getOrDefault(position, ImageFragment.newInstance(item.value))
    //                urlFragments[position] = frag
    //                frag
    //            }
    //            //            is Item.Bitmap -> {
    //            //                val frag = urlFragments.getOrDefault(position, ImageFragment.newInstance(position))
    //            //                urlFragments[position] = frag
    //            //                frag
    //            //            }
    //        }
    //
    //
    //    }
    //
    //    override fun getCount(): Int {
    //        return items.size
    //    }
    //
    //    override fun getItemPosition(`object`: Any): Int {
    //        return PagerAdapter.POSITION_NONE
    //    }

    sealed class Item {
        class Url(val value: String) : Item()
        //        class Bitmap(val value: android.graphics.Bitmap) : Item()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (val item = items[position]) {
            is Item.Url -> {
                val frag = urlFragments.getOrDefault(position, ImageFragment.newInstance(item.value))
                urlFragments[position] = frag
                frag
            }
        }
    }


}