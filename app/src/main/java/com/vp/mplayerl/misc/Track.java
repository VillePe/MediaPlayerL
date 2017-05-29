package com.vp.mplayerl.misc;

import android.media.Image;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Ville on 9.10.2016.
 */

public class Track implements Serializable, Comparable{
    private String artist;
    private String title;
    private int lengthInSeconds;
    private String album;
    private File trackFile;
    private String lyrics;

    public Track() {
        this.artist = "Unknown";
        this.title = "Unknown";
        this.lengthInSeconds = 0;
        this.album = "Unknown";
        this.trackFile = null;
        this.lyrics = "No lyrics";
    }

    public Track(File trackFile, String artist, String title, int lengthInSeconds) {
        this.trackFile = trackFile;
        this.artist = artist;
        this.title = title;
        this.album = "Unknown";
        this.lengthInSeconds = lengthInSeconds;
        this.lyrics = "No lyrics";
    }

    public Track(File trackFile, String artist, String title, int lengthInSeconds, String album) {
        this.trackFile = trackFile;
        this.artist = artist;
        this.title = title;
        this.lengthInSeconds = lengthInSeconds;
        this.album = album;
        this.lyrics = "No lyrics";
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLengthInSeconds() {
        return lengthInSeconds;
    }

    public void setLengthInSeconds(int lengthInSeconds) {
        this.lengthInSeconds = lengthInSeconds;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public File getTrackFile() {
        return trackFile;
    }

    public void setTrackFile(File trackFile) {
        this.trackFile = trackFile;
    }

    @Override
    public boolean equals(Object another) {
        if (another == null) {
            return false;
        }
        Track paramTrack = (Track)another;
        if (paramTrack == null) {
            return false;
        }
        return this.getArtist().equals(paramTrack.getArtist()) && this.getTitle().equals(paramTrack.getTitle());
    }

    @Override
    public int compareTo(Object another) {
        if (another == null) {
            return -1;
        }
        Track paramTrack = (Track)another;
        if (paramTrack == null) {
            return -1;
        }
        if (this.getArtist().compareTo(paramTrack.getArtist()) == 0)  {
            return this.getTitle().compareTo(paramTrack.getArtist());
        } else {
            return this.getArtist().compareTo(paramTrack.getArtist());
        }
    }
}
