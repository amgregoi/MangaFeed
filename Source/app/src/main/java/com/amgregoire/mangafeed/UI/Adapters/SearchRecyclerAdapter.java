package com.amgregoire.mangafeed.UI.Adapters;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.DbManga;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.Utils.BusEvents.MangaSelectedEvent;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class SearchRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public final static String TAG = SearchRecyclerAdapter.class.getSimpleName();

    private ArrayList<DbManga> mOriginalData = null;
    private ArrayList<DbManga> mFilteredData = null;
    private TextFilter mFilter = new TextFilter();

    private boolean mIsOfflineFlag = false;

    public SearchRecyclerAdapter(List<DbManga> data)
    {
        mOriginalData = new ArrayList<>(data);
        mFilteredData = new ArrayList<>(data);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View lView = LayoutInflater.from(parent.getContext())
                                   .inflate(R.layout.item_manga_search_recycler_adapter, parent, false);
        return new ViewHolderManga(lView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        DbManga lDbManga = mFilteredData.get(position);
        ViewHolderManga lHolder = (ViewHolderManga) holder;

        lHolder.setViews(lDbManga);
        lHolder.loadImage(lDbManga);
    }

    @Override
    public int getItemCount()
    {
        return mFilteredData.size();
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder)
    {
        super.onViewRecycled(holder);
        ((ViewHolderManga) holder).recycleImage();
    }

    /***
     * This function returns the adapter item specified by its position.
     *
     * @param position
     * @return
     */
    public DbManga getItem(int position)
    {
        return mFilteredData.get(position);
    }

    /***
     * This function updates the adapter original data, and notifies the adapter it needs to update views.
     *
     * @param dbMangaList
     */
    public void updateOriginalData(List<DbManga> dbMangaList)
    {
        mOriginalData = new ArrayList<>(dbMangaList);
        mFilteredData = new ArrayList<>(dbMangaList);
        notifyDataSetChanged();
    }

    /***
     * This function notifies the adapter that a manga object has been interacted with and needs to be updated in case its state has changed.
     *
     * @param aDbManga
     */
    public void updateItem(DbManga aDbManga)
    {
        if (aDbManga == null)
        {
            return;
        }

        int lFilterPos = mFilteredData.indexOf(aDbManga);
        int lOriginalPos = mOriginalData.indexOf(aDbManga);

        if (lOriginalPos >= 0)
        {
            mOriginalData.set(lOriginalPos, aDbManga);
        }

        if (lFilterPos >= 0)
        {
            mFilteredData.set(lFilterPos, aDbManga);

            notifyItemChanged(lFilterPos);
        }
    }

    /***
     * This function notifies the adapter that a manga object has been interacted with and needs to be updated, this function is used for items
     * in the Library fragment.
     *
     * @param aDbManga
     * @param isAddingFlag
     */
    public void updateItem(DbManga aDbManga, boolean isAddingFlag)
    {
        if (isAddingFlag && !mOriginalData.contains(aDbManga))
        {
            mOriginalData.add(aDbManga);
            mFilteredData.add(aDbManga);
            Collections.sort(mFilteredData, (emp1, emp2) -> emp1.getTitle()
                                                                .compareToIgnoreCase(emp2.getTitle()));
            Collections.sort(mOriginalData, (emp1, emp2) -> emp1.getTitle()
                                                                .compareToIgnoreCase(emp2.getTitle()));
        }
        else if (!isAddingFlag && mOriginalData.contains(aDbManga))
        {
            mOriginalData.remove(aDbManga);
            mFilteredData.remove(aDbManga);
        }

        notifyDataSetChanged();
    }

    public void setOffline()
    {
        mIsOfflineFlag = true;
    }

    /***
     * This function causes the adapter filter to initiate.
     *
     * @param aQuery the text query used as a filter.
     */
    public void performTextFilter(String aQuery)
    {
        mFilter.filter(aQuery);
    }

    /***
     * This function causes the adapter filter to initiate.
     *
     * @param filterType item status used as a filter.
     */
    public void filterByStatus(MangaEnums.FilterType filterType)
    {
        mFilter.filterByStatus(filterType);
        mFilter.filter(mFilter.mLastQuery);
        notifyDataSetChanged();
    }

    /***
     * This class acts as the view holder for items in the adapter.
     *
     */
    public class ViewHolderManga extends RecyclerView.ViewHolder
    {
        @BindView(R.id.textViewMangaGridItemTitle) TextView mTitle;
        @BindView(R.id.imageViewMangaGridItem) ImageView mImage;
        @BindView(R.id.linearLayoutMangaGridItemFooter) LinearLayout mFooter;

        @BindColor(R.color.manga_white) int mWhite;
        @BindColor(R.color.colorPrimary) int mPrimary;
        @BindColor(R.color.manga_black) int mBlack;
        @BindColor(R.color.manga_red) int mRed;
        @BindColor(R.color.manga_green) int mGreen;
        @BindColor(R.color.manga_gray) int mGray;

        @BindDrawable(R.drawable.manga_loading_image) Drawable mPlaceHolder;
        @BindDrawable(R.drawable.manga_error) Drawable mError;

        private final Target mImageTarget = new Target()
        {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
            {
                mImage.setImageBitmap(bitmap);
                mImage.setScaleType(ImageView.ScaleType.FIT_XY);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable)
            {
                mImage.setImageDrawable(errorDrawable);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable)
            {
                mImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                mImage.setImageDrawable(placeHolderDrawable);
            }
        };

        public ViewHolderManga(View view)
        {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.cardViewMangaGridItem)
        public void onCardItemClick()
        {
            MangaFeed.Companion.getApp()
                     .rxBus()
                     .send(new MangaSelectedEvent(mFilteredData.get(getLayoutPosition()), mIsOfflineFlag));
        }

        /***
         * this function sets the item views to their correct state.
         *
         * @param dbManga
         */
        public void setViews(DbManga dbManga)
        {
            int status = dbManga.getFollowing();

            mFooter.setBackgroundColor(backGroundFactory(status));
            mTitle.setBackgroundColor(backGroundFactory(status));
            mTitle.setTextColor(textColorFactory(status));
            mTitle.setText(dbManga.toString());
        }

        /***
         * This function loads the image for the adapter item.
         *
         * @param dbManga
         */
        public void loadImage(DbManga dbManga)
        {
            if (dbManga.getImage() != null && !dbManga.getImage().isEmpty())
            {
                Picasso.get().load(dbManga.getImage())
                       .error(mError)
                       .placeholder(mPlaceHolder)
                       .resize(200, 400) // resize image before placing in target to fix laggy scrolling
                       .into(mImageTarget);
            }
            else
            {
                Picasso.get().load(R.drawable.manga_error).into(mImage);
            }
        }

        /***
         * This function recycles the glide references when an item is being recycled by the adapter.
         *
         */
        public void recycleImage()
        {

//            Glide.with(itemView.getContext()).clear(mBackgroundImage);
            mImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        /***
         * This function returns the background color based on the adapter items followed status.
         *
         * @param status
         * @return
         */
        private int backGroundFactory(int status)
        {
            switch (status)
            {
                case DbManga.FOLLOW_READING:
                    return mPrimary;
                case DbManga.FOLLOW_COMPLETE:
                    return mGreen;
                case DbManga.FOLLOW_ON_HOLD:
                    return mRed;
                case DbManga.FOLLOW_PLAN_TO_READ:
                    return mGray;
                default:
                    return mWhite;
            }
        }

        /***
         * This function returns the text color based on the adapter items followed status.
         *
         * @param status
         * @return
         */
        private int textColorFactory(int status)
        {
            switch (status)
            {
                case 1:
                case 2:
                case 3:
                case 4:
                    return mWhite;
                default:
                    return mBlack;
            }
        }
    }

    /***
     * This private class filters the adapter items based on a text query search, as well as user selected item status filter.
     *
     */
    public class TextFilter extends Filter
    {
        private CharSequence mLastQuery = "";
        private MangaEnums.FilterType mLastFilter = MangaEnums.FilterType.NONE;

        @Override
        protected FilterResults performFiltering(CharSequence aFilterText)
        {

            String lFilter = aFilterText.toString().toLowerCase();
            FilterResults lResult = new FilterResults();

            final ArrayList<DbManga> lBaseData = mOriginalData;

            int lCount = lBaseData.size();
            final ArrayList<DbManga> lFilteredList = new ArrayList<>(lCount);

            String lFilterableString;
            DbManga lDbManga;
            for (int iIndex = 0; iIndex < lCount; iIndex++)
            {
                lDbManga = lBaseData.get(iIndex);

                //Filter by Title and Alternate titles
                lFilterableString = lDbManga.toString();
                if (lDbManga.getAlternate() != null)
                {
                    lFilterableString += ", " + lBaseData.get(iIndex).getAlternate();
                }

                //Filter by item status
                if (lFilterableString.toLowerCase().contains(lFilter))
                {
                    //Filter Type NONE
                    if (mLastFilter == MangaEnums.FilterType.NONE)
                    {
                        lFilteredList.add(lBaseData.get(iIndex));
                    }
                    //Filter TYPE READING, COMPLETE, AND ON_HOLD
                    else if (mLastFilter == MangaEnums.FilterType.FOLLOWING)
                    {
                        if (lDbManga.getFollowing() > 0)
                        {
                            lFilteredList.add(lBaseData.get(iIndex));
                        }
                    }
                    //Filter Type SPECIFIC
                    else if (lDbManga.getFollowing() == mLastFilter.getValue())
                    {
                        lFilteredList.add(lBaseData.get(iIndex));
                    }
                }
            }

            lResult.values = lFilteredList;
            lResult.count = lFilteredList.size();

            mLastQuery = aFilterText.toString();
            return lResult;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence aFilterText, FilterResults aFilterResult)
        {
            mFilteredData = (ArrayList<DbManga>) aFilterResult.values;
            notifyDataSetChanged();
        }

        /***
         * This functions sets the item status the filter will use.
         *
         * @param aFilterType
         */
        public void filterByStatus(MangaEnums.FilterType aFilterType)
        {
            mLastFilter = aFilterType;
        }


    }

}
