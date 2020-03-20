package com.amgregoire.mangafeed.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import android.util.Log;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.DbChapter;
import com.amgregoire.mangafeed.Models.DbManga;
import com.amgregoire.mangafeed.Utils.BusEvents.DownloadEventUpdateComplete;
import com.amgregoire.mangafeed.Utils.BusEvents.DownloadEventUpdatePageCount;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Andy Gregoire on 3/14/2018.
 */

public class DownloadManager
{
    public final static String TAG = DownloadManager.class.getSimpleName();
    public final static String DOWNLOAD_FOLDER = "/MangaFeedDownloads/";
    private static String sFileRegex = "\\d+(?!.*-)"; // retrieves the database id from the file names

    private DbChapter mDbChapter;
    private DbManga mDbManga;
    private SourceBase mSource;

    private List<String> mChapterUrls;
    private List<Target> mTargets;
    private int mTotalPages = 0;
    private int mSavedPages = 0;
    private File mChapterDirectory;

    public DownloadManager(DbChapter dbChapter)
    {
        mDbChapter = dbChapter;
        mDbManga = MangaDB.getInstance().getManga(mDbChapter.getMangaUrl());
        mSource = MangaFeed.Companion.getApp().getSourceByUrl(mDbChapter.getUrl());
        setChapterFilePath();

        mChapterUrls = new ArrayList<>();
        mTargets = new ArrayList<>();
    }

    /***
     * This function retrieves the number of pages being downloaded for this chapter.
     *
     * @return
     */
    public int getTotalPageCount()
    {
        return mTotalPages;
    }

    public int getCurrentPageCount()
    {
        return mSavedPages;
    }

    public DbChapter getChapter() { return mDbChapter; }

    /***
     * This function starts the chapter download.
     *
     */
    public void startDownload()
    {
        if (mDbChapter.get_id() == null)
        {
            mDbChapter = MangaDB.getInstance().getChapter(mDbChapter);
        }

        getChapterPages();
    }


    /***
     * This function sets the chapter file directory for the respective chapter and its source.
     *
     */
    private void setChapterFilePath()
    {
        mChapterDirectory = new File(new StringBuilder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                                                  .getPath()).append(DOWNLOAD_FOLDER)
                                                                             .append("/")
                                                                             .append(mDbManga.getTitle()
                                                                                             .replaceAll("\\W", ""))
                                                                             .append("-")
                                                                             .append(mDbManga.get_id())
                                                                             .append("/Ch")
                                                                             .append(mDbChapter.getChapterNumber())
                                                                             .append("-")
                                                                             .append(mDbChapter.get_id())
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

        mSource.getChapterImageListObservable(new RequestWrapper(mDbChapter))
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
                           mTotalPages = mChapterUrls.size();
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
                                mSource.getChapterImageListObservable(new RequestWrapper(mDbChapter))
                                       .cache()
                                       .subscribe(s ->
                                       {
                                           File lSavedFile = new File(mChapterDirectory, position + ".txt");
                                           FileWriter fw = new FileWriter(lSavedFile);

                                           try
                                           {
                                               fw.write(s);
                                               incrementPageSaved();
                                           }
                                           finally
                                           {
                                               fw.close();
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
                    incrementPageSaved();
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

    /***
     * This function increments the total number of downloaded pages, until all pages are downloaded.
     * And calls the scheduler to remove itself, and queue up the next chapter.
     *
     */
    private void incrementPageSaved()
    {
        mSavedPages++;
        if (mSavedPages == mTotalPages)
        {
            mDbChapter.setDownloadStatus(DbChapter.Companion.getDOWNLOAD_STATUS_FINISHED());
            MangaDB.getInstance().putChapter(mDbChapter);

            DownloadScheduler.removeChapterDownloading(this);
            MangaFeed.Companion.getApp().rxBus().send(new DownloadEventUpdateComplete(mDbChapter.getUrl()));
            MangaLogger.logInfo(TAG, "Finished downloading: " + mDbChapter.getChapterTitle());
        }
        else
        {
            MangaFeed.Companion.getApp().rxBus().send(new DownloadEventUpdatePageCount(mDbChapter.getUrl()));
        }
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
            if (MangaFeed.Companion.getApp()
                         .checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
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

    /***
     * This function retrieves the list of manga that have saved chapters on the devices external storage.
     *
     * @return
     */
    public static List<DbManga> getMangaWithSavedChapters()
    {
        File lDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                              .getPath() + DOWNLOAD_FOLDER);
        File[] mangaDirectories = lDirectory.listFiles();
        List<DbManga> lResult = new ArrayList<>();
        Pattern pattern = Pattern.compile(sFileRegex);

        for (File file : mangaDirectories)
        {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find())
            {
                String lId = matcher.group(0);
                DbManga lDbManga = MangaDB.getInstance().getManga(lId);
                lResult.add(lDbManga);
            }
        }

        return lResult;
    }

    /***
     * This function retrieves the chapters for a specified manga that have been saved to the devices external storage.
     *
     * @param dbManga
     * @return
     */
    public static List<DbChapter> getSavedChapters(DbManga dbManga)
    {
        File lDirectory = new File(new StringBuilder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                                                .getPath()).append(DOWNLOAD_FOLDER)
                                                                           .append("/")
                                                                           .append(dbManga.getTitle()
                                                                                          .replaceAll("\\W", ""))
                                                                           .append("-")
                                                                           .append(dbManga.get_id())
                                                                           .toString());

        File[] mangaDirectories = lDirectory.listFiles();
        List<DbChapter> lResult = new ArrayList<>();
        Pattern pattern = Pattern.compile(sFileRegex);

        for (File file : mangaDirectories)
        {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find())
            {
                String lId = matcher.group(0);
                DbChapter lDbChapter = MangaDB.getInstance().getChapter(lId);
                lResult.add(lDbChapter);
            }
        }

        return lResult;
    }

    /***
     * This function returns the saved files for the chapter.
     *
     * @return
     */
    public static File[] getSavedPages(DbChapter dbChapter, DbManga dbManga)
    {
        if (dbChapter.get_id() == null)
        {
            dbChapter = MangaDB.getInstance().getChapter(dbChapter.getUrl());
        }

//        if (manga.get_id() == null)
//        {
//            manga = MangaDB.getInstance().getManga(manga.getLink());
//        }

        return new File(new StringBuilder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                                     .getPath()).append(DOWNLOAD_FOLDER)
                                                                .append("/")
                                                                .append(dbManga.getTitle()
                                                                               .replaceAll("\\W", ""))
                                                                .append("-")
                                                                .append(dbManga.get_id())
                                                                .append("/Ch")
                                                                .append(dbChapter.getChapterNumber())
                                                                .append("-")
                                                                .append(dbChapter.get_id())
                                                                .append("/")
                                                                .toString()).listFiles();
    }
}
