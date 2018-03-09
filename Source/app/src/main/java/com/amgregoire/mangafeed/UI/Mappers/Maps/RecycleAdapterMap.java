package com.amgregoire.mangafeed.UI.Mappers.Maps;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public interface RecycleAdapterMap
{
    void registerAdapter(RecyclerView.Adapter adapter, RecyclerView.LayoutManager manager, boolean needsSpacing);
}
