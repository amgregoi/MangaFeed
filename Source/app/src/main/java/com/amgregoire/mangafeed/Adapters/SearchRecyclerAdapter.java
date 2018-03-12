package com.amgregoire.mangafeed.Adapters;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateItemEvent;
import com.l4digital.fastscroll.FastScroller;
import com.squareup.picasso.MemoryPolicy;
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

public class SearchRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScroller.SectionIndexer
{
    public final static String TAG = SearchRecyclerAdapter.class.getSimpleName();

    private ArrayList<Manga> mOriginalData = null;
    private ArrayList<Manga> mFilteredData = null;
    private TextFilter mFilter = new TextFilter();

    public SearchRecyclerAdapter(List<Manga> data)
    {
        mOriginalData = new ArrayList<>(data);
        mFilteredData = new ArrayList<>(data);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View lView = LayoutInflater.from(parent.getContext())
                                   .inflate(R.layout.manga_grid_item, parent, false);
        return new ViewHolderManga(lView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        Manga lManga = mFilteredData.get(position);
        ViewHolderManga lHolder = (ViewHolderManga) holder;

        lHolder.setViews(lManga);
        lHolder.loadImage(lManga);
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
    public Manga getItem(int position)
    {
        return mFilteredData.get(position);
    }

    /***
     * This function updates the adapter original data, and notifies the adapter it needs to update views.
     *
     * @param mangaList
     */
    public void updateOriginalData(List<Manga> mangaList)
    {
        mOriginalData = new ArrayList<>(mangaList);
        mFilteredData = new ArrayList<>(mangaList);
        notifyDataSetChanged();
    }

    /***
     * This function notifies the adapter that a manga object has been interacted with and needs to be updated in case its state has changed.
     *
     * @param aManga
     */
    public void updateItem(Manga aManga)
    {
        if (aManga == null)
        {
            return;
        }

        int lFilterPos = mFilteredData.indexOf(aManga);

        if (lFilterPos >= 0)
        {
            mOriginalData.set(mOriginalData.indexOf(aManga), aManga);
            mFilteredData.set(lFilterPos, aManga);

            notifyItemChanged(lFilterPos);
        }
    }

    /***
     * This function notifies the adapter that a manga object has been interacted with and needs to be updated, this function is used for items
     * in the Library fragment.
     *
     * @param aManga
     * @param isAddingFlag
     */
    public void updateItem(Manga aManga, boolean isAddingFlag)
    {
        if (isAddingFlag && !mOriginalData.contains(aManga))
        {
            mOriginalData.add(aManga);
            mFilteredData.add(aManga);
            Collections.sort(mFilteredData, (emp1, emp2) -> emp1.getTitle()
                                                                .compareToIgnoreCase(emp2.getTitle()));
            Collections.sort(mOriginalData, (emp1, emp2) -> emp1.getTitle()
                                                                .compareToIgnoreCase(emp2.getTitle()));
        }
        else if (!isAddingFlag && mOriginalData.contains(aManga))
        {
            mOriginalData.remove(aManga);
            mFilteredData.remove(aManga);
        }

        notifyDataSetChanged();
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
    public void filterByStatus(MangaEnums.FilterStatus filterType)
    {
        mFilter.filterByStatus(filterType);
        mFilter.filter(mFilter.mLastQuery);
        notifyDataSetChanged();
    }

    /***
     * This function returns the character shown in the fast scroller bubble
     *
     * @param position The current position in the recycler view
     * @return The character to the be displayed in the fast scroll bubble
     */
    @Override
    public String getSectionText(int position)
    {
        char lChar = mFilteredData.get(position).getTitle().charAt(0);
        if (!Character.isLetterOrDigit(lChar))
        {
            return "#";
        }
        return Character.toString(lChar);
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

        @BindColor(R.color.Manga_white) int mWhite;
        @BindColor(R.color.colorPrimary) int mPrimary;
        @BindColor(R.color.Manga_black) int mBlack;
        @BindColor(R.color.Manga_red) int mRed;
        @BindColor(R.color.Manga_green) int mGreen;

        @BindDrawable(R.drawable.intercom_input_gallery) Drawable mPlaceHolder;
        @BindDrawable(R.drawable.intercom_error) Drawable mError;

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

            // TODO : launch manga info fragment
            notifyItemChanged(getLayoutPosition());
            MangaFeed.getInstance()
                     .rxBus()
                     .send(new UpdateItemEvent(mFilteredData.get(getLayoutPosition())));
        }

        /***
         * this function sets the item views to their correct state.
         *
         * @param manga
         */
        public void setViews(Manga manga)
        {
            int status = manga.following;

            mFooter.setBackgroundColor(backGroundFactory(status));
            mTitle.setBackgroundColor(backGroundFactory(status));
            mTitle.setTextColor(textColorFactory(status));
            mTitle.setText(manga.toString());

        }

        /***
         * This function loads the image for the adapter item.
         *
         * @param manga
         */
        public void loadImage(Manga manga)
        {
            Picasso.get().load(manga.getPicUrl())
                   .error(mError)
                   .placeholder(mPlaceHolder)
                   .memoryPolicy(MemoryPolicy.NO_STORE)
                   .into(mImageTarget);

        }

        /***
         * This function recycles the glide references when an item is being recycled by the adapter.
         *
         */
        public void recycleImage()
        {
//            Glide.with(itemView.getContext()).clear(mImage);
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
                case 1:
                    return mPrimary;
                case 2:
                    return mGreen;
                case 3:
                    return mRed;
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
        private MangaEnums.FilterStatus mLastFilter = MangaEnums.FilterStatus.NONE;

        @Override
        protected FilterResults performFiltering(CharSequence aFilterText)
        {

            String lFilter = aFilterText.toString().toLowerCase();
            FilterResults lResult = new FilterResults();

            final ArrayList<Manga> lBaseData = mOriginalData;

            int lCount = lBaseData.size();
            final ArrayList<Manga> lFilteredList = new ArrayList<>(lCount);

            String lFilterableString;
            Manga lManga;
            for (int iIndex = 0; iIndex < lCount; iIndex++)
            {
                lManga = lBaseData.get(iIndex);

                //Filter by Title and Alternate titles
                lFilterableString = lManga.toString();
                if (lManga.getAlternate() != null)
                {
                    lFilterableString += ", " + lBaseData.get(iIndex).getAlternate();
                }

                //Filter by item status
                if (lFilterableString.toLowerCase().contains(lFilter))
                {
                    //Filter Type NONE
                    if (mLastFilter == MangaEnums.FilterStatus.NONE)
                    {
                        lFilteredList.add(lBaseData.get(iIndex));
                    }
                    //Filter TYPE READING, COMPLETE, AND ON_HOLD
                    else if (mLastFilter == MangaEnums.FilterStatus.FOLLOWING)
                    {
                        if (lManga.getFollowingValue() > 0)
                        {
                            lFilteredList.add(lBaseData.get(iIndex));
                        }
                    }
                    //Filter Type SPECIFIC
                    else if (lManga.getFollowingValue() == mLastFilter.getValue())
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
            mFilteredData = (ArrayList<Manga>) aFilterResult.values;
            notifyDataSetChanged();
        }

        /***
         * This functions sets the item status the filter will use.
         *
         * @param aFilterType
         */
        public void filterByStatus(MangaEnums.FilterStatus aFilterType)
        {
            mLastFilter = aFilterType;
        }


    }

}
