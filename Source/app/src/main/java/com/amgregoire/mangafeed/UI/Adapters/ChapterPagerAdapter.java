package com.amgregoire.mangafeed.UI.Adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.Models.DbChapter;
import com.amgregoire.mangafeed.Models.DbManga;
import com.amgregoire.mangafeed.UI.Fragments.ReaderFragmentChapter;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

public class ChapterPagerAdapter extends FragmentStatePagerAdapter
{
    public final static String TAG = ChapterPagerAdapter.class.getSimpleName();

    private ArrayList<DbChapter> mDbChapterList;
    private SparseArray<WeakReference<Fragment>> mPageReferenceMap = new SparseArray<>();

    private boolean mParentFollowing;
    private DbManga mDbManga;
    public ChapterPagerAdapter(FragmentManager manager, List<DbChapter> dbChapterList, boolean isParentFollowing, DbManga dbManga)
    {
        super(manager);
        mDbChapterList = new ArrayList<>(dbChapterList);
        mParentFollowing = isParentFollowing;
        mDbManga = dbManga;
    }

    @Override
    public Fragment getItem(int position)
    {
        try
        {
            WeakReference<Fragment> lWeakReference = mPageReferenceMap.get(position);

            if (lWeakReference != null)
            {
                return lWeakReference.get();
            }
            else
            {
                Fragment lChapterFragment = ReaderFragmentChapter.newInstance(mParentFollowing, position, mDbManga);
                mPageReferenceMap.put(position, new WeakReference<>(lChapterFragment));

                return lChapterFragment;
            }
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, "Fragment null", ex.getMessage());
            return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup aContainer, int aPosition)
    {
        String lMethod = Thread.currentThread().getStackTrace()[2].getMethodName();

        Fragment lFragment = null;
        try
        {
            lFragment = (ReaderFragmentChapter) super.instantiateItem(aContainer, aPosition);
            mPageReferenceMap.put(aPosition, new WeakReference<>(lFragment));
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, lMethod, ex.getMessage());
        }

        return lFragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        super.destroyItem(container, position, object);
        mPageReferenceMap.remove(position);
    }

    @Override
    public int getCount()
    {
        return mDbChapterList.size();
    }

}
