package com.amgregoire.mangafeed.Models;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;

@Entity(nameInDb = "Chapter")
public class Chapter implements Parcelable
{
    public final static String TAG = "CHAPTER";

    public final static int DOWNLOAD_STATUS_DOWNLOADING = 1;
    public final static int DOWNLOAD_STATUS_FINISHED = 2;

    @Property(nameInDb = "url")
    public String url;

    @Property(nameInDb = "date")
    public String date;

    @Property(nameInDb = "mangaTitle")
    public String mangaTitle;

    @Property(nameInDb = "mangaUrl")
    public String mangaUrl;

    @Property(nameInDb = "chapterTitle")
    public String chapterTitle;

    @Property(nameInDb = "chapterNumber")
    public int chapterNumber;

    @Property(nameInDb = "currentPage")
    public int currentPage;

    @Property(nameInDb = "totalPages")
    public int totalPages;

    @Property(nameInDb = "downloadStatus")
    public int downloadStatus;


    @Transient
    public boolean mDownloadChecked = false;

    public Chapter()
    {

    }

    public Chapter(String aTitle, String url)
    {
        mangaTitle = aTitle;
        chapterTitle = aTitle;
        mangaUrl = url;
    }

    public Chapter(String aUrl, String aMangaTitle, String aChapterTitle, String aDate, int aNum, String aMangaUrl)
    {
        url = aUrl;
        date = aDate;
        mangaTitle = aMangaTitle;
        chapterTitle = aChapterTitle;
        chapterNumber = aNum;
        mangaUrl = aMangaUrl;
    }

    public Chapter(String aUrl, String aMangaTitle, String aChapterTitle, String aDate, String aMangaUrl)
    {
        url = aUrl;
        date = aDate;
        mangaTitle = aMangaTitle;
        chapterTitle = aChapterTitle;
        mangaUrl = aMangaUrl;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel aDest, int aFlags)
    {
        aDest.writeString(url);
        aDest.writeString(date);
        aDest.writeString(mangaTitle);
        aDest.writeString(chapterTitle);
        aDest.writeInt(chapterNumber);
        aDest.writeInt(currentPage);
        aDest.writeInt(totalPages);
    }

    protected Chapter(Parcel aIn)
    {
        url = aIn.readString();
        date = aIn.readString();
        mangaTitle = aIn.readString();
        chapterTitle = aIn.readString();
        chapterNumber = aIn.readInt();
        currentPage = aIn.readInt();
        totalPages = aIn.readInt();
    }

    @Generated(hash = 1911873980)
    public Chapter(String url, String date, String mangaTitle, String mangaUrl, String chapterTitle, int chapterNumber,
            int currentPage, int totalPages, int downloadStatus) {
        this.url = url;
        this.date = date;
        this.mangaTitle = mangaTitle;
        this.mangaUrl = mangaUrl;
        this.chapterTitle = chapterTitle;
        this.chapterNumber = chapterNumber;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.downloadStatus = downloadStatus;
    }

    public static final Creator<Chapter> CREATOR = new Creator<Chapter>()
    {
        @Override
        public Chapter createFromParcel(Parcel in)
        {
            return new Chapter(in);
        }

        @Override
        public Chapter[] newArray(int size)
        {
            return new Chapter[size];
        }
    };

    public String getChapterUrl()
    {
        return url;
    }

    public void setChapterUrl(String aUrl)
    {
        url = aUrl;
    }

    public String getChapterDate()
    {
        return date;
    }

    public void setChapterDate(String aDate)
    {
        date = aDate;
    }

    public String getChapterTitle()
    {
        return chapterTitle;
    }

    public void setChapterTitle(String aTitle)
    {
        chapterTitle = aTitle;
    }

    public int getChapterNumber()
    {
        return chapterNumber;
    }

    public void setChapterNumber(int aNum)
    {
        chapterNumber = aNum;
    }

    public String toString()
    {
        return chapterTitle;
    }

    public String getMangaTitle()
    {
        return mangaTitle;
    }

    public void setMangaTitle(String aTitle)
    {
        mangaTitle = aTitle;
    }

    public int getCurrentPage()
    {
        return currentPage;
    }

    public void setCurrentPage(int aCurrentPage)
    {
        currentPage = aCurrentPage;
    }

    public int getTotalPages()
    {
        return totalPages;
    }

    public void setTotalPages(int aTotlePages)
    {
        totalPages = aTotlePages;
    }

    public String getUrl()
    {
        return this.url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getDate()
    {
        return this.date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public boolean getMDownloadChecked() {
        return this.mDownloadChecked;
    }

    public void setMDownloadChecked(boolean mDownloadChecked) {
        this.mDownloadChecked = mDownloadChecked;
    }

    public String getMangaUrl() {
        return this.mangaUrl;
    }

    public void setMangaUrl(String mangaUrl) {
        this.mangaUrl = mangaUrl;
    }

    public int getDownloadStatus() {
        return this.downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }


}
