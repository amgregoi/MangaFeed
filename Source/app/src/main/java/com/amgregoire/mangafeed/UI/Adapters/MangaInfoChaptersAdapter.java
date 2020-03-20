package com.amgregoire.mangafeed.UI.Adapters;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.DbChapter;
import com.amgregoire.mangafeed.Models.DbManga;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.Utils.BusEvents.ChapterSelectedEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.ToggleDownloadViewEvent;
import com.amgregoire.mangafeed.Utils.DownloadScheduler;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.SharedPrefs;
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

    private final static int VIEW_HEADER = 1;
    private final static int VIEW_CHAPTER = 2;
    private final static int VIEW_CHAPTER_HEADER = 3;

    private List<DbChapter> mDbChapterData;
    private List<DbChapter> mDownloadList;
    private DbManga mDbManga;
    private boolean mDownloadViewFlag = false;

    public MangaInfoChaptersAdapter(List<DbChapter> data, DbManga dbManga)
    {
        mDbChapterData = new ArrayList<>(data);
        mDownloadList = new ArrayList<>();
        mDbManga = dbManga;
    }

    public MangaInfoChaptersAdapter()
    {
        mDbChapterData = new ArrayList<>();
        mDbManga = null;
    }

    public MangaInfoChaptersAdapter(DbManga dbManga)
    {
        mDbManga = dbManga;
        mDbChapterData = new ArrayList<>();
        mDownloadList = new ArrayList<>();
    }

    public MangaInfoChaptersAdapter(List<DbChapter> data)
    {
        mDbChapterData = new ArrayList<>(data);
        mDownloadList = new ArrayList<>();
        mDbManga = null;
    }

    public void setManga(DbManga dbManga)
    {
        mDbManga = dbManga;
        notifyItemChanged(0);
    }

    public void setChapters(List<DbChapter> dbChapters)
    {
        mDbChapterData = new ArrayList<>(dbChapters);
        notifyDataSetChanged();
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
            else if (position == 1)
            {
                return VIEW_CHAPTER_HEADER;
            }

            return VIEW_CHAPTER;
        }

        if (position == 0)
        {
            return VIEW_CHAPTER_HEADER;
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
            lView = lInflater.inflate(R.layout.item_manga_info_adapter_header, parent, false);
            lHolder = new ViewHolderHeader(lView);
        }
        else if (viewType == VIEW_CHAPTER)
        {
            lView = lInflater.inflate(R.layout.item_manga_info_adapter_chapter, parent, false);
            lHolder = new ViewHolderChapter(lView);
        }
        else
        {
            lView = lInflater.inflate(R.layout.item_manga_info_adapter_chapter_header, parent, false);
            lHolder = new ViewHolderChapterHeader(lView);
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
        else if (holder instanceof ViewHolderChapter)
        {
            ((ViewHolderChapter) holder).initViews(position - getHeaderCount());
        }
        else
        {
            ((ViewHolderChapterHeader) holder).initViews();
        }
    }

    @Override
    public int getItemCount()
    {
        if (mDbManga == null)
        {
            return 0;
        }

        return mDbChapterData.size() + getHeaderCount();
    }

    /***
     * This function returns the header offsets for the item count in the adapter.
     * We will have atleast 1 header visible, and at most 2.
     *
     * @return the header count.
     */
    private int getHeaderCount()
    {
        return (mDownloadViewFlag ? 0 : 1) + 1; // Always have Chapter Header,
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
        @BindView(R.id.textViewMangaInfoHeaderTitle) TextView mTitle;
        @BindView(R.id.imageViewMangaInfoHeader) ImageView mBackgroundImage;
        @BindView(R.id.imageViewMangaInfoHeader2) ImageView mMainImage;

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

        /***
         * This function sets the views for the manga information header view holder when it is being bound.
         *
         */
        public void initViews()
        {
            mTitle.setText(mDbManga.getTitle());
            mAlternates.setText(mDbManga.getAlternate());
            mArtist.setText(mDbManga.getArtist());
            mAuthor.setText(mDbManga.getAuthor());
            mDescription.setText(mDbManga.getDescription());
            mStatus.setText(mDbManga.getStatus());
            mGenres.setText(mDbManga.getGenres());

            if (mDbManga.getImage() != null && !mDbManga.getImage().isEmpty())
            {
                Picasso.get().load(mDbManga.getImage())
                       .error(mError)
                       .placeholder(mPlaceHolder)
                       .into(mImageTarget);
            }
            else
            {
                Picasso.get().load(R.drawable.manga_error).into(mMainImage);
            }
        }

        private final Target mImageTarget = new Target()
        {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
            {
                mBackgroundImage.setImageBitmap(bitmap);
                mBackgroundImage.setScaleType(ImageView.ScaleType.FIT_XY);

                mMainImage.setImageBitmap(bitmap);
                mMainImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable)
            {
                mMainImage.setBackgroundColor(mAccent); //TODO: might remove
                mMainImage.setImageDrawable(errorDrawable);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable)
            {
                mMainImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                mMainImage.setImageDrawable(placeHolderDrawable);
                mBackgroundImage.setImageDrawable(null);

            }
        };
    }

    public class ViewHolderChapterHeader extends RecyclerView.ViewHolder
    {
        @BindView(R.id.textViewMangaInfoHeaderChapterLabel) TextView mChapterHeaderLabel;


        public ViewHolderChapterHeader(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        /***
         * This function sets the views for the chapter header view holder when it is being bound.
         *
         */
        public void initViews()
        {
            if (mDbChapterData.size() == 0)
            {
                mChapterHeaderLabel.setText(R.string.manga_info_adapter_chapter_header_none);
            }
            else
            {
                mChapterHeaderLabel.setText(R.string.manga_info_adapter_chapter_header);
            }
        }
    }

    public class ViewHolderChapter extends RecyclerView.ViewHolder
    {
        @BindView(R.id.textViewMangaInfoChapterDate) TextView mDate;
        @BindView(R.id.textViewMangaInfoChapterTitle) TextView mTitle;
        @BindView(R.id.imageViewMangaInfoChapterDownloadCheckBox) ImageView mDownloadBox;

        @BindDrawable(R.drawable.ic_download_grey600_24dp) Drawable mDownload;

        @BindDrawable(R.drawable.ic_check_circle_outline_black_24dp) Drawable mBlackCheck;
        @BindDrawable(R.drawable.ic_check_circle_outline_white_24dp) Drawable mWhiteCheck;
        @BindDrawable(R.drawable.ic_checkbox_blank_circle_outline_black_24dp) Drawable mBlackOutline;
        @BindDrawable(R.drawable.ic_checkbox_blank_circle_outline_white_24dp) Drawable mWhiteOutline;
        @BindDrawable(R.drawable.icon_seen) Drawable mDrawableSeen;

        public ViewHolderChapter(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        /***
         * This function sets the views for the chapter view holder when it is being bound.
         *
         * @param position the layout position of the view.
         */
        public void initViews(int position)
        {
            DbChapter lDbChapter = mDbChapterData.get(position);

            mTitle.setText(lDbChapter.getChapterTitle());
            mDate.setText(lDbChapter.getChapterDate());

            if (mDownloadViewFlag)
            {
                mDownloadBox.setVisibility(View.VISIBLE);
                boolean lIsChecked = mDownloadList.contains(lDbChapter);
                mDownloadBox.setImageDrawable(iconFactory(lIsChecked));
            }
            else
            {
                if (MangaDB.getInstance().getChapter(lDbChapter.getUrl()) != null)
                {
                    mDownloadBox.setVisibility(View.VISIBLE);
                    mDownloadBox.setImageDrawable(mDrawableSeen);
                }
                else
                {
                    mDownloadBox.setVisibility(View.GONE);
                }
            }
        }

        @OnClick(R.id.linearLayoutMangaInfoChapterRoot)
        public void onChapterRootClick()
        {
            DbChapter lDbChapter = mDbChapterData.get(getAdapterPosition() - getHeaderCount());
            if (mDownloadViewFlag)
            {
                boolean lIsChecked = mDownloadList.contains(lDbChapter);
                if (lIsChecked)
                {
                    mDownloadList.remove(lDbChapter);
                }
                else
                {
                    mDownloadList.add(lDbChapter);
                }

                mDownloadBox.setImageDrawable(iconFactory(!lIsChecked));
            }
            else
            {
                MangaFeed.Companion.getApp().setCurrentDbChapters(mDbChapterData);
                MangaFeed.Companion.getApp()
                         .rxBus()
                         .send(new ChapterSelectedEvent(mDbManga, lDbChapter.getChapterNumber() - 1));
            }
        }

        @OnLongClick(R.id.linearLayoutMangaInfoChapterRoot)
        public boolean onChapterRootLongClick()
        {
            // activate multiple download view
            if (!mDownloadViewFlag)
            {
                mDownloadList = new ArrayList<>();
                mDownloadList.add(mDbChapterData.get(getAdapterPosition() - getHeaderCount()));
                MangaFeed.Companion.getApp().rxBus().send(new ToggleDownloadViewEvent(mDbManga));

                return true;
            }

            return false;
        }

        /***
         * This function returns the correct sets of icons for the chapter items, based on user settings and
         * the state of each chapter item.
         *
         * @param checked the current state of the chapter item.
         * @return the requested drawable.
         */
        private Drawable iconFactory(boolean checked)
        {
            if (SharedPrefs.isLightTheme())
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

    /***
     * This function toggles the selector option for each chapter, either adding or removing all chapters
     * to the download list.
     *
     * @param isAll a flag specifying to check or un-check all items.
     */
    public void onSelectAllOrNone(boolean isAll)
    {
        if (isAll)
        {
            mDownloadList = new ArrayList<>(mDbChapterData);
        }
        else
        {
            mDownloadList = new ArrayList<>();
        }

        notifyDataSetChanged();
    }

    /***
     * This function exits the download view, and returns the adapter to its normal state.
     *
     */
    public void onDownloadCancel()
    {
        mDownloadViewFlag = false;
        mDownloadList.clear();
        notifyDataSetChanged();
        MangaFeed.Companion.getApp().rxBus().send(new ToggleDownloadViewEvent(mDbManga));
    }

    /***
     * This function triggers the start download event for the selected chapters.
     *
     */
    public void onDownloadDownload()
    {
        if (mDownloadList.size() > 0)
        {
            DownloadScheduler.addChaptersToQueue(mDownloadList);
        }
        else
        {
            MangaFeed.Companion.getApp().makeToastShort("No items have been selected");
        }
    }

    public void onDownloadRemove()
    {
        MangaFeed.Companion.getApp().makeToastShort("NOT IMPLEMENTED");
    }

    /***
     * This function retrieves the adapter position of the first item that caused the download view to be toggled.
     * This is done via a long press click on a chapter, or from the overflow window.  If it did not occur from
     * a long press we return the first chapter position.
     *
     * @return adapter position of the first download selection.
     */
    public int getFirstDownloadScrollPosition()
    {
        if (mDownloadList.size() == 0)
        {
            return 0;
        }
        else
        {
            return mDbChapterData.indexOf(mDownloadList.get(0));
        }
    }

    /***
     * This function enables the adapters mass download select view.
     *
     */
    public void onDownloadViewEnabled()
    {
        if (mDownloadList.size() > 1)
        {
            mDownloadList.clear();
        }

        mDownloadViewFlag = true;
        notifyDataSetChanged();
    }


}
