package com.vp.mplayerl.misc;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Ville on 9.10.2016.
 */

public class Artist implements Serializable{

    private String name;
    private ArrayList<Album> albums;
    private ArrayList<Track> tracks;

    public Artist() {
        this.name = "Unknown";
        this.albums = new ArrayList<>();
        this.tracks = new ArrayList<>();
    }

    public Artist(String name) {
        this.name = name;
        this.albums = new ArrayList<>();
        this.tracks = new ArrayList<>();
    }

    public Artist(String name, ArrayList<Album> albums) {
        this.name = name;
        this.albums = albums;
        this.tracks = new ArrayList<>();
    }

    public Artist(String name, ArrayList<Album> albums, ArrayList<Track> tracks) {
        this.name = name;
        this.albums = albums;
        this.tracks = tracks;
    }

    public void addTrack(Track t) {
        if (t != null) {
            tracks.add(t);
        }
    }

    public void addAlbum(Album a) {
        if (a != null) {
            albums.add(a);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(ArrayList<Album> albums) {
        this.albums = albums;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public void setTracks(ArrayList<Track> tracks) {
        this.tracks = tracks;
    }
}
