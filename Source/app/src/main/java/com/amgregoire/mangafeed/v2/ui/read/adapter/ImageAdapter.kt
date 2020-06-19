package com.amgregoire.mangafeed.v2.ui.read.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.amgregoire.mangafeed.v2.ui.read.ImageFragment

class ImageAdapter(parent: Fragment) : FragmentStatePagerAdapter(parent.childFragmentManager) {

    var items: ArrayList<Item> = arrayListOf()
    var urlFragments = hashMapOf<Int, ImageFragment>()

    constructor(parent: Fragment, urls: List<String>) : this(parent) {
        urls.forEach { items.add(Item.Url(it)) }
    }


    override fun getItem(position: Int): Fragment {
        return when (val item = items[position]) {
            is Item.Url -> {
                val frag = urlFragments.getOrDefault(position, ImageFragment.newInstance(item.value))
                urlFragments[position] = frag
                frag
            }
            //            is Item.Bitmap -> {
            //                val frag = urlFragments.getOrDefault(position, ImageFragment.newInstance(position))
            //                urlFragments[position] = frag
            //                frag
            //            }
        }


    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    sealed class Item {
        class Url(val value: String) : Item()
        //        class Bitmap(val value: android.graphics.Bitmap) : Item()
    }
}