package com.amgregoire.mangafeed.Models;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

@Entity(nameInDb = "Chapter")
public class Chapter implements Parcelable
{
    public final static String TAG = "CHAPTER";

    public final static int DOWNLOAD_STATUS_NONE = 0;
    public final static int DOWNLOAD_STATUS_DOWNLOADING = 1;
    public final static int DOWNLOAD_STATUS_FINISHED = 2;

    @Id(autoincrement = true)
    public Long _id;

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
        _id = null;
    }

    public Chapter(String aUrl, String aMangaTitle, String aChapterTitle, String aDate, int aNum, String aMangaUrl)
    {
        url = aUrl;
        date = aDate;
        mangaTitle = aMangaTitle;
        chapterTitle = aChapterTitle;
        chapterNumber = aNum;
        mangaUrl = aMangaUrl;
        _id = null;
    }

    public Chapter(String aUrl, String aMangaTitle, String aChapterTitle, String aDate, String aMangaUrl)
    {
        url = aUrl;
        date = aDate;
        mangaTitle = aMangaTitle;
        chapterTitle = aChapterTitle;
        mangaUrl = aMangaUrl;
        _id = null;
    }

    /***
     * Function copies the contents of a chapter, but retains the current chapters id.
     *
     * @param chapter
     * @return
     */
    public Chapter copy(Chapter chapter)
    {
        url = chapter.url;
        date = chapter.date;
        mangaTitle = chapter.mangaTitle;
        chapterTitle = chapter.chapterTitle;
        mangaUrl = chapter.mangaUrl;
        currentPage = chapter.currentPage;
        chapterNumber = chapter.chapterNumber;
        totalPages = chapter.totalPages;
        downloadStatus = chapter.downloadStatus;

        return this;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel aDest, int aFlags)
    {
        aDest.writeLong(_id);
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
        _id = aIn.readLong();
        url = aIn.readString();
        date = aIn.readString();
        mangaTitle = aIn.readString();
        chapterTitle = aIn.readString();
        chapterNumber = aIn.readInt();
        currentPage = aIn.readInt();
        totalPages = aIn.readInt();
    }

    @Generated(hash = 88779533)
    public Chapter(Long _id, String url, String date, String mangaTitle, String mangaUrl, String chapterTitle,
            int chapterNumber, int currentPage, int totalPages, int downloadStatus) {
        this._id = _id;
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

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }
}
