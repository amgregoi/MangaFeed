package com.amgregoire.mangafeed.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.amgregoire.mangafeed.v2.database.AppDatabase;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.MangaFeedKt;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateMangaItemViewEvent;
import com.amgregoire.mangafeed.v2.di.ApplicationContext;
import com.amgregoire.mangafeed.v2.di.DatabaseInfo;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import cz.msebera.android.httpclient.Header;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

@Singleton
public class MangaDB extends SQLiteOpenHelper
{
    public static final String TAG = MangaDB.class.getSimpleName();
    private static final int DATABASE_VERSION = 1;
    private static final String DB_PATH = "/data/data/com.amgregoire.mangafeed/databases/";
    public static final String DB_NAME = "MangaFeed.db";
    private static MangaDB mInstance;


    //    private DaoSession mSession;
//    private Context mContext;

    @Inject
    public MangaDB(@ApplicationContext Context context,
                   @DatabaseInfo String dbName,
                   @DatabaseInfo Integer version,
                   AppDatabase appDatabase)
    {
        super(context, dbName, null, version);
        createDB(context);
        database = appDatabase;
    }

    AppDatabase database;

    public MangaDB(Context context)
    {
        super(context, DB_NAME, null, DATABASE_VERSION);
//        mContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // empty
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //empty
    }

    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
        db.enableWriteAheadLogging();
    }

    /***
     * This function gets an instance of the MangaDB helper.
     */
    public static synchronized MangaDB getInstance()
    {
        return MangaFeedKt.getAppComponent().getDataManager();
    }

    /***
     * This function verifies if the app needs to copy the shipped database.
     */
    public void createDB(Context context)
    {
        boolean dbExist = DBExists(context);
        if (!dbExist)
        {
            copyDBFromResource();
        }
    }

    /***
     * This function verifies if the database has been copied already.
     *
     * @return true if database exists
     */
    private boolean DBExists(Context context)
    {
        SQLiteDatabase lDb = null;

        try
        {
            File lDatabase = context.getDatabasePath(DB_NAME);
            if (lDatabase.exists())
            {
                lDb = SQLiteDatabase.openDatabase(lDatabase.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
                lDb.setLocale(Locale.getDefault());
                lDb.setVersion(1);
            }
        }
        catch (NullPointerException aException)
        {
            MangaLogger.logError(TAG, aException.getMessage(), "Database not found");
        }

        if (lDb != null)
        {
            lDb.close();
        }

        return lDb != null;
    }

    /***
     * This function copies the database provided with the apk.
     */
    private void copyDBFromResource()
    {
        MangaDB helper = this;
        SQLiteDatabase lDb = helper.getReadableDatabase();
        String filePath = lDb.getPath();
        lDb.close();

        try
        {
            InputStream lInputStream = MangaFeed.Companion.getApp().getAssets().open(DB_NAME);
            OutputStream lOutStream = new FileOutputStream(filePath);

            byte[] lBuffer = new byte[1024];
            int lLength;
            while ((lLength = lInputStream.read(lBuffer)) > 0)
            {
                lOutStream.write(lBuffer, 0, lLength);
            }

            lOutStream.flush();
            lOutStream.close();
            lInputStream.close();
        }
        catch (IOException aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
        }
    }

    /***
     * This function adds or updates a manga objec tin the local database.
     *
     * @param manga the object to the added, or updated.
     */
    public Manga putManga(Manga manga)
    {
        database.mangaDao().insertAll(manga);
        return getManga(manga.getLink());
    }

    /***
     * This function retrieves a unique manga object from the local database specified by its url.
     *
     * @param link the URL of the manga.
     * @return
     */
    public Manga getManga(String link)
    {
        return database.mangaDao().findByUrl(link, MangaFeedKt.getCurrentSource().getSourceName());
    }

    public Manga getManga(int id)
    {
        return database.mangaDao().findById(id);
    }

    /***
     * This function verifies if a manga is already in the database specified its url.
     *
     * @param link the url of the manga.
     * @return true if the manga is in the database, false otherwise.
     */
    public boolean containsManga(String link)
    {
        return getManga(link) != null;
    }


    /***
     * This function adds or updates a chapter object in the local database.
     *
     * @param chapter the Chapter to be added, or updated.
     */
    public void putChapter(Chapter chapter)
    {
        database.chapterDao().insertAll(chapter);
    }

    /***
     * This function retrieves a unique chapter object from the local database specified by its url.
     *
     * @param url the URL of the chapter.
     * @return
     */
    public Chapter getChapter(String url)
    {
        return database.chapterDao().findByUrl(url);
    }

    public Chapter getChapter(int id)
    {
        return database.chapterDao().findById(id);
    }

    /***
     * This function retrieves a locally created chapter with the stored version in the local database.
     * If there is no entry for the chapter, one is made and returned.
     *
     * @param chapter
     * @return
     */
    public Chapter getChapter(Chapter chapter)
    {
        return database.chapterDao().findById(chapter.get_id());
    }


    /***
     * This function retrieves a list of manga that have downloaded chapters for offline viewing.
     *
     * @return
     */
    public Observable<List<Manga>> getMangaWithDownloadedChapters()
    {

        return Observable.create((ObservableEmitter<List<Manga>> subscriber) ->
        {
            try
            {
                List<Manga> lManga = DownloadManager.getMangaWithSavedChapters();
                Collections.sort(lManga, (emp1, emp2) -> emp1.getTitle()
                                                             .compareToIgnoreCase(emp2.getTitle()));

                subscriber.onNext(lManga);
                subscriber.onComplete();
            }
            catch (Exception aException)
            {
                subscriber.onError(aException);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<Chapter>> getDownloadedChapters(Manga manga)
    {
        return Observable.create((ObservableEmitter<List<Chapter>> subscriber) ->
        {
            try
            {
                List<Chapter> lResult = DownloadManager.getSavedChapters(manga);
                Collections.sort(lResult, (emp1, emp2) ->
                {
                    if (emp1.getChapterNumber() > emp2.getChapterNumber())
                    {
                        return -1;
                    }
                    return 1;
                });

                subscriber.onNext(lResult);
                subscriber.onComplete();
            }
            catch (Exception ex)
            {
                subscriber.onError(ex);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /***
     * This function returns the list of cached chapters, representing what the user has viewed.
     *
     * @param manga
     * @return
     */
    public Observable<List<Chapter>> getViewedChapters(Manga manga)
    {
        return Observable.create((ObservableEmitter<List<Chapter>> subscriber) ->
        {
            try
            {
                subscriber.onNext(database.chapterDao().findAllByMangaUrl(manga.getLink()));
                subscriber.onComplete();
            }
            catch (Exception aException)
            {
                subscriber.onError(aException);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * This function retrieves the list of followed items from the database.
     *
     * @return Observable arraylist of users followed manga
     */
    public Observable<ArrayList<Manga>> getLibraryList()
    {
        return Observable.create((ObservableEmitter<ArrayList<Manga>> subscriber) ->
        {
            try
            {
                ArrayList<Manga> lMangaList = new ArrayList<>(database.mangaDao().findFollowed(SharedPrefs.getSavedSource()));

                subscriber.onNext(lMangaList);
                subscriber.onComplete();
            }
            catch (Exception aException)
            {
                subscriber.onError(aException);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * This function retrieves the list of followed items from the database with a specified filter (follow type)
     *
     * @return Observable arraylist of users followed manga
     */
    public Observable<ArrayList<Manga>> getLibraryList(int filter)
    {
        return Observable.create((ObservableEmitter<ArrayList<Manga>> subscriber) ->
        {
            try
            {
                ArrayList<Manga> lMangaList = new ArrayList<>(database.mangaDao().findFollowedWithFilter(SharedPrefs.getSavedSource(), filter));
                subscriber.onNext(lMangaList);
                subscriber.onComplete();
            }
            catch (Exception aException)
            {
                subscriber.onError(aException);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /***
     * This function returns the count of each filter type for a users library.
     *
     * @param filters
     * @return
     */
    public Observable<Long> getLibraryFilterCount(int... filters)
    {
        return Observable.create((ObservableEmitter<Long> subscriber) ->
        {
            try
            {
                for (int filter : filters)
                {
                    long lFilterCount = database.mangaDao().findFollowedWithFilter(SharedPrefs.getSavedSource(), filter).size();
                    subscriber.onNext(lFilterCount);
                }

                subscriber.onComplete();
            }
            catch (Exception aException)
            {
                subscriber.onError(aException);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * This function retrieves the source catalog from the database.
     *
     * @return Observable arraylist of sources manga
     */
    public Observable<ArrayList<Manga>> getCatalogList()
    {
        return Observable.create((ObservableEmitter<ArrayList<Manga>> subscriber) ->
        {
            try
            {

                ArrayList<Manga> lMangaList = new ArrayList<>(database.mangaDao().findAllBySource(SharedPrefs.getSavedSource()));
                subscriber.onNext(lMangaList);
                subscriber.onComplete();
            }
            catch (Exception lException)
            {
                subscriber.onError(lException);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /***
     * This function queries the server and retrieves the current users list of followed
     * items, and updates the local database to reflect the status of these items.
     *
     * Afterwards its posts an event to let the application views know they need to be updated to reflect these changes.
     */
    public void updateNewUsersLibrary()
    {
        int lUserId = SharedPrefs.getUserId();
        RequestParams lParams = new RequestParams();

        MangaFeedRest.getUserLibrary(lUserId, lParams, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                super.onSuccess(statusCode, headers, response);
                try
                {
                    JSONArray lFollowList = response.getJSONArray("library");
                    JSONObject lFollow;
                    for (int i = 0; i < lFollowList.length(); i++)
                    {
                        lFollow = lFollowList.getJSONObject(i);

                        Manga lManga = getManga(lFollow.getString("url"));
                        if (lManga != null)
                        {
                            lManga.updateFollowing(lFollow.getInt("followType"));
                            lManga.setImage(lFollow.getString("image"));
                            putManga(lManga);
                        }
                    }

                    MangaFeed.Companion.getApp().rxBus().send(new UpdateMangaItemViewEvent());
                }
                catch (JSONException e)
                {
                    MangaLogger.logError(TAG, e.getMessage());
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse)
            {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    /***
     * This function resets the local database, and sets all following flags to 0.
     *
     */
    public void resetLibrary()
    {
        ContentValues cv = new ContentValues();
        cv.put("following", 0);

        SQLiteDatabase lDb = getWritableDatabase();
        lDb.update("Manga", cv, "NOT following = ?", new String[]{"0"});
        lDb.close();

        MangaFeed.Companion.getApp().rxBus().send(new UpdateMangaItemViewEvent());
    }

}
