package com.amgregoire.mangafeed.Common;

import android.graphics.Rect;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Created by Andy Gregoire on 3/9/2018.
 */

public class RecyclerViewSpaceDecoration extends RecyclerView.ItemDecoration
{
    private int mSpacing;

    public RecyclerViewSpaceDecoration(int space)
    {
        mSpacing = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        int position = parent.getChildViewHolder(view).getAdapterPosition();
        int itemCount = state.getItemCount();
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        setSpacingForDirection(outRect, layoutManager, position, itemCount);
    }

    private void setSpacingForDirection(Rect outRect, RecyclerView.LayoutManager layoutManager, int position, int itemCount)
    {

        if (layoutManager instanceof GridLayoutManager)
        {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            int cols = gridLayoutManager.getSpanCount();
            int rows = itemCount / cols;

            outRect.left = mSpacing;
            outRect.right = position % cols == cols - 1 ? mSpacing : 0;
            outRect.top = mSpacing;
            outRect.bottom = position / cols == rows ? mSpacing : 0;
        }
        else if (layoutManager instanceof LinearLayoutManager)
        {
            outRect.left = mSpacing;
            outRect.right = mSpacing;
            outRect.top = mSpacing;
            outRect.bottom = position == itemCount - 1 ? mSpacing : 0;
        }
        else
        {
            outRect.left = mSpacing;
            outRect.right = position == itemCount - 1 ? mSpacing : 0;
            outRect.top = mSpacing;
            outRect.bottom = mSpacing;
        }
    }
}
