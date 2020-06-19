package com.amgregoire.mangafeed.v2.ui.read.adapter

import android.graphics.Bitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.service.ReadService
import com.amgregoire.mangafeed.v2.ui.read.ImageFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ImageAdapter(parent: Fragment) : FragmentStatePagerAdapter(parent.childFragmentManager) {

    var items: ArrayList<Item> = arrayListOf()
    var urlFragments = hashMapOf<Int, ImageFragment>()
//    var bitmapFragments = hashMapOf<Int, ImageFragment>()

    constructor(parent: Fragment, bitmaps: List<Item>):this(parent)
    {
        bitmaps.forEach { items.add(it) }
    }

    constructor(parent: Fragment,urls: List<String>, onDataChanged:(List<Item>)->Unit):this(parent)
    {
        ioScope.launch {
            val map = HashMap<Int, List<Item>>()
            urls.forEachIndexed { index, url ->
                ReadService.urlToBitmaps(url, parent.context) { bitmaps ->
                    map.put(index, bitmaps)
                }
            }

            while (map.keys.size < urls.size) {
                delay(500)
                Logger.error("KeyCount=${map.keys.size} ==> ${urls.size}")
            }

            uiScope.launch {
                items.clear()
                urlFragments.clear()
                map.toSortedMap().forEach { it.value.forEach { items.add(it) } }
                notifyDataSetChanged()
                onDataChanged(items)
            }
        }
    }



    override fun getItem(position: Int): Fragment {
        return when (val item = items[position]) {
            is Item.Url -> {
                val frag = urlFragments.getOrDefault(position, ImageFragment.newInstance(item.value))
                urlFragments[position] = frag
                frag
            }
            is Item.Bitmap -> {
                val frag = urlFragments.getOrDefault(position, ImageFragment.newInstance(position))
                urlFragments[position] = frag
                frag
            }
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
        class Bitmap(val value: android.graphics.Bitmap) : Item()
    }
}