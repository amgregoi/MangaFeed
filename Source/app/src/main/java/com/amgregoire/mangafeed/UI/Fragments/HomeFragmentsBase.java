package com.amgregoire.mangafeed.UI.Fragments;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.amgregoire.mangafeed.Common.RecyclerViewSpaceDecoration;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Mappers.IHome;
import com.l4digital.fastscroll.FastScrollRecyclerView;

import butterknife.BindView;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public abstract class HomeFragmentsBase extends Fragment implements IHome.HomeBaseMap
{

    @BindView(R.id.recyclerViewHomeManga) FastScrollRecyclerView mRecyclerView;

    protected IHome.HomeBasePres mPresenter;
    private RecyclerViewSpaceDecoration mSpaceDecor;

    @Override
    public void initViews() {}

    @Override
    public void startRefresh() {}

    @Override
    public void stopRefresh() {}

    @Override
    public void registerAdapter(RecyclerView.Adapter adapter, RecyclerView.LayoutManager manager)
    {
        if(mSpaceDecor == null)
        {
            mSpaceDecor = new RecyclerViewSpaceDecoration(20);
        }
        else
        {
            mRecyclerView.removeItemDecoration(mSpaceDecor);
        }

        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mRecyclerView.addItemDecoration(mSpaceDecor);
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mPresenter.onResume();
    }
}
