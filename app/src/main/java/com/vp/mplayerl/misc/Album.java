package com.vp.mplayerl.misc;

import java.util.ArrayList;

/**
 * Created by Ville on 9.10.2016.
 */

public class Album {

    private String name;
    private String artist;
    private ArrayList<Track> tracks;

    public Album(String name, String artist) {
        this.name = name;
        this.artist = artist;
        this.tracks = new ArrayList<>();
    }

    public Album(String name, String artist, ArrayList<Track> tracks) {
        this.name = name;
        this.artist = artist;
        this.tracks = tracks;
    }

    public void AddTrack(Track track) {
        tracks.add(track);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public void setTracks(ArrayList<Track> tracks) {
        this.tracks = tracks;
    }
}
