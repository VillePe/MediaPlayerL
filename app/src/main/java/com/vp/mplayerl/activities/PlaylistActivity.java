package com.vp.mplayerl.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.vp.mplayerl.MediaPlayerService;
import com.vp.mplayerl.R;
import com.vp.mplayerl.misc.Artist;
import com.vp.mplayerl.misc.Playlist;
import com.vp.mplayerl.misc.Track;
import com.vp.mplayerl.misc.TrackAdapter;

/**
 * Created by Ville on 27.10.2016.
 */

public class PlaylistActivity extends AppCompatActivity {

    public static final String PLAYLIST_INTENT_EXTRA_NAME = "parcelable_playlist";

    TrackAdapter mTrackAdapter;
    MediaPlayerService mediaPlayerService;
    MediaPlayerService.LocalBinder binder;
    Playlist mPlaylist = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        Toolbar toolbar = (Toolbar) findViewById(R.id.playlist_toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        if (mediaPlayerService == null) {
            binder = (MediaPlayerService.LocalBinder) getIntent().getBundleExtra(MediaPlayerService.SERVICE_BINDER_KEY)
                    .getBinder(MediaPlayerService.SERVICE_BINDER_KEY);
            if (binder != null) {
                mediaPlayerService = binder.getService();
            } else {
                onBackPressed();
                return;
            }
        }

        if (mPlaylist == null) {
            mPlaylist = mediaPlayerService.getPlaylist();
        }

        mTrackAdapter = new TrackAdapter(this, getLayoutInflater());

        ListView tracksListView = (ListView) findViewById(R.id.playlist_listview);
        tracksListView.setAdapter(mTrackAdapter);
        setOnClickListener(tracksListView);

        initializeTracksList(mPlaylist);
    }

    public static Intent createPlaylistActivityIntent(Context ctx, MediaPlayerService service) {
        final Intent intentOpenPlaylistActivity = new Intent(ctx, PlaylistActivity.class);
        intentOpenPlaylistActivity.putExtra(MediaPlayerService.SERVICE_BINDER_KEY, MediaPlayerService.createBinderBundle(service.getBinder()));
        return intentOpenPlaylistActivity;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_playlist, menu);
        return true;
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

        if (id == R.id.action_playback) {
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

        switch (id) {
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

                Track t = (Track) mTrackAdapter.getItem(position);

                serviceBundle.putBinder(MediaPlayerService.SERVICE_BINDER_KEY, mediaPlayerService.getBinder());

                Log.i("INFO", "Opening new activity");

                final Intent intentOpenPlaybackActivity = new Intent(getApplicationContext(), PlaybackActivity.class);
                intentOpenPlaybackActivity.putExtra(MediaPlayerService.SERVICE_BINDER_KEY, serviceBundle);
                intentOpenPlaybackActivity.putExtra(MediaPlayerService.TRACK_BUNDLE_KEY, MediaPlayerService.createTrackBundle(t));
                startActivity(intentOpenPlaybackActivity);

            }
        });
    }

    private void initializeTracksList(Playlist playlist) {
        for (int i = 0; i < playlist.size(); i++) {
            Object track = playlist.getTrack(i);
            if (track != null) {
                mTrackAdapter.addTrack((Track) track);
            }
        }
        TrackAdapter.putTrackImagesAsync(mTrackAdapter);
    }

    private void fillListWithArtist(Artist a ) {
        if (a.getTracks() != null && a.getTracks().size() > 0) {
            for (Track t : a.getTracks()) {
                mTrackAdapter.addTrack(t);
            }
        } else {
            Log.d("FillTracks", "Artist had no tracks!");
        }
    }
}

