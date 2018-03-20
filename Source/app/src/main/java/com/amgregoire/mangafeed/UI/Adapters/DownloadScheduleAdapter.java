package com.amgregoire.mangafeed.UI.Adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.Utils.BusEvents.DownloadEventUpdateComplete;
import com.amgregoire.mangafeed.Utils.BusEvents.DownloadEventUpdatePageCount;
import com.amgregoire.mangafeed.Utils.DownloadManager;
import com.amgregoire.mangafeed.Utils.DownloadScheduler;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

/**
 * Created by Andy Gregoire on 3/19/2018.
 */

public class DownloadScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public final static String TAG = DownloadScheduleAdapter.class.getSimpleName();

    public final static int VIEW_TYPE_HEADER = 0;
    public final static int VIEW_TYPE_DOWNLOADING = 1;
    public final static int VIEW_TYPE_QUEUE = 2;

    // Change then when decide on active download count, currently only do 1 at at ime.
    private int mDownloadSizeOffset = 3; // 3 == 1 downloading, 4 == 2 downloading etc..

    private List<Chapter> mDownloading;
    private List<Chapter> mQueue;


    public DownloadScheduleAdapter()
    {
        setHasStableIds(true);

        mDownloading = new ArrayList<>(DownloadScheduler.mDownloading2);
        mQueue = new ArrayList<>(DownloadScheduler.mQueue);
    }

    @Override
    public long getItemId(int position)
    {
        int lViewType = getItemViewType(position);

        if (lViewType == VIEW_TYPE_HEADER)
        {
            return position - 4;
        }
        else if (lViewType == VIEW_TYPE_DOWNLOADING)
        {
            return mDownloading.get(position - 1).chapterNumber;
        }
        else
        {
            return mQueue.get(position - mDownloadSizeOffset).chapterNumber;
        }
    }


    @Override
    public int getItemViewType(int position)
    {
        if (position == 0 || position == 2)
        {
            return VIEW_TYPE_HEADER;
        }
        else if (position < 2)
        {
            return VIEW_TYPE_DOWNLOADING;
        }

        return VIEW_TYPE_QUEUE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        RecyclerView.ViewHolder lHolder;
        View lView;

        if (viewType == VIEW_TYPE_HEADER)
        {
            lView = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.item_download_schedule_adapter_header, parent, false);
            lHolder = new HeaderViewHolder(lView);
        }
        else if (viewType == VIEW_TYPE_DOWNLOADING)
        {
            lView = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.item_download_schedule_adapter, parent, false);
            lHolder = new DownloadingViewHolder(lView);
        }
        else
        {
            lView = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.item_download_schedule_adapter, parent, false);
            lHolder = new QueueViewHolder(lView);
        }

        return lHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        if (holder instanceof HeaderViewHolder)
        {
            ((HeaderViewHolder) holder).initViews(position);
        }
        else if (holder instanceof DownloadingViewHolder)
        {
            ((DownloadingViewHolder) holder).initViews(position);
        }
        else
        {
            ((QueueViewHolder) holder).initViews(position);
        }
    }

    @Override
    public int getItemCount()
    {
        int lQueueSize = mQueue.size();
        int lDownloadingSize = mDownloading.size();

        if (lQueueSize + lDownloadingSize == 0)
        {
            return 1;
        }

        return mQueue.size() + mDownloading.size() + 2;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder)
    {
        super.onViewRecycled(holder);
        if (holder instanceof DownloadingViewHolder)
        {
            ((DownloadingViewHolder) holder).mRxBus.dispose();
            ((DownloadingViewHolder) holder).mRxBus = null;
        }
    }

    public class DownloadingViewHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.textViewDownloadScheduleChapterTitle) TextView mChapterTitle;
        @BindView(R.id.textViewDownloadScheduleCurrentPage) TextView mCurrentPage;
        @BindView(R.id.textViewDownloadScheduleMangaUrl) TextView mMangaUrl;
        @BindView(R.id.textViewDownloadScheduleTotalPages) TextView mTotalPages;

        Disposable mRxBus;

        public DownloadingViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressLint("SetTextI18n")
        public void initViews(final int position)
        {

            DownloadManager lManager = DownloadScheduler.mDownloading.get(position - 1);

            mTotalPages.setText("" + lManager.getTotalPageCount());
            mCurrentPage.setText("" + lManager.getCurrentPageCount());
            mChapterTitle.setText(lManager.getChapter().chapterTitle);
            mMangaUrl.setText(lManager.getChapter().mangaTitle);

            if (mRxBus == null)
            {
                mRxBus = MangaFeed.getInstance()
                                  .rxBus()
                                  .toObservable()
                                  .subscribe(o ->
                                  {
                                      if (o instanceof DownloadEventUpdatePageCount)
                                      {
                                          mCurrentPage.setText("" + lManager.getCurrentPageCount());
                                          mTotalPages.setText("" + lManager.getTotalPageCount());
                                      }
                                      else if (o instanceof DownloadEventUpdateComplete)
                                      {
                                          mDownloading = new ArrayList<>(DownloadScheduler.mDownloading2);
                                          mQueue = new ArrayList<>(DownloadScheduler.mQueue);
                                          notifyDataSetChanged();
                                      }
                                  }, throwable -> MangaLogger.logError(TAG, throwable.getMessage()));
            }
        }
    }

    public class QueueViewHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.textViewDownloadScheduleChapterTitle) TextView mChapterTitle;
        @BindView(R.id.textViewDownloadScheduleCurrentPage) TextView mCurrentPage;
        @BindView(R.id.textViewDownloadScheduleMangaUrl) TextView mMangaUrl;
        @BindView(R.id.textViewDownloadScheduleTotalPages) TextView mTotalPages;

        public QueueViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void initViews(int position)
        {
            position = position - mDownloadSizeOffset; // Account for first two headers, and first two items being downloaded
            try
            {
                Chapter lChapter = mQueue.get(position);

                mTotalPages.setText("?");
                mCurrentPage.setText("0");
                mChapterTitle.setText(lChapter.chapterTitle);
                mMangaUrl.setText(lChapter.mangaTitle);
            }
            catch (Exception ex)
            {

            }

        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.textViewDownloadScheduleHeaderTitle) TextView mHeader;

        public HeaderViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

        public void initViews(int position)
        {
            if (getItemCount() == 1)
            {
                mHeader.setText("There are no downloads queued");
            }
            else if (position == 0)
            {
                mHeader.setText("Downloading");
            }
            else
            {
                mHeader.setText("Items in queue (" + mQueue.size() + ")");
            }
        }
    }
}
