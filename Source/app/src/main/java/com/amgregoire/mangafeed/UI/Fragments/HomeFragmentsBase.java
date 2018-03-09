package com.amgregoire.mangafeed.UI.Fragments;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;

import com.amgregoire.mangafeed.Common.RecyclerViewSpaceDecoration;
import com.amgregoire.mangafeed.UI.Mappers.IHome;
import com.amgregoire.mangafeed.R;
import com.l4digital.fastscroll.FastScrollRecyclerView;

import butterknife.BindView;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public abstract class HomeFragmentsBase extends Fragment implements IHome.HomeBaseMap
{

    @BindView(R.id.recyclerViewHomeManga) FastScrollRecyclerView mRecyclerView;

    protected IHome.HomeBasePres mPresenter;


    @Override
    public void initViews() {}

    @Override
    public void startRefresh() {}

    @Override
    public void stopRefresh() {}

    @Override
    public void registerAdapter(RecyclerView.Adapter adapter, RecyclerView.LayoutManager manager, boolean needSpacing)
    {
        if(needSpacing)
        {
            mRecyclerView.addItemDecoration(new RecyclerViewSpaceDecoration(20));
            mRecyclerView.getItemAnimator().setChangeDuration(0);
        }
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);
    }
}
