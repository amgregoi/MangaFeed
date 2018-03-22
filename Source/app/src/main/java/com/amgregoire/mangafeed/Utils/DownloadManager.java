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
import com.amgregoire.mangafeed.Models.Manga;
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

    private Chapter mChapter;
    private Manga mManga;
    private SourceBase mSource;

    private List<String> mChapterUrls;
    private List<Target> mTargets;
    private int mTotalPages = 0;
    private int mSavedPages = 0;
    private File mChapterDirectory;

    public DownloadManager(Chapter chapter)
    {
        mChapter = chapter;
        mManga = MangaDB.getInstance().getManga(mChapter.mangaUrl);
        mSource = MangaFeed.getInstance().getSourceByUrl(mChapter.getUrl());
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

    public Chapter getChapter() { return mChapter; }

    /***
     * This function starts the chapter download.
     *
     */
    public void startDownload()
    {
        if (mChapter._id == null)
        {
            mChapter = MangaDB.getInstance().getChapter(mChapter);
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
                                                                             .append(mManga.getTitle()
                                                                                           .replaceAll("\\W", ""))
                                                                             .append("-")
                                                                             .append(mManga._id)
                                                                             .append("/Ch")
                                                                             .append(mChapter.getChapterNumber())
                                                                             .append("-")
                                                                             .append(mChapter._id)
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
                                mSource.getChapterImageListObservable(new RequestWrapper(mChapter))
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
            mChapter.downloadStatus = Chapter.DOWNLOAD_STATUS_FINISHED;
            MangaDB.getInstance().putChapter(mChapter);

            DownloadScheduler.removeChapterDownloading(this);
            MangaFeed.getInstance().rxBus().send(new DownloadEventUpdateComplete(mChapter.url));
            MangaLogger.logInfo(TAG, "Finished downloading: " + mChapter.chapterTitle);
        }
        else
        {
            MangaFeed.getInstance().rxBus().send(new DownloadEventUpdatePageCount(mChapter.url));
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
            if (MangaFeed.getInstance()
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
    public static List<Manga> getMangaWithSavedChapters()
    {
        File lDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                              .getPath() + DOWNLOAD_FOLDER);
        File[] mangaDirectories = lDirectory.listFiles();
        List<Manga> lResult = new ArrayList<>();
        Pattern pattern = Pattern.compile(sFileRegex);

        for (File file : mangaDirectories)
        {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find())
            {
                String lId = matcher.group(0);
                Manga lManga = MangaDB.getInstance().getManga(Long.parseLong(lId));
                lResult.add(lManga);
            }
        }

        return lResult;
    }

    /***
     * This function retrieves the chapters for a specified manga that have been saved to the devices external storage.
     *
     * @param manga
     * @return
     */
    public static List<Chapter> getSavedChapters(Manga manga)
    {
        File lDirectory = new File(new StringBuilder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                                                .getPath()).append(DOWNLOAD_FOLDER)
                                                                           .append("/")
                                                                           .append(manga.getTitle()
                                                                                        .replaceAll("\\W", ""))
                                                                           .append("-")
                                                                           .append(manga._id)
                                                                           .toString());

        File[] mangaDirectories = lDirectory.listFiles();
        List<Chapter> lResult = new ArrayList<>();
        Pattern pattern = Pattern.compile(sFileRegex);

        for (File file : mangaDirectories)
        {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find())
            {
                String lId = matcher.group(0);
                Chapter lChapter = MangaDB.getInstance().getChapter(Long.parseLong(lId));
                lResult.add(lChapter);
            }
        }

        return lResult;
    }

    /***
     * This function returns the saved files for the chapter.
     *
     * @return
     */
    public static File[] getSavedPages(Chapter chapter, Manga manga)
    {
        if (chapter._id == null)
        {
            chapter = MangaDB.getInstance().getChapter(chapter.url);
        }

        if (manga._id == null)
        {
            manga = MangaDB.getInstance().getManga(manga.link);
        }

        return new File(new StringBuilder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                                     .getPath()).append(DOWNLOAD_FOLDER)
                                                                .append("/")
                                                                .append(manga.getTitle()
                                                                             .replaceAll("\\W", ""))
                                                                .append("-")
                                                                .append(manga._id)
                                                                .append("/Ch")
                                                                .append(chapter.getChapterNumber())
                                                                .append("-")
                                                                .append(chapter._id)
                                                                .append("/")
                                                                .toString()).listFiles();
    }
}
