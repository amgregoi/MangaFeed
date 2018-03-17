package com.amgregoire.mangafeed.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.ChapterDao;
import com.amgregoire.mangafeed.Models.DaoMaster;
import com.amgregoire.mangafeed.Models.DaoSession;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.Models.MangaDao;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateMangaItemViewEvent;
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
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class MangaDB extends SQLiteOpenHelper
{
    public static final String TAG = MangaDB.class.getSimpleName();
    private static final int DATABASE_VERSION = 1;
    private static final String DB_PATH = "/data/data/com.amgregoire.mangafeed/databases/";
    private static final String DB_NAME = "MangaFeed.db";
    private static MangaDB mInstance;


    private DaoSession mSession;
    private Context mContext;

    public MangaDB(Context context)
    {
        super(context, DB_NAME, null, DATABASE_VERSION);
        mContext = context;
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

    /***
     * This function gets an instance of the MangaDB helper.
     */
    public static synchronized MangaDB getInstance()
    {
        if (mInstance == null)
        {
            mInstance = new MangaDB(MangaFeed.getInstance());
        }
        return mInstance;
    }

    public void initDao()
    {
        mSession = new DaoMaster(MangaDB.getInstance().getWritableDatabase()).newSession();
    }

    /***
     * This function verifies if the app needs to copy the shipped database.
     */
    public void createDB()
    {
        boolean dbExist = DBExists();
        if (!dbExist)
        {
            this.getReadableDatabase();
            copyDBFromResource();
        }
    }

    /***
     * This function verifies if the database has been copied already.
     *
     * @return true if database exists
     */
    private boolean DBExists()
    {
        SQLiteDatabase lDb = null;

        try
        {
            File lDatabase = mContext.getDatabasePath(DB_NAME);
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
        String lFilePath = DB_PATH + DB_NAME;
        try
        {
            InputStream lInputStream = MangaFeed.getInstance().getAssets().open(DB_NAME);
            OutputStream lOutStream = new FileOutputStream(lFilePath);

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
    public void putManga(Manga manga)
    {
        MangaDao lDao = mSession.getMangaDao();

        if (lDao.hasKey(manga))
        {
            lDao.update(manga);
        }
        else
        {
            lDao.insert(manga);
        }
    }

    /***
     * This function retrieves a unique manga object from the local database specified by its url.
     *
     * @param link the URL of the manga.
     * @return
     */
    public Manga getManga(String link)
    {
        return mSession.getMangaDao()
                       .queryBuilder()
                       .where(MangaDao.Properties.Link.eq(link))
                       .unique();
    }

    /***
     * This function verifies if a manga is already in the database specified its url.
     *
     * @param link the url of the manga.
     * @return true if the manga is in the database, false otherwise.
     */
    public boolean containsManga(String link)
    {
        return mSession.getMangaDao()
                       .queryBuilder()
                       .where(MangaDao.Properties.Link.eq(link))
                       .count() > 0;
    }


    /***
     * This function adds or updates a chapter object in the local database.
     *
     * @param chapter the Chapter to be added, or updated.
     */
    public void putChapter(Chapter chapter)
    {
        ChapterDao lDao = mSession.getChapterDao();

        if (lDao.hasKey(chapter))
        {
            lDao.update(chapter);
        }
        else
        {
            lDao.insert(chapter);
        }
    }

    /***
     * This function retrieves a unique chapter object from the local database specified by its url.
     *
     * @param url the URL of the chapter.
     * @return
     */
    public Chapter getChapter(String url)
    {
        return mSession.getChapterDao()
                       .queryBuilder()
                       .where(ChapterDao.Properties.Url.eq(url))
                       .unique();
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
                List<String> lUrls = new ArrayList<>();
                try (Cursor cursor = getReadableDatabase().rawQuery("SELECT DISTINCT mangaUrl FROM Chapter WHERE downloadStatus = 2", new String[]{}))
                {
                    int index = cursor.getColumnIndex("mangaUrl");
                    while (cursor.moveToNext())
                    {
                        lUrls.add(cursor.getString(index));
                    }
                }

                // Return list of manga specified by the distinct list of mangaUrls contained in the downloaded chapters
                subscriber.onNext(mSession.getMangaDao()
                                          .queryBuilder()
                                          .where(MangaDao.Properties.Link.in(lUrls))
                                          .list());
                subscriber.onComplete();
            }
            catch (Exception aException)
            {
                subscriber.onError(aException);
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
                subscriber.onNext(mSession.getChapterDao()
                                          .queryBuilder()
                                          .where(ChapterDao.Properties.MangaUrl.eq(manga.getMangaURL()))
                                          .list());
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
                ArrayList<Manga> lMangaList = new ArrayList<>(mSession.getMangaDao()
                                                                      .queryBuilder()
                                                                      .where(MangaDao.Properties.Source
                                                                              .eq(SharedPrefs.getSavedSource()))
                                                                      .where(MangaDao.Properties.Following
                                                                              .notEq(0))
                                                                      .list());

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
                ArrayList<Manga> lMangaList = new ArrayList<>(mSession.getMangaDao()
                                                                      .queryBuilder()
                                                                      .where(MangaDao.Properties.Source
                                                                              .eq(SharedPrefs.getSavedSource()))
                                                                      .where(MangaDao.Properties.Following
                                                                              .eq(filter))
                                                                      .list());

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
                    long lFilterCount = mSession.getMangaDao()
                                                .queryBuilder()
                                                .where(MangaDao.Properties.Source.eq(SharedPrefs.getSavedSource()))
                                                .where(MangaDao.Properties.Following.eq(filter))
                                                .count();

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

                ArrayList<Manga> lMangaList = new ArrayList<>(mSession.getMangaDao()
                                                                      .queryBuilder()
                                                                      .where(MangaDao.Properties.Source
                                                                              .eq(SharedPrefs.getSavedSource()))
                                                                      .list());

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
                            lManga.following = lFollow.getInt("followType");
                            lManga.image = lFollow.getString("image");
                            putManga(lManga);
                        }
                    }

                    mSession.clear();
                    MangaFeed.getInstance().rxBus().send(new UpdateMangaItemViewEvent());
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
        getWritableDatabase().update("Manga", cv, "NOT following = ?", new String[]{"0"});

        // Clear Dao cache, to get pull fresh db values
        mSession.clear();
        MangaFeed.getInstance().rxBus().send(new UpdateMangaItemViewEvent());
    }

}
