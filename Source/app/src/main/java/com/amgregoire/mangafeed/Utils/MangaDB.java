package com.amgregoire.mangafeed.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.ChapterDao;
import com.amgregoire.mangafeed.Models.DaoMaster;
import com.amgregoire.mangafeed.Models.DaoSession;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.Models.MangaDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import io.reactivex.Observable;

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

    public void putManga(Manga manga, boolean derp){

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

    /**
     * This function retrieves the list of followed items from the database.
     *
     * @return Observable arraylist of users followed manga
     */
    public Observable<ArrayList<Manga>> getLibraryList()
    {
        return Observable.create(subscriber ->
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
        });
    }

    /**
     * This function retrieves the list of followed items from the database with a specified filter (follow type)
     *
     * @return Observable arraylist of users followed manga
     */
    public Observable<ArrayList<Manga>> getLibraryList(int filter)
    {
        return Observable.create(subscriber ->
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
        });
    }

    public Observable<Long> test(int... filters)
    {
        return Observable.create(subscriber ->
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
        });


    }


    /**
     * This function retrieves the source catalog from the database.
     *
     * @return Observable arraylist of sources manga
     */
    public Observable<ArrayList<Manga>> getCatalogList()
    {
        return Observable.create(subscriber ->
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
        });
    }


    /***
     * This inner class defines the sql column names for the Manga Table
     */
    static class MangaTable
    {
        public final static String ID = "_id";
        public final static String Title = "title";
        public final static String Alternate = "alternate";
        public final static String Image = "image";
        public final static String Description = "description";
        public final static String Artist = "artist";
        public final static String Author = "author";
        public final static String Genres = "genres";
        public final static String Status = "status";
        public final static String Source = "source";
        public final static String RecentChapter = "recentChapter";
        public final static String URL = "link";
        public final static String Following = "following";
    }

    /***
     * This inner class defines the sql column names for the Chapter Table
     */
    static class ChapterTable
    {
        public final static String TotalPages = "totalPages";
        public final static String CurrentPage = "currentPage";
        public final static String ChapterNumber = "chapterNumber";
        public final static String ChapterTitle = "chapterTitle";
        public final static String MangaTitle = "mangaTitle";
        public final static String Date = "date";
        public final static String URL = "url";
    }
}
