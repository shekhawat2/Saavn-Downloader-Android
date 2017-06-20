package com.arunkr.saavn.downloader.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import org.apache.commons.lang3.StringEscapeUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.arunkr.saavn.downloader.activity_frag.MainActivity.decrypter;

/**
 * Created by Arun Kumar Shreevastava on 23/10/16.
 */

public class SongInfo implements Parcelable
{
    private String download_url,album_name,song_name,download_folder,albumArtUrl;
    private String artist_name,year,language;
    private String extension;

    public SongInfo()
    {

    }

    public String getDownload_url()
    {
        return download_url;
    }

    public void setDownload_url(String encrypted_url, final boolean highest_quality)
    {
        byte[] array = Base64.decode(encrypted_url.trim(), 0);
        String decrypted_url = null;
        try
        {
            decrypted_url = new String(decrypter.doFinal(array));
        } catch (IllegalBlockSizeException e)
        {
            e.printStackTrace();
        } catch (BadPaddingException e)
        {
            e.printStackTrace();
        }
        final String temp_url = decrypted_url;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                download_url = getDownloadLink(temp_url,highest_quality);
            }
        }).start();
    }

    public String getDownload_folder()
    {
        return download_folder;
    }

    public void setDownload_folder(String download_folder)
    {
        this.download_folder = StringEscapeUtils.unescapeXml(StringEscapeUtils.unescapeHtml3(download_folder))
                .replaceAll("[\\\\/:*?\"<>|]","").trim();
    }

    public void setDownload_folder(String show_title, String season_title)
    {
        this.download_folder = StringEscapeUtils.unescapeXml(StringEscapeUtils.unescapeHtml3(show_title))
                .replaceAll("[\\\\/:*?\"<>|]","") + "/" +
                StringEscapeUtils.unescapeXml(StringEscapeUtils.unescapeHtml3(season_title))
                .replaceAll("[\\\\/:*?\"<>|]","").trim();
    }

    public String getAlbum_name()
    {
        return album_name;
    }

    public void setAlbum_name(String album_name)
    {
        this.album_name = StringEscapeUtils.unescapeXml(StringEscapeUtils.unescapeHtml3(album_name))
                .replaceAll("[\\\\/:*?\"<>|]", "").trim();
    }

    public String getSong_name()
    {
        return song_name;
    }

    public void setSong_name(String song_name)
    {
        //this.song_name = Html.fromHtml(song_name).toString();
        this.song_name = StringEscapeUtils.unescapeXml(StringEscapeUtils.unescapeHtml3(song_name))
                .replaceAll("[\\\\/:*?\"<>|]","").trim();
    }

    private String getDownloadLink(String link,boolean higest_quality)
    {
        String songLink = link;

        int index =link.lastIndexOf('.');

        if(index==-1)
            return "";

        extension = link.substring(index);
        if(extension.equals(".mp4"))
            extension = ".m4a";

        //String s = "http://aac.saavncdn.com/480/dec8a44e725e68c682962df67e7909dc_160.mp4";
        Matcher m = Pattern.compile("(.+/[^/_]+_)[\\d]+(\\..+)$").matcher(link);
        if(m.find())
        {
            if(higest_quality)
            {
                songLink = m.replaceFirst("$1320$2");
                try
                {
                    //from stackoverflow
                    //as the call is made from a thread other than main thread we can
                    //do network operations
                    HttpURLConnection.setFollowRedirects(false);
                    HttpURLConnection con = (HttpURLConnection) new URL(songLink).openConnection();
                    //HEAD to just check if the URL exists
                    con.setRequestMethod("HEAD");
                    if ((con.getResponseCode() == HttpURLConnection.HTTP_OK))
                        return songLink;
                } catch (Exception e)
                {
                    return m.replaceFirst("$1160$2");
                }
            }
            else
            {
                return m.replaceFirst("$1160$2");
            }
        }

        return songLink;
    }

    public String getArtist_name()
    {
        return artist_name;
    }

    public void setArtist_name(String artist_name)
    {
        this.artist_name = artist_name.trim();
    }

    public String getYear()
    {
        return year;
    }

    public void setYear(String year)
    {
        this.year = year.trim();
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language.trim();
    }

    public String getExtension()
    {
        return extension;
    }

    public String getAlbumArtUrl()
    {
        return albumArtUrl;
    }

    public void setAlbumArtUrl(String albumArtUrl)
    {
        //http://c.saavncdn.com/569/M-S-Dhoni-The-Untold-Story-3-Hindi-2016-150x150.jpg
        //hacky way but should work
        this.albumArtUrl = albumArtUrl.replace("150x150","500x500");
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.download_url);
        dest.writeString(this.album_name);
        dest.writeString(this.song_name);
        dest.writeString(this.download_folder);
        dest.writeString(this.artist_name);
        dest.writeString(this.year);
        dest.writeString(this.language);
        dest.writeString(this.extension);
        dest.writeString(this.albumArtUrl);
    }

    protected SongInfo(Parcel in)
    {
        this.download_url = in.readString();
        this.album_name = in.readString();
        this.song_name = in.readString();
        this.download_folder = in.readString();
        this.artist_name = in.readString();
        this.year = in.readString();
        this.language = in.readString();
        this.extension = in.readString();
        this.albumArtUrl = in.readString();
    }

    public static final Parcelable.Creator<SongInfo> CREATOR = new Parcelable.Creator<SongInfo>()
    {
        @Override
        public SongInfo createFromParcel(Parcel source)
        {
            return new SongInfo(source);
        }

        @Override
        public SongInfo[] newArray(int size)
        {
            return new SongInfo[size];
        }
    };
}
