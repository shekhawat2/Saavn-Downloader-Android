package com.arunkr.saavn.downloader;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.compat.BuildConfig;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import java.io.File;

/**
 * Created by Arun Kumar Shreevastava on 15/12/16.
 */

public class DownloadCompleteReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.getAction().equals("android.intent.action.DOWNLOAD_COMPLETE"))
        {
            DatabaseHelper helper = new DatabaseHelper(context.getApplicationContext(), "metadata.db", null, DownloadService.DATABASE_VERSION);
            Bundle extras = intent.getExtras();
            Long downloaded_id = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
            SQLiteDatabase db = helper.getReadableDatabase();
            String query = "SELECT * FROM Metadata WHERE download_id = ?;";
            String[] whereArgs = new String[]{
                    downloaded_id.toString()
            };
            Cursor cursor = db.rawQuery(query, whereArgs);
            if (cursor.getCount() > 0)
            {
                try
                {
                    cursor.moveToFirst();
                    String rel_down_location = cursor.getString(cursor.getColumnIndexOrThrow("rel_down_location"));
                    File filename = new File(Utils.getSaveLocation(context), rel_down_location);
                    AudioFile f = AudioFileIO.read(filename);
                    Tag tag = f.getTagOrCreateAndSetDefault();

                    String title = rel_down_location.substring(rel_down_location.lastIndexOf('/')+1,
                            rel_down_location.lastIndexOf('.'));
                    tag.setField(FieldKey.TITLE, title);
                    tag.setField(FieldKey.ALBUM, cursor.getString(cursor.getColumnIndexOrThrow("album")));
                    tag.setField(FieldKey.ARTIST, cursor.getString(cursor.getColumnIndexOrThrow("artist")));
                    tag.setField(FieldKey.YEAR, cursor.getString(cursor.getColumnIndexOrThrow("year")));
                    tag.setField(FieldKey.LANGUAGE, cursor.getString(cursor.getColumnIndexOrThrow("language")));
                    tag.setField(FieldKey.GENRE, cursor.getString(cursor.getColumnIndexOrThrow("language")));

                    String album_art = cursor.getString(cursor.getColumnIndexOrThrow("album_art"));
                    Artwork art = ArtworkFactory.createArtworkFromFile(new File(
                            context.getExternalCacheDir(), album_art));
                    tag.deleteArtworkField();
                    tag.setField(art);

                    AudioFileIO.write(f);

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                db.execSQL("DELETE FROM Metadata WHERE download_id=?;",whereArgs);
            }

            cursor.close();
            db.close();
            helper.close();
        }
    }
}
