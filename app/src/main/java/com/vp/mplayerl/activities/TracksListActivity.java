package com.vp.mplayerl.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.vp.mplayerl.MediaPlayerService;
import com.vp.mplayerl.R;
import com.vp.mplayerl.Utils;
import com.vp.mplayerl.misc.Artist;
import com.vp.mplayerl.misc.Logger;
import com.vp.mplayerl.misc.Track;
import com.vp.mplayerl.misc.TrackAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Ville on 27.10.2016.
 */

public class TracksListActivity extends AppCompatActivity {

    TrackAdapter trackAdapter;
    MediaPlayerService mediaPlayerService;
    MediaPlayerService.LocalBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tracks_list_toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        if (mediaPlayerService == null) {
            binder = (MediaPlayerService.LocalBinder) getIntent().getBundleExtra(MediaPlayerService.SERVICE_BINDER_KEY)
                    .getBinder(MediaPlayerService.SERVICE_BINDER_KEY);
            if (binder != null) {
                mediaPlayerService = binder.getService();
            } else {
                Toast.makeText(this, "Could not bind to media player", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        trackAdapter = new TrackAdapter(this, getLayoutInflater());

        ListView tracksListView = (ListView) findViewById(R.id.tracks_list_listview);
        tracksListView.setAdapter(trackAdapter);
        setOnClickListener(tracksListView);
        registerForContextMenu(tracksListView);

        initializeTracksList();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tracks_list, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu_track, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Track pickedTrack = (Track)trackAdapter.getItem(info.position);
        mediaPlayerService.getPlaylist().addTrack(pickedTrack);
        return super.onContextItemSelected(item);
    }

    public void setMediaPlayerService(MediaPlayerService service) {
        this.mediaPlayerService = service;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_playback:
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
                break;
            case R.id.action_playlist:
                if (mediaPlayerService != null) {
                    startActivity(PlaylistActivity.createPlaylistActivityIntent(this, mediaPlayerService));
                }
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    private void setOnClickListener(ListView lView) {
        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle serviceBundle = new Bundle();

                Track t = (Track) trackAdapter.getItem(position);

                serviceBundle.putBinder(MediaPlayerService.SERVICE_BINDER_KEY, mediaPlayerService.getBinder());

                Log.i("INFO", "Opening new activity");

                final Intent intentOpenPlaybackActivity = new Intent(getApplicationContext(), PlaybackActivity.class);
                intentOpenPlaybackActivity.putExtra(MediaPlayerService.SERVICE_BINDER_KEY, serviceBundle);
                intentOpenPlaybackActivity.putExtra(MediaPlayerService.TRACK_BUNDLE_KEY, MediaPlayerService.createTrackBundle(t));
                startActivity(intentOpenPlaybackActivity);
            }
        });
        lView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });
    }

    private void initializeTracksList() {
        Bundle bundle = getIntent().getBundleExtra("artist");
        if (bundle == null) {
            Log.w("InitializeTracks", "Bundle was NULL!");
        } else {
            Artist artist = (Artist)bundle.getSerializable("artist");
            if (artist == null) {
                Log.w("Deserialize", "Artist in bundle was NULL!");
            } else {
                fillListWithArtist(artist);
            }
        }
    }

    private void fillListWithArtist(Artist a ) {
        ArrayList<Track> tracksSorted = a.getTracks();
        Collections.sort(tracksSorted, new Utils.TrackComparator());
        if (tracksSorted.size() > 0) {
            for (Track t : tracksSorted) {
                trackAdapter.addTrack(t);
            }
        } else {
            Logger.log("Artist had no tracks!");
        }
    }
}

