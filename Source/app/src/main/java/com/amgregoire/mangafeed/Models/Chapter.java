package com.amgregoire.mangafeed.Models;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "Chapter")
public class Chapter implements Parcelable
{
    public final static String TAG = "CHAPTER";

    @Property(nameInDb = "url")
    public String url;

    @Property(nameInDb = "date")
    public String date;

    @Property(nameInDb = "mangaTitle")
    public String mangaTitle;

    @Property(nameInDb = "chapterTitle")
    public String chapterTitle;

    @Property(nameInDb = "chapterNumber")
    public int chapterNumber;

    @Property(nameInDb = "currentPage")
    public int currentPage;

    @Property(nameInDb = "totalPages")
    private int totalPages;

    public boolean mDownloadChecked = false;

    public Chapter()
    {

    }

    public Chapter(String aTitle)
    {
        mangaTitle = aTitle;
        chapterTitle = aTitle;
    }

    public Chapter(String aUrl, String aMangaTitle, String aChapterTitle, String aDate, int aNum)
    {
        url = aUrl;
        date = aDate;
        mangaTitle = aMangaTitle;
        chapterTitle = aChapterTitle;
        chapterNumber = aNum;
    }

    public Chapter(String aUrl, String aMangaTitle, String aChapterTitle, String aDate)
    {
        url = aUrl;
        date = aDate;
        mangaTitle = aMangaTitle;
        chapterTitle = aChapterTitle;
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

    @Generated(hash = 1273349077)
    public Chapter(String url, String date, String mangaTitle, String chapterTitle, int chapterNumber,
            int currentPage, int totalPages, boolean mDownloadChecked) {
        this.url = url;
        this.date = date;
        this.mangaTitle = mangaTitle;
        this.chapterTitle = chapterTitle;
        this.chapterNumber = chapterNumber;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.mDownloadChecked = mDownloadChecked;
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


}
