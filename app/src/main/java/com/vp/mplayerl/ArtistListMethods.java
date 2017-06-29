package com.vp.mplayerl;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.vp.mplayerl.misc.Artist;
import com.vp.mplayerl.misc.ArtistAdapter;
import com.vp.mplayerl.misc.Logger;
import com.vp.mplayerl.misc.Playlist;
import com.vp.mplayerl.misc.Track;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ArtistListMethods {

    public static ArrayList<Artist> initializeTracksFromStorage(ContentResolver resolver) {
        ArrayList<Artist> result = new ArrayList<>();

        Log.i("TrackInitializer", "Initializing artists...");
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = resolver.query(uri, null, null, null, null);

        if (cursor == null) {
            Log.w("CURSOR", "Cursor object was null");
        } else if (!cursor.moveToFirst()) {
            Log.i("CURSOR", "Moving the cursor to the first row failed");
        } else {
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int fileColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            ArrayList<Track> tracks = new ArrayList<>();
            do {
                Track newTrack = new Track();
                String thisTitle = cursor.getString(titleColumn);
                String thisArtist = cursor.getString(artistColumn);
                String thisAlbum = cursor.getString(albumColumn);
                String thisDuration = cursor.getString(durationColumn);
                File trackFile = new File(cursor.getString(fileColumn));

                newTrack = parseTrack(thisTitle, thisArtist, thisAlbum, thisDuration, trackFile);

                tracks.add(newTrack);
            } while (cursor.moveToNext());

            ArrayList<Artist> artistsList = ParseTracksToArtistList(tracks);

            Collections.sort(artistsList, new Utils.ArtistComparator());

            for (Artist a : artistsList) {
                result.add(a);
            }
            cursor.close();
        }
        return result;
    }

    private static Track parseTrack(String title, String artist, String album, String duration, File file) {
        Track newTrack = new Track();
        if (title != null)  newTrack.setTitle(title);
        if (artist != null) newTrack.setArtist(artist);
        if (album != null)  newTrack.setAlbum(album);

        int durationInSeconds = 0;
        if (duration != null && !duration.isEmpty()) {
            durationInSeconds = Integer.parseInt(duration) / 1000;
            newTrack.setLengthInSeconds(durationInSeconds);
        }
        newTrack.setTrackFile(file);
        return newTrack;
    }

    private static ArrayList<Artist> ParseTracksToArtistList(ArrayList<Track> tracks) {
        HashMap<String, Artist> artistMap = new HashMap<>();
        ArrayList<Artist> artists = new ArrayList<>();
        for (Track t : tracks) {
            if (!artistMap.containsKey(t.getArtist())) {
                artistMap.put(t.getArtist(), new Artist(t.getArtist()));
                artistMap.get(t.getArtist()).addTrack(t);
            } else {
                artistMap.get(t.getArtist()).addTrack(t);
            }
        }

        for (Artist a : artistMap.values()) {
            artists.add(a);
        }

        return artists;
    }
}
