package com.amgregoire.mangafeed.Adapters;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.Utils.BusEvents.MangaDownloadSelectEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateItemEvent;
import com.amgregoire.mangafeed.Utils.SharedPrefs;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by Andy Gregoire on 3/12/2018.
 */

public class MangaInfoChaptersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public final static String TAG = MangaInfoChaptersAdapter.class.getSimpleName();
    public final static int VIEW_HEADER = 1;
    public final static int VIEW_CHAPTER = 2;

    private List<Chapter> mChapterData;
    private Manga mManga;
    private boolean mHasChapters = false;
    private boolean mDownloadViewFlag = false;

    public MangaInfoChaptersAdapter(List<Chapter> data, Manga manga)
    {
        if (data.size() > 0)
        {
            mHasChapters = true;
        }

        mChapterData = new ArrayList<>(data);
        mDownloadList = new ArrayList<>();
        mManga = manga;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (!mDownloadViewFlag)
        {
            if (position == 0)
            {
                return VIEW_HEADER;
            }
        }

        return VIEW_CHAPTER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater lInflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder lHolder;
        View lView;

        if (viewType == VIEW_HEADER)
        {
            lView = lInflater.inflate(R.layout.item_manga_info_header, parent, false);
            lHolder = new ViewHolderHeader(lView);
        }
        else
        {
            lView = lInflater.inflate(R.layout.item_manga_info_chapter, parent, false);
            lHolder = new ViewHolderChapter(lView);
        }

        return lHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        if (holder instanceof ViewHolderHeader)
        {
            ((ViewHolderHeader) holder).initViews();
        }
        else
        {
            ((ViewHolderChapter) holder).initViews(position - getHeaderCount());
        }
    }

    @Override
    public int getItemCount()
    {
        return mChapterData.size() + getHeaderCount();
    }

    private int getHeaderCount()
    {
        return mDownloadViewFlag ? 0 : 1;
    }


    public class ViewHolderHeader extends RecyclerView.ViewHolder
    {
        @BindView(R.id.relativeLayoutMangaInfoHeaderContainer) RelativeLayout mRoot;
        @BindView(R.id.textViewMangaInfoHeaderAlternates) TextView mAlternates;
        @BindView(R.id.textViewMangaInfoHeaderArtist) TextView mArtist;
        @BindView(R.id.textViewMangaInfoHeaderAuthor) TextView mAuthor;
        @BindView(R.id.textViewMangaInfoDescription) TextView mDescription;
        @BindView(R.id.textViewMangaInfoHeaderStatus) TextView mStatus;
        @BindView(R.id.textViewMangaInfoHeaderGenres) TextView mGenres;
        @BindView(R.id.textViewMangaInfoHeaderChapterLabel) TextView mChapterHeaderLabel;
        @BindView(R.id.textViewMangaInfoHeaderTitle) TextView mTitle;
        @BindView(R.id.imageViewMangaInfoHeader) ImageView mImage;

        // change continue reading to button instead of menu
        @BindView(R.id.famMangaInfoHeaderContinueReading) FloatingActionButton mContinueReading;
        @BindView(R.id.famMangaInfoHeaderFollow) FloatingActionMenu mFollowMenu;

        @BindColor(R.color.manga_red) int mRed;
        @BindColor(R.color.manga_blue) int mBlue;
        @BindColor(R.color.manga_green) int mGreen;
        @BindColor(R.color.manga_gray) int mGray;
        @BindColor(R.color.colorAccent) int mAccent;

        @BindDrawable(R.drawable.manga_error) Drawable mError;
        @BindDrawable(R.drawable.manga_loading_image) Drawable mPlaceHolder;

        @BindDrawable(R.drawable.ic_heart_white_24dp) Drawable mFollowing;
        @BindDrawable(R.drawable.ic_heart_outline_white_24dp) Drawable mDrawableNotFollowing;

        @BindString(R.string.manga_info_header_fab_complete) String mCompleteText;
        @BindString(R.string.manga_info_header_fab_on_hold) String mOnHoldText;
        @BindString(R.string.manga_info_header_fab_plan_to_read) String mPlanToReadText;
        @BindString(R.string.manga_info_header_fab_reading) String mReadingText;
        @BindString(R.string.manga_info_header_fab_unfollow) String mUnfollowText;

        public ViewHolderHeader(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void initViews()
        {
            mTitle.setText(mManga.title);
            mAlternates.setText(mManga.alternate);
            mArtist.setText(mManga.artist);
            mAuthor.setText(mManga.author);
            mDescription.setText(mManga.description);
            mStatus.setText(mManga.status);
            mGenres.setText(mManga.genres);

            if (!mHasChapters)
            {
                mChapterHeaderLabel.setVisibility(View.INVISIBLE);
            }

            Picasso.get().load(mManga.image)
                   .error(mError)
                   .placeholder(mPlaceHolder)
                   .into(mImageTarget);

            switch (mManga.following)
            {
                case Manga.FOLLOW_COMPLETE:
                    onFABFollowCompleteClick();
                    break;
                case Manga.FOLLOW_ON_HOLD:
                    onFABFollowOnHoldClick();
                    break;
                case Manga.FOLLOW_PLAN_TO_READ:
                    onFABFollowPlanToReadClick();
                    break;
                case Manga.FOLLOW_READING:
                    onFABFollowReadingClick();
                    break;
                case Manga.UNFOLLOW:
                    onFABUnfollowReadingClick();
                    break;
            }

        }

        @OnClick(R.id.fabMangaInfoFollowComplete)
        public void onFABFollowCompleteClick()
        {
            mManga.following = Manga.FOLLOW_COMPLETE;
            MangaFeed.getInstance().rxBus().send(new UpdateItemEvent(mManga));
            mFollowMenu.getMenuIconView().setImageDrawable(mFollowing);
            mFollowMenu.setMenuButtonColorNormal(mGreen);
            mContinueReading.setColorNormal(mGreen);
            mFollowMenu.setMenuButtonLabelText(mCompleteText);
            mFollowMenu.close(true);
        }

        @OnClick(R.id.fabMangaInfoFollowOnHold)
        public void onFABFollowOnHoldClick()
        {
            mManga.following = Manga.FOLLOW_ON_HOLD;
            MangaFeed.getInstance().rxBus().send(new UpdateItemEvent(mManga));
            mFollowMenu.getMenuIconView().setImageDrawable(mFollowing);
            mFollowMenu.setMenuButtonColorNormal(mRed);
            mContinueReading.setColorNormal(mRed);
            mFollowMenu.setMenuButtonLabelText(mOnHoldText);
            mFollowMenu.close(true);
        }

        @OnClick(R.id.fabMangaInfoFollowPlanToRead)
        public void onFABFollowPlanToReadClick()
        {
            mManga.following = Manga.FOLLOW_PLAN_TO_READ;
            MangaFeed.getInstance().rxBus().send(new UpdateItemEvent(mManga));
            mFollowMenu.getMenuIconView().setImageDrawable(mFollowing);
            mFollowMenu.setMenuButtonColorNormal(mGray);
            mContinueReading.setColorNormal(mGray);
            mFollowMenu.setMenuButtonLabelText(mPlanToReadText);
            mFollowMenu.close(true);
        }

        @OnClick(R.id.fabMangaInfoFollowReading)
        public void onFABFollowReadingClick()
        {
            mManga.following = Manga.FOLLOW_READING;
            MangaFeed.getInstance().rxBus().send(new UpdateItemEvent(mManga));
            mFollowMenu.getMenuIconView().setImageDrawable(mFollowing);
            mFollowMenu.setMenuButtonColorNormal(mBlue);
            mContinueReading.setColorNormal(mBlue);
            mFollowMenu.setMenuButtonLabelText(mReadingText);
            mFollowMenu.close(true);
        }

        @OnClick(R.id.fabMangaInfoFollowUnFollow)
        public void onFABUnfollowReadingClick()
        {
            mManga.following = Manga.UNFOLLOW;
            MangaFeed.getInstance().rxBus().send(new UpdateItemEvent(mManga));
            mFollowMenu.getMenuIconView().setImageDrawable(mDrawableNotFollowing);
            mFollowMenu.setMenuButtonColorNormal(mAccent);
            mContinueReading.setColorNormal(mAccent);
            mFollowMenu.setMenuButtonLabelText(mUnfollowText);
            mFollowMenu.close(true);
        }

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
    }

    private List<Chapter> mDownloadList;

    public class ViewHolderChapter extends RecyclerView.ViewHolder
    {
        @BindView(R.id.textViewMangaInfoChapterDate) TextView mDate;
        @BindView(R.id.textViewMangaInfoChapterTitle) TextView mTitle;
        @BindView(R.id.imageViewMangaInfoChapterDownload) ImageView mDownloadImage;
        @BindView(R.id.imageViewMangaInfoChapterDownloadCheckBox) ImageView mDownloadBox;

        @BindDrawable(R.drawable.ic_download_grey600_24dp) Drawable mDownload;

        @BindDrawable(R.drawable.ic_check_circle_outline_black_24dp) Drawable mBlackCheck;
        @BindDrawable(R.drawable.ic_check_circle_outline_white_24dp) Drawable mWhiteCheck;
        @BindDrawable(R.drawable.ic_checkbox_blank_circle_outline_black_24dp) Drawable mBlackOutline;
        @BindDrawable(R.drawable.ic_checkbox_blank_circle_outline_white_24dp) Drawable mWhiteOutline;


        public ViewHolderChapter(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


        public void initViews(int position)
        {
            Chapter lChapter = mChapterData.get(position);

            mTitle.setText(lChapter.chapterTitle);
            mDate.setText(lChapter.date);

            if (mDownloadViewFlag)
            {
                mDownloadBox.setVisibility(View.VISIBLE);
                mDownloadImage.setVisibility(View.GONE);
                boolean lIsChecked = mDownloadList.contains(lChapter);
                mDownloadBox.setImageDrawable(iconFactory(lIsChecked));
            }
            else
            {
                mDownloadBox.setVisibility(View.GONE);
                mDownloadImage.setVisibility(View.VISIBLE);
            }

            // TODO
            // Check if Downloaded -> if downloaded set image to checked
            // else
            mDownloadImage.setImageDrawable(mDownload);

        }

        @OnClick(R.id.linearLayoutMangaInfoChapterRoot)
        public void onChapterRootClick()
        {
            Chapter lChapter = mChapterData.get(getAdapterPosition() - getHeaderCount());
            if (mDownloadViewFlag)
            {
                boolean lIsChecked = mDownloadList.contains(lChapter);
                if (lIsChecked)
                {
                    mDownloadList.remove(lChapter);
                }
                else
                {
                    mDownloadList.add(lChapter);
                }

                mDownloadBox.setImageDrawable(iconFactory(!lIsChecked));
            }
            else
            {
                // Launch reader activity
            }
        }

        @OnLongClick(R.id.linearLayoutMangaInfoChapterRoot)
        public boolean onChapterRootLongClick()
        {
            // activate multiple download view
            if (!mDownloadViewFlag)
            {
                mDownloadList = new ArrayList<>();
                mDownloadList.add(mChapterData.get(getAdapterPosition() - getHeaderCount()));

                onDownloadViewEnabled();

                return true;
            }

            return false;
        }


        @OnClick(R.id.frameLayoutMangaInfoChapterDownload)
        public void onChapterDownloadButtonClick()
        {
            // TODO
            // download chapter
        }

        private Drawable iconFactory(boolean checked)
        {
            if (SharedPrefs.getLayoutTheme())
            {
                if (checked)
                {
                    return mBlackCheck;
                }
                return mBlackOutline;
            }
            else
            {
                if (checked)
                {
                    return mWhiteCheck;
                }
                return mWhiteOutline;
            }
        }
    }

    public void onSelectAllOrNone(boolean isAll)
    {
        if (isAll)
        {
            mDownloadList = new ArrayList<>(mChapterData);
        }
        else
        {
            mDownloadList = new ArrayList<>();
        }

        notifyDataSetChanged();
    }

    public void onDownloadCancel()
    {
        mDownloadViewFlag = false;
        mDownloadList.clear();
        notifyDataSetChanged();
        MangaFeed.getInstance().rxBus().send(new MangaDownloadSelectEvent(mManga));
    }

    public void onDownloadDownload()
    {
        // TODO - implement download service
        // Send RX Bus event to signal start of download, and pass in chapter list to event
    }

    public int getFirstDownloadScrollPosition()
    {
        if (mDownloadList.size() == 0)
        {
            return 0;
        }
        else
        {
            return mChapterData.indexOf(mDownloadList.get(0));
        }
    }

    public void onDownloadViewEnabled()
    {
        mDownloadViewFlag = true; // Set it to true, after getting toolbar for downloads working
        MangaFeed.getInstance().rxBus().send(new MangaDownloadSelectEvent(mManga));
        notifyDataSetChanged();
    }


}
