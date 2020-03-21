//package com.amgregoire.mangafeed.Utils;
//
//import com.amgregoire.mangafeed.MangaFeed;
//import com.amgregoire.mangafeed.Models.DbChapter;
//import com.amgregoire.mangafeed.Utils.BusEvents.DownloadEventUpdateComplete;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by Andy Gregoire on 3/18/2018.
// */
//
//public class DownloadScheduler
//{
//    public final static String TAG = DownloadScheduler.class.getSimpleName();
//    public static List<DbChapter> mQueue = new ArrayList<>();
//    public static List<DownloadManager> mDownloading = new ArrayList<>();
//    public static List<DbChapter> mDownloading2 = new ArrayList<>();
//
//    public static void initManager()
//    {
//        mQueue = new ArrayList<>();
//        mDownloading = new ArrayList<>();
//    }
//
//    /***
//     * This function adds items to the download queue.
//     *
//     * @param dbChapters
//     */
//    public static void addChaptersToQueue(List<DbChapter> dbChapters)
//    {
//        if (mQueue == null)
//        {
//            initManager();
//        }
//
////        mQueue.addAll(chapters);
//        for (DbChapter dbChapter : dbChapters)
//        {
//            dbChapter.setDownloadStatus(DbChapter.Companion.getDOWNLOAD_STATUS_DOWNLOADING());
//            mQueue.add(MangaDB.getInstance().getChapter(dbChapter));
//        }
//
//        // keep downloading list size to 2
//        while (mDownloading.size() < 1)
//        {
//            DownloadManager lManager = new DownloadManager(mQueue.remove(0));
//            mDownloading2.add(lManager.getChapter());
//            mDownloading.add(lManager);
//            lManager.startDownload();
//        }
//    }
//
//    /***
//     * This function removes an item from the downloading list, and replaces it with an item from the queue.
//     *
//     * @param chapter
//     */
//    public synchronized static void removeChapterDownloading(DownloadManager chapter)
//    {
//        mDownloading.remove(chapter);
//        mDownloading2.remove(chapter.getChapter());
//
//        if (mQueue.size() > 0)
//        {
//            DownloadManager lManager = new DownloadManager(mQueue.remove(0));
//            mDownloading2.add(lManager.getChapter());
//            mDownloading.add(lManager);
//            lManager.startDownload();
//        }
//    }
//
//    public synchronized static void clearDownloads()
//    {
//        mDownloading2 = new ArrayList<>();
//        mQueue = new ArrayList<>();
//
//        // send update complete to update adapter ui
//        if (mDownloading.size() > 0)
//        {
//            MangaFeed.Companion.getApp()
//                     .rxBus()
//                     .send(new DownloadEventUpdateComplete(mDownloading.get(0).getChapter().getUrl()));
//        }
//        mDownloading = new ArrayList<>();
//    }
//
//}
