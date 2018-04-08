package com.amgregoire.mangafeed.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaFeedRest;
import com.amgregoire.mangafeed.Utils.MangaLogger;
import com.amgregoire.mangafeed.Utils.SharedPrefs;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

@Entity(nameInDb = "Manga")
public class Manga implements Parcelable
{
    public final static String TAG = "MANGA";


    public final static int UNFOLLOW = 0;
    public final static int FOLLOW_READING = 1;
    public final static int FOLLOW_COMPLETE = 2;
    public final static int FOLLOW_ON_HOLD = 3;
    public final static int FOLLOW_PLAN_TO_READ = 4;


    @Id(autoincrement = true)
    public Long _id;

    @Property(nameInDb = "title")
    public String title;

    @Property(nameInDb = "image")
    public String image;

    @Property(nameInDb = "link")
    public String link;

    @Property(nameInDb = "description")
    public String description;

    @Property(nameInDb = "author")
    public String author;

    @Property(nameInDb = "artist")
    public String artist;

    @Property(nameInDb = "genres")
    public String genres;

    @Property(nameInDb = "status")
    public String status;

    @Property(nameInDb = "source")
    public String source;

    @Property(nameInDb = "alternate")
    public String alternate;

    @Property(nameInDb = "following")
    public int following;

    @Property(nameInDb = "initialized")
    public int initialized;

    @Property(nameInDb = "recentChapter")
    public String recentChapter;

    public Manga()
    {

    }

    public Manga(String aTitle, String aUrl, String aSource)
    {
        title = aTitle;
        link = aUrl;
        source = aSource;
        initialized = 0;
        _id = null;
        recentChapter = "";
    }

    public Manga(Manga aIn)
    {
        _id = aIn._id;
        title = aIn.title;
        image = aIn.image;
        link = aIn.link;
        description = aIn.description;
        author = aIn.author;
        artist = aIn.artist;
        genres = aIn.genres;
        status = aIn.status;
        source = aIn.source;
        alternate = aIn.alternate;
        following = aIn.following;
        initialized = aIn.initialized;
        recentChapter = aIn.recentChapter;
    }

    protected Manga(Parcel aIn)
    {
        _id = aIn.readLong();
        title = aIn.readString();
        image = aIn.readString();
        link = aIn.readString();
        description = aIn.readString();

        author = aIn.readString();
        artist = aIn.readString();
        genres = aIn.readString();
        status = aIn.readString();
        source = aIn.readString();
        alternate = aIn.readString();
        following = aIn.readInt();
        initialized = aIn.readInt();
        recentChapter = aIn.readString();
    }

    @Generated(hash = 736454909)
    public Manga(Long _id, String title, String image, String link,
                 String description, String author, String artist, String genres,
                 String status, String source, String alternate, int following,
                 int initialized, String recentChapter)
    {
        this._id = _id;
        this.title = title;
        this.image = image;
        this.link = link;
        this.description = description;
        this.author = author;
        this.artist = artist;
        this.genres = genres;
        this.status = status;
        this.source = source;
        this.alternate = alternate;
        this.following = following;
        this.initialized = initialized;
        this.recentChapter = recentChapter;
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
        aDest.writeString(title);
        aDest.writeString(image);
        aDest.writeString(link);
        aDest.writeString(description);

        aDest.writeString(author);
        aDest.writeString(artist);
        aDest.writeString(genres);
        aDest.writeString(status);
        aDest.writeString(source);
        aDest.writeString(alternate);
        aDest.writeInt(following);
        aDest.writeInt(initialized);
        aDest.writeString(recentChapter);
    }

    public static final Creator<Manga> CREATOR = new Creator<Manga>()
    {
        @Override
        public Manga createFromParcel(Parcel aIn)
        {
            return new Manga(aIn);
        }

        @Override
        public Manga[] newArray(int aSize)
        {
            return new Manga[aSize];
        }
    };

    public Long get_id()
    {
        return _id;
    }

    public void set_id(Long aId)
    {
        _id = aId;
    }

    public String toString()
    {
        return title;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String aTitle)
    {
        title = aTitle;
    }

    public String getDescription() { return description;}

    public void setDescription(String aDesc) {description = aDesc;}

    public String getPicUrl()
    {
        return image;
    }

    public void setPicUrl(String aPicUrl)
    {
        image = aPicUrl;
    }

    public String getMangaURL()
    {
        return link;
    }

    public void setMangaUrl(String aUrl)
    {
        link = aUrl;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String aAuthor)
    {
        author = aAuthor;
    }

    public String getArtist()
    {
        return artist;
    }

    public void setArtist(String aArtist)
    {
        artist = aArtist;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String aStatus)
    {
        status = aStatus;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String aSource)
    {
        source = aSource;
    }

    public String getAlternate() { return alternate; }

    public void setAlternate(String aAlternate) { alternate = aAlternate; }

    public long getFollowing()
    {
        return following;
    }

    public boolean isFollowing()
    {
        return following > 0;
    }

    public int getFollowingValue()
    {
        return following;
    }

    public int setFollowing(int lVal)
    {
        following = lVal;
        MangaDB.getInstance().putManga(this);
        updateFollowItem();
        return following;
    }

    public int getInitialized()
    {
        return initialized;
    }

    public void setInitialized(int aInitialized)
    {
        initialized = aInitialized;
    }

    @Override
    public boolean equals(Object aObject)
    {
        boolean lCompare = false;
        if (aObject != null && aObject instanceof Manga)
        {
            if (link.equals(((Manga) aObject).getMangaURL()))
            {
                lCompare = true;
            }
        }

        return lCompare;
    }

    public String getImage()
    {
        return this.image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public String getLink()
    {
        return this.link;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public String getGenres()
    {
        return this.genres;
    }

    public void setGenres(String genres)
    {
        this.genres = genres;
    }

    public String getRecentChapter()
    {
        return this.recentChapter;
    }

    public void setRecentChapter(String recentChapter)
    {
        this.recentChapter = recentChapter;
    }


    /***
     * This function posts a follow update to the server
     */
    private void updateFollowItem()
    {
        int lUserId = SharedPrefs.getUserId();

        if(lUserId < 0)
        {
            return;
        }

        RequestParams params = new RequestParams();
        params.put("image", image);
        params.put("url", link);
        params.put("followStatus", following);

        MangaFeedRest.postFollowedUpdate(lUserId, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                super.onSuccess(statusCode, headers, response);
                MangaLogger.logError(TAG, response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse)
            {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

}
