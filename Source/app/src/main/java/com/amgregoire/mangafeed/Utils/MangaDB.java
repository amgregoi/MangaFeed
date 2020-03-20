package com.amgregoire.mangafeed.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.MangaFeedKt;
import com.amgregoire.mangafeed.Models.DbChapter;
import com.amgregoire.mangafeed.Models.DbManga;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateMangaItemViewEvent;
import com.amgregoire.mangafeed.v2.database.AppDatabase;
import com.amgregoire.mangafeed.v2.di.ApplicationContext;
import com.amgregoire.mangafeed.v2.di.DatabaseInfo;
import com.amgregoire.mangafeed.v2.model.domain.Manga;
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
import java.util.function.Consumer;

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

    private AppDatabase database;

    public AppDatabase getDatabase()
    {
        return database;
    }

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
        return MangaFeed.Companion.getApp().getAppComponent().getDataManager();
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
     * This function adds or updates a manga object in the local database.
     *
     * @param dbManga the object to the added, or updated.
     */
    public DbManga putManga(DbManga dbManga)
    {
        database.mangaDao().insertAll(dbManga);
        return getManga(dbManga.getLink(), dbManga.getSource());
    }

    /***
     * This function retrieves a unique manga object from the local database specified by its url.
     *
     * @param link the URL of the manga.
     * @return
     */
    public DbManga getManga(String link)
    {

        return database.mangaDao().findByUrl(link, MangaFeedKt.getCurrentSource().getSourceName());
    }

    public DbManga getManga(String link, String source)
    {

        return database.mangaDao().findByUrl(link, source);
    }

    public DbManga getManga(int id)
    {
        return database.mangaDao().findById(id);
    }

    public List<DbManga> getExistingManga(List<String> urlList, String source)
    {
        return database.mangaDao().test(source, urlList);
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

    public boolean containsManga(DbManga dbManga)
    {
        String url = DbManga.Companion.mangaToFormattedUrl(dbManga);
        return getManga(url, dbManga.getSource()) != null;
    }


    /***
     * This function adds or updates a chapter object in the local database.
     *
     * @param dbChapter the Chapter to be added, or updated.
     */
    public void putChapter(DbChapter dbChapter)
    {
        database.chapterDao().insertAll(dbChapter);
    }

    /***
     * This function retrieves a unique chapter object from the local database specified by its url.
     *
     * @param url the URL of the chapter.
     * @return
     */
    public DbChapter getChapter(String url)
    {
        return database.chapterDao().findByUrl(url);
    }

    public DbChapter getChapter(int id)
    {
        return database.chapterDao().findById(id);
    }

    /***
     * This function retrieves a locally created chapter with the stored version in the local database.
     * If there is no entry for the chapter, one is made and returned.
     *
     * @param dbChapter
     * @return
     */
    public DbChapter getChapter(DbChapter dbChapter)
    {
        return getChapter(dbChapter.getUrl());
    }


    /***
     * This function retrieves a list of manga that have downloaded chapters for offline viewing.
     *
     * @return
     */
    public Observable<List<DbManga>> getMangaWithDownloadedChapters()
    {

        return Observable.create((ObservableEmitter<List<DbManga>> subscriber) ->
        {
            try
            {
                List<DbManga> lDbManga = DownloadManager.getMangaWithSavedChapters();
                Collections.sort(lDbManga, (emp1, emp2) -> emp1.getTitle()
                                                               .compareToIgnoreCase(emp2.getTitle()));

                subscriber.onNext(lDbManga);
                subscriber.onComplete();
            }
            catch (Exception aException)
            {
                subscriber.onError(aException);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<DbChapter>> getDownloadedChapters(DbManga dbManga)
    {
        return Observable.create((ObservableEmitter<List<DbChapter>> subscriber) ->
        {
            try
            {
                List<DbChapter> lResult = DownloadManager.getSavedChapters(dbManga);
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
     * @param dbManga
     * @return
     */
    public Observable<List<DbChapter>> getViewedChapters(DbManga dbManga)
    {
        return Observable.create((ObservableEmitter<List<DbChapter>> subscriber) ->
        {
            try
            {
                subscriber.onNext(database.chapterDao().findAllByMangaUrl(dbManga.getLink()));
                subscriber.onComplete();
            }
            catch (Exception aException)
            {
                subscriber.onError(aException);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


//    /**
//     * This function retrieves the list of followed items from the database.
//     *
//     * @return Observable arraylist of users followed manga
//     */
//    public Observable<ArrayList<DbManga>> getLibraryList()
//    {
//        return Observable.create((ObservableEmitter<ArrayList<DbManga>> subscriber) ->
//        {
//            try
//            {
//                ArrayList<DbManga> lDbMangaList = new ArrayList<>(database.mangaDao().findFollowed(SharedPrefs.getSavedSource()));
//                subscriber.onNext(lDbMangaList);
//                subscriber.onComplete();
//            }
//            catch (Exception aException)
//            {
//                subscriber.onError(aException);
//            }
//        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
//    }

    /**
     * This function retrieves the list of followed items from the database with a specified filter (follow type)
     *
     * @return Observable arraylist of users followed manga
     */
    public Observable<ArrayList<DbManga>> getLibraryList(int filter)
    {
        return Observable.create((ObservableEmitter<ArrayList<DbManga>> subscriber) ->
        {
            try
            {
                ArrayList<DbManga> lDbMangaList = new ArrayList<>(database.mangaDao().findFollowedWithFilter(SharedPrefs.getSavedSource(), filter));
                subscriber.onNext(lDbMangaList);
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
    public Observable<ArrayList<DbManga>> getCatalogList()
    {
        return Observable.create((ObservableEmitter<ArrayList<DbManga>> subscriber) ->
        {
            try
            {

                ArrayList<DbManga> lDbMangaList = new ArrayList<>(database.mangaDao().findAllBySource(SharedPrefs.getSavedSource()));
                subscriber.onNext(lDbMangaList);
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

                        DbManga lDbManga = getManga(lFollow.getString("url"));
                        if (lDbManga != null)
                        {
                            lDbManga.updateFollowing(lFollow.getInt("followType"));
                            lDbManga.setImage(lFollow.getString("image"));
                            putManga(lDbManga);
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
