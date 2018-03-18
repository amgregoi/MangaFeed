package com.amgregoire.mangafeed.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Chapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Andy Gregoire on 3/14/2018.
 */

public class DownloadManager
{
    public final static String DOWNLOAD_FOLDER = "/MangaFeedDownloads/";

    private Chapter mChapter;
    private SourceBase mSource;

    private DownloadUpdater mUpdater;
    private List<String> mChapterUrls;
    private List<Target> mTargets;

    private File mChapterDirectory;

    public DownloadManager(Chapter chapter, DownloadUpdater updater)
    {
        mUpdater = updater;
        mChapter = chapter;

        setChapterFilePath();

        mChapterUrls = new ArrayList<>();
        mTargets = new ArrayList<>();
    }

    /***
     * This function starts the chapter download.
     *
     */
    public void startDownload()
    {
        mChapter.downloadStatus = 2;
        MangaDB.getInstance().putChapter(mChapter);
        getChapterPages();
    }

    /***
     * This function returns the saved files for the chapter.
     *
     * @return
     */
    public File[] getSavedFiles()
    {
        return mChapterDirectory.listFiles();
    }


    /***
     * This function sets the chapter file directory for the respective chapter and its source.
     *
     */
    private void setChapterFilePath()
    {
        mSource = MangaFeed.getInstance().getSourceByUrl(mChapter.getUrl());
        mChapterDirectory = new File(new StringBuilder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                                                  .getPath()).append(DOWNLOAD_FOLDER)
                                                                             .append("/")
                                                                             .append(mSource.getSourceName())
                                                                             .append("/")
                                                                             .append(mChapter.mangaTitle.replaceAll("\\W", ""))
                                                                             .append("/")
                                                                             .append(mChapter.getChapterNumber())
                                                                             .append("/")
                                                                             .toString());
        //Create missing directories
        mChapterDirectory.mkdirs();

    }

    /***
     * This function retrieves the page urls for the download managers chapter.
     *
     */
    public void getChapterPages()
    {
        if (mChapterUrls.size() > 0)
        {
            mChapterUrls.clear();
            mTargets.clear();
        }

        mSource.getChapterImageListObservable(new RequestWrapper(mChapter))
               .subscribe(
                       url ->
                       {
                           //OnNext
                           mChapterUrls.add(url);
                       },
                       throwable ->
                       {
                           // OnError
                           MangaLogger.logError(TAG, "Failed to retrieve image", throwable.getMessage());
                       },
                       () ->
                       {
                           // OnComplete
                           mUpdater.onPageCountReceived(mChapterUrls.size());
                           saveChapterPages();
                       });
    }

    /***
     * This function loads each chapter page and saves them.
     *
     */
    private void saveChapterPages()
    {
        Observable.range(0, mChapterUrls.size()).observeOn(AndroidSchedulers.mainThread()).subscribe
                (
                        position ->
                        {
                            // onNext
                            if (mSource.getSourceType() == MangaEnums.SourceType.MANGA)
                            {
                                mTargets.add(getTarget(position));
                                Picasso.get()
                                       .load(mChapterUrls.get(position))
                                       .into(mTargets.get(position));
                            }
                            else
                            {
                                mSource.getChapterImageListObservable(new RequestWrapper(mChapter))
                                       .cache()
                                       .subscribe(s ->
                                       {
                                           File lSavedFile = new File(mChapterDirectory, position + ".txt");
                                           FileOutputStream stream = new FileOutputStream(lSavedFile);
                                           try
                                           {
                                               stream.write(s.getBytes());
                                               mUpdater.incrementFinishedPages();
                                           }
                                           finally
                                           {
                                               stream.close();
                                           }

                                       }, throwable -> MangaLogger.logError(TAG, throwable.getMessage()));
                            }
                        },
                        throwable ->
                        {
                            // onError
                            MangaLogger.logError(TAG, throwable.getMessage());
                        }
                );
    }


    public final static String TAG = DownloadManager.class.getSimpleName();

    /***
     * This function returns a new target, that saves the loaded bitmap in its appropriate directory.
     *
     * @param pageNumber
     * @return
     */
    private Target getTarget(final int pageNumber)
    {
        return new Target()
        {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from)
            {
                File lSavedImage = new File(mChapterDirectory, pageNumber + ".jpg");

                try
                {
                    if (lSavedImage.exists())
                    {
                        lSavedImage.delete();
                    }

                    lSavedImage.createNewFile();
                    FileOutputStream ostream = new FileOutputStream(lSavedImage);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                    mUpdater.incrementFinishedPages();
                }
                catch (IOException e)
                {
                    MangaLogger.logError(TAG, "IOException", e.getLocalizedMessage());
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable)
            {
                MangaLogger.logError(TAG, e.getMessage());
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable)
            {

            }
        };
    }

    public interface DownloadUpdater
    {
        /***
         * This function lets the listening view know when a page is done so it can increment the downloaded page counter.
         *
         */
        void incrementFinishedPages();

        /***
         * This function lets the listening view know the total amount of pages being downloaded for the chapter.
         *
         */
        void onPageCountReceived(int count);
    }

    /***
     * This function requests storage permission.
     * This permission is required to save chapters for offline reading use.
     *
     * @param activity
     * @return
     */
    public static boolean isStoragePermissionGranted(Activity activity)
    {
        if (Build.VERSION.SDK_INT >= 23)
        {
            if (MangaFeed.getInstance()
                         .checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)
            {
                Log.v(TAG, "Storage permission is granted");
                return true;
            }
            else
            {
                Log.v(TAG, "Storage permission is revoked");
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else
        {
            //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Storage permission is granted");
            return true;
        }
    }
}
