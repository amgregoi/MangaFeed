package com.amgregoire.mangafeed.v2.ui.read

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import android.util.SparseArray
import android.view.ViewGroup
import com.amgregoire.mangafeed.Models.Chapter
import com.amgregoire.mangafeed.Models.Manga
import java.lang.ref.WeakReference

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

class ChapterPagerAdapter(
        private val manager: androidx.fragment.app.FragmentManager,
        private val chapterList: List<Chapter>,
        private val isFollowing: Boolean,
        private val manga: Manga
) : androidx.fragment.app.FragmentStatePagerAdapter(manager)
{
    private val mFragmentRefs = SparseArray<WeakReference<androidx.fragment.app.Fragment>>()

    override fun getItem(position: Int): androidx.fragment.app.Fragment?
    {
        val reference = mFragmentRefs.get(position)

        return if (reference != null)
        {
            reference.get()
        }
        else
        {
            val chapterFragment = ChapterFragment.newInstance(isFollowing, position, manga)
            mFragmentRefs.put(position, WeakReference(chapterFragment))
            chapterFragment
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any)
    {
        super.destroyItem(container, position, `object`)
        mFragmentRefs.remove(position)
    }

    override fun getCount(): Int
    {
        return chapterList.size
    }
}
