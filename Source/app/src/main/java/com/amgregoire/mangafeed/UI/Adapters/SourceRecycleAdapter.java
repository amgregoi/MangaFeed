package com.amgregoire.mangafeed.UI.Adapters;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateSourceEvent;
import com.amgregoire.mangafeed.Utils.SharedPrefs;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class SourceRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public final static String TAG = SourceRecycleAdapter.class.getSimpleName();


    private ArrayList<MangaEnums.Source> mOriginalData = null;

    public SourceRecycleAdapter(List<MangaEnums.Source> data)
    {
        mOriginalData = new ArrayList<>(data);
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View lView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_source_adapter, parent, false);
        return new ViewHolderManga(lView, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        ViewHolderManga lHolder = (ViewHolderManga) holder;
        lHolder.setViews();
    }

    @Override
    public int getItemCount()
    {
        return mOriginalData.size();
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    /***
     * This class acts as the view holder for items in the adapter.
     *
     */
    public class ViewHolderManga extends RecyclerView.ViewHolder
    {
        @BindView(R.id.textViewSourceName) TextView mSourceName;
        @BindView(R.id.imageViewSourceBackground) ImageView mBackGround;
        @BindView(R.id.textViewSourceUrl) TextView mLink;

        @BindColor(R.color.manga_white) int mWhite;
        @BindColor(R.color.colorAccent) int mAccent;

        @BindDrawable(R.drawable.funmanga_logo) Drawable mFunMangaLogo;
        @BindDrawable(R.drawable.mangaeden_logo) Drawable mMangaEdenLogo;
        @BindDrawable(R.drawable.mangahere_logo) Drawable mMangaHereLogo;
        @BindDrawable(R.drawable.readlight_logo) Drawable mReadLightLogo;

        private MangaEnums.Source mSource;

        public ViewHolderManga(View view, int pos)
        {
            super(view);
            ButterKnife.bind(this, view);

            mSource = mOriginalData.get(pos);

            mSourceName.setText(mSource.name());

            switch (mSource)
            {
                case FunManga:
                    mBackGround.setImageDrawable(mFunMangaLogo);
                    mLink.setText("http://www.funmanga.com/");
                    break;
                case MangaEden:
                    mBackGround.setImageDrawable(mMangaEdenLogo);
                    mLink.setText("https://www.mangaeden.com/eng/");
                    break;
                case MangaHere:
                    mBackGround.setImageDrawable(mMangaHereLogo);
                    mLink.setText("http://www.mangahere.cc/");
                    break;
                case ReadLight:
                    mBackGround.setImageDrawable(mReadLightLogo);
                    mLink.setText("https://www.readlightnovel.org/");
                    break;
            }
        }

        @OnClick(R.id.cardViewSourceItemRoot)
        public void onCardItemClick()
        {
            SharedPrefs.setSavedSource(mSource.name());
            notifyDataSetChanged();
            MangaFeed.getInstance().rxBus().send(new UpdateSourceEvent());
        }

        /***
         * this function sets the item views to their correct state.
         *
         */
        public void setViews()
        {
            if(mSource == MangaFeed.getInstance().getCurrentSource().getCurrentSource())
            {
                mSourceName.setTextColor(mAccent);
                mLink.setTextColor(mAccent);
            }
            else
            {
                mSourceName.setTextColor(mWhite);
                mLink.setTextColor(mWhite);
            }
        }
    }

}
