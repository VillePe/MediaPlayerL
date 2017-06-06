package com.vp.mplayerl.misc;

import android.os.Parcel;
import android.os.Parcelable;

import com.vp.mplayerl.misc.Track;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Created by Ville on 29.5.2017.
 */

public class Playlist implements Parcelable{

    private ArrayList trackList;
    private Track previousTrack;
    private boolean suffle;
    private Random random;
    private int currentTrackNumber = 0;

    public Playlist(Parcel in) {
        this.trackList = (ArrayList) in.readSerializable();
        this.currentTrackNumber = in.readInt();
        this.suffle = in.readInt() == 1;
    }

    public Playlist() {
        random = new Random();
        this.trackList = new ArrayList();
    }

    public void addTrack(Track track) {
        if (!trackList.contains(track)) {
            trackList.add(track);
        }
    }

    public void addTracks(List<Track> tracks) {
        for (Track t : tracks) {
            addTrack(t);
        }
    }

    public void addTracks(Track[] tracks) {
        for (Track t : tracks) {
            addTrack(t);
        }
    }

    public Track getNextTrack() {
        currentTrackNumber++;
        if (currentTrackNumber >= trackList.size()) {
            currentTrackNumber = 0;
        }
        return getTrack(currentTrackNumber);
    }

    public Track getPreviousTrack() {
        currentTrackNumber--;
        if (currentTrackNumber < 0) {
            currentTrackNumber = 0;
        }
        return getTrack(currentTrackNumber);
    }

    public Track getRandomTrack() {
        int randomNumber = random.nextInt(trackList.size());
        return getTrack(randomNumber);
    }

    public Track getTrack(int index) {
        if (index < trackList.size()) {
            Track track = (Track) trackList.get(index);
            previousTrack = track;
            return track;
        } else {
            return null;
        }
    }

    public void removeTrack(Track track) {
        int indx = trackList.indexOf(track);
        if (indx != -1) {
            trackList.remove(indx);
        }
    }

    public int size() {
        return trackList.size();
    }

    public ArrayList getTrackList() {
        return trackList;
    }

    public boolean getSuffle() {
        return suffle;
    }

    public void setSuffle(boolean suffle) {
        this.suffle = suffle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(trackList);
        parcel.writeInt(currentTrackNumber);
        parcel.writeInt(suffle ? 1 : 0);
    }

    public static final Parcelable.Creator<Playlist> CREATOR = new Parcelable.Creator<Playlist>() {
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    public void clear() {
        this.trackList.clear();
    }
}
