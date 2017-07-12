package com.vp.mplayerl.misc;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * Created by Ville on 29.5.2017.
 */

public class Playlist implements Parcelable{

    private ArrayList mTrackList;
    private Track mPreviousTrack;
    private boolean mSuffle;
    private Random mRandom;
    private int mCurrentTrackNumber = 0;
    private Stack<Integer> mPreviousTracks = new Stack<>();

    public Playlist(Parcel in) {
        this.mTrackList = (ArrayList) in.readSerializable();
        this.mCurrentTrackNumber = in.readInt();
        this.mSuffle = in.readInt() == 1;
    }

    public Playlist() {
        mRandom = new Random();
        this.mTrackList = new ArrayList();
    }

    public void addTrack(Track track) {
        if (!mTrackList.contains(track)) {
            mTrackList.add(track);
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
        int nextTrackIndx = mCurrentTrackNumber;
        nextTrackIndx++;
        if (nextTrackIndx >= mTrackList.size()) {
            nextTrackIndx = 0;
        }
        return getTrack(nextTrackIndx);
    }

    public Track getPreviousTrack() {
        int prevTrackIndx = mCurrentTrackNumber;
        if (!mPreviousTracks.empty()) {
            return (Track) mTrackList.get(mPreviousTracks.pop());
        }
        prevTrackIndx--;
        if (prevTrackIndx < 0) {
            prevTrackIndx = 0;
        }
        return getTrack(prevTrackIndx);
    }

    public Track getRandomTrack() {
        int randomNumber = mRandom.nextInt(mTrackList.size());
        return getTrack(randomNumber);
    }

    public Track getTrack(int index) {
        if (index < mTrackList.size()) {
            mPreviousTracks.push(getCurrentTrackNumber());
            Track track = (Track) mTrackList.get(index);
            setCurrentTrackNumber(index);
            return track;
        } else {
            return null;
        }
    }

    public void removeTrack(Track track) {
        int indx = mTrackList.indexOf(track);
        if (indx != -1) {
            mTrackList.remove(indx);
        }
        if (mPreviousTracks.contains(track)) {
            mPreviousTracks.remove(track);
        }
    }

    public int size() {
        return mTrackList.size();
    }

    public ArrayList getTrackList() {
        return mTrackList;
    }

    public boolean getSuffle() {
        return mSuffle;
    }

    public void setSuffle(boolean suffle) {
        this.mSuffle = suffle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(mTrackList);
        parcel.writeInt(mCurrentTrackNumber);
        parcel.writeInt(mSuffle ? 1 : 0);
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
        this.mTrackList.clear();
    }

    public int getCurrentTrackNumber() {
        return mCurrentTrackNumber;
    }

    public void setCurrentTrackNumber(int currentTrackNumber) {
        if (currentTrackNumber > mTrackList.size()) {
            getNextTrack();
        } else {
            this.mCurrentTrackNumber = currentTrackNumber;
        }
    }
}
