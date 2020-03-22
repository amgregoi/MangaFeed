package com.amgregoire.mangafeed.v2.ui.read

import android.util.SparseArray
import android.view.ViewGroup
import com.amgregoire.mangafeed.Models.DbChapter
import com.amgregoire.mangafeed.v2.model.domain.Manga
import java.lang.ref.WeakReference

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

class ChapterPagerAdapter(
        private val manager: androidx.fragment.app.FragmentManager,
        private val dbChapterList: List<DbChapter>,
        private val isFollowing: Boolean,
        private val manga: Manga
) : androidx.fragment.app.FragmentStatePagerAdapter(manager)
{
    private val mFragmentRefs = SparseArray<WeakReference<ChapterFragment>>()

    override fun getItem(position: Int): androidx.fragment.app.Fragment?
    {
        val reference = mFragmentRefs.get(position)

        return if (reference != null)
        {
            reference.get()
        }
        else
        {
            val chapterFragment = ChapterFragment.newInstance(isFollowing, position)
            mFragmentRefs.put(position, WeakReference(chapterFragment))
            chapterFragment
        }
    }

    fun updateItemAt(position: Int)
    {
         mFragmentRefs.get(position)?.get()?.refreshAllPages()
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any)
    {
        super.destroyItem(container, position, `object`)
        mFragmentRefs.remove(position)
    }

    override fun getCount(): Int
    {
        return dbChapterList.size
    }
}
