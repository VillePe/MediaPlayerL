package com.vp.mplayerl;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.vp.mplayerl.misc.Artist;
import com.vp.mplayerl.misc.ArtistAdapter;
import com.vp.mplayerl.misc.Track;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ArtistsListActivity extends AppCompatActivity {

    ListView listView;
    ArtistAdapter artistAdapter;
    private MediaPlayerService mediaPlayerService;
    private boolean serviceBound;
    private Intent mpServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artists);

        artistAdapter = new ArtistAdapter(this, getLayoutInflater());

        listView = (ListView) findViewById(R.id.main_listview);
        listView.setAdapter(artistAdapter);
        setOnClickListener(listView);

        if (getIntent().getBundleExtra(MediaPlayerService.SERVICE_BINDER_KEY) == null) {
            mpServiceIntent = new Intent(this, MediaPlayerService.class);

            bindToMPService();
        } else {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder)getIntent().getBundleExtra(MediaPlayerService.SERVICE_BINDER_KEY).getBinder(MediaPlayerService.SERVICE_BINDER_KEY);
            mediaPlayerService = binder.getService();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Artists");

        InitializeTracksFromStorage();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (serviceBound) {
            unbindService(connection);
            stopService(mpServiceIntent);
            serviceBound = false;
        }
        Log.d("ArtistsListActivity", "Activity stopped");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artists_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_playlist) {
            if (mediaPlayerService != null) {
                final Intent intentOpenPlaybackActivity = new Intent(this, PlaybackActivity.class);
                intentOpenPlaybackActivity.putExtra(MediaPlayerService.SERVICE_BINDER_KEY, MediaPlayerService.createBinderBundle(mediaPlayerService.getBinder()));
                Track currentTrack = mediaPlayerService.getCurrentTrack();
                if (currentTrack != null) {
                    intentOpenPlaybackActivity.putExtra(MediaPlayerService.TRACK_BUNDLE_KEY, MediaPlayerService.createTrackBundle(mediaPlayerService.getCurrentTrack()));

                    startActivity(intentOpenPlaybackActivity);
                    return true;
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void setMediaPlayerService(MediaPlayerService service) {
        this.mediaPlayerService = service;
    }

    private void bindToMPService() {
        boolean bindingSuccessful = bindService(mpServiceIntent, connection, Context.BIND_AUTO_CREATE);

        Log.d("ArtistsListActivity", "Binded to service: " + bindingSuccessful);
    }

    private void setOnClickListener(ListView lView) {
        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle artistBundle = new Bundle();
                Bundle serviceBundle = new Bundle();
                Artist a = (Artist) artistAdapter.getItem(position);
                artistBundle.putSerializable("artist", a);
                serviceBundle.putBinder(MediaPlayerService.SERVICE_BINDER_KEY, mediaPlayerService.getBinder());

                Intent intent = new Intent(getApplicationContext(), TracksListActivity.class);
                intent.putExtra("artist", artistBundle);
                intent.putExtra(MediaPlayerService.SERVICE_BINDER_KEY, serviceBundle);
                startActivity(intent);
            }
        });
    }

    private void InitializeTracksFromStorage() {

        Log.i("TrackInitializer", "Initializing artists...");
        ContentResolver cResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = cResolver.query(uri, null, null, null, null);

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

            Collections.sort(artistsList, new Comparator<Artist>() {
                @Override
                public int compare(Artist lhs, Artist rhs) {
                    if (lhs.getName() == null) {
                        return 1;
                    } else if (rhs.getName() == null) {
                        return -1;
                    }
                    return lhs.getName().compareTo(rhs.getName());
                }
            });

            for (Artist a : artistsList) {
                artistAdapter.addArtist(a);
            }
            cursor.close();
        }
    }

    private Track parseTrack(String title, String artist, String album, String duration, File file) {
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

    private ArrayList<Artist> ParseTracksToArtistList(ArrayList<Track> tracks) {
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

    protected ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("ArtistsListActivity", "Service is connected!");
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("ArtistsListActivity", "Service is disconnected!");
            serviceBound = false;
        }
    };
}
