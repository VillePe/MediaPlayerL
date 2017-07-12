package com.vp.mplayerl.misc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.vp.mediafileparsers.ParseController;
import com.vp.mplayerl.MediaPlayerService;
import com.vp.parsers.flac.FlacParser;
import com.vp.parsers.flac.MetadataBlock;
import com.vp.parsers.flac.MetadataBlockCollection;
import com.vp.parsers.mp3.Mp3Parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;

/**
 * Created by Ville on 9.10.2016.
 */

public class Track implements Serializable, Comparable{
    private String mArtist;
    private String mTitle;
    private int mLengthInSeconds;
    private String mAlbum;
    private File mTrackFile;
    private String mLyrics;

    public Track() {
        this.mArtist = "Unknown";
        this.mTitle = "Unknown";
        this.mLengthInSeconds = 0;
        this.mAlbum = "Unknown";
        this.mTrackFile = null;
        this.mLyrics = "No lyrics";
    }

    public Track(File trackFile, String artist, String title, int lengthInSeconds) {
        this.mTrackFile = trackFile;
        this.mArtist = artist;
        this.mTitle = title;
        this.mAlbum = "Unknown";
        this.mLengthInSeconds = lengthInSeconds;
        this.mLyrics = "No lyrics";
    }

    public Track(File trackFile, String artist, String title, int lengthInSeconds, String album) {
        this.mTrackFile = trackFile;
        this.mArtist = artist;
        this.mTitle = title;
        this.mLengthInSeconds = lengthInSeconds;
        this.mAlbum = album;
        this.mLyrics = "No lyrics";
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {
        this.mArtist = artist;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public int getLengthInSeconds() {
        return mLengthInSeconds;
    }

    public void setLengthInSeconds(int lengthInSeconds) {
        this.mLengthInSeconds = lengthInSeconds;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(String album) {
        this.mAlbum = album;
    }

    public String getLyrics() {
        return mLyrics;
    }

    public void setLyrics(String lyrics) {
        this.mLyrics = lyrics;
    }

    public Bitmap getLargeBitmap() {
        Bitmap bMap = null;
        ByteArrayInputStream inputStream = ParseController.getPictureFromFile(getTrackFile());
        bMap = BitmapFactory.decodeStream(inputStream);
        return bMap;
    }

    public Bitmap getScaledBitmap(int width, int height) {
        Bitmap largeBitmap = getLargeBitmap();
        if (largeBitmap != null) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(largeBitmap, width, height, false);

            // CreateScaledBitmap returns the same object if it does no scaling
            if (scaledBitmap == largeBitmap) return largeBitmap;
            largeBitmap.recycle();
            largeBitmap = null;
            return scaledBitmap;
        }
        return null;
    }

    public Bitmap getSmallBitmap() {
        return getScaledBitmap(128, 128);
    }

    public File getTrackFile() {
        return mTrackFile;
    }

    public void setTrackFile(File trackFile) {
        this.mTrackFile = trackFile;
    }

    @Override
    public boolean equals(Object another) {
        if (another == null) {
            return false;
        }
        if (!(another instanceof Track)) {
            return false;
        }
        Track paramTrack = (Track)another;
        return this.getArtist().equals(paramTrack.getArtist()) && this.getTitle().equals(paramTrack.getTitle());
    }

    @Override
    public int compareTo(@NonNull Object another) {
        Track paramTrack = (Track)another;
        if (this.getArtist().compareTo(paramTrack.getArtist()) == 0)  {
            return this.getTitle().compareTo(paramTrack.getArtist());
        } else {
            return this.getArtist().compareTo(paramTrack.getArtist());
        }
    }

    public static Track getTrackFromIntent(Intent intent) {
        if (intent.getExtras() != null) {
            Bundle bundle = intent.getExtras().getBundle(MediaPlayerService.TRACK_BUNDLE_KEY);
            Track t = null;
            if (bundle != null) {
                t = (Track) bundle.getSerializable(MediaPlayerService.TRACK_BUNDLE_KEY);
            }
            if (t != null) {
                return t;
            } else {
                Logger.log("Could not deserialize track!");
                return null;
            }

        } else {
            Logger.log("Extras in intent was NULL");
            return null;
        }
    }
}
