package com.vp.mplayerl;

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

import com.vp.mplayerl.misc.Artist;
import com.vp.mplayerl.misc.Track;
import com.vp.mplayerl.misc.TrackAdapter;

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
            mediaPlayerService = binder.getService();
        }

        trackAdapter = new TrackAdapter(this, getLayoutInflater());

        ListView tracksListView = (ListView) findViewById(R.id.tracks_list_listview);
        tracksListView.setAdapter(trackAdapter);
        setOnClickListener(tracksListView);

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

    public void setMediaPlayerService(MediaPlayerService service) {
        this.mediaPlayerService = service;
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

                Track t = (Track) trackAdapter.getItem(position);

                serviceBundle.putBinder(MediaPlayerService.SERVICE_BINDER_KEY, mediaPlayerService.getBinder());

                Log.i("INFO", "Opening new activity");

                final Intent intentOpenPlaybackActivity = new Intent(getApplicationContext(), PlaybackActivity.class);
                intentOpenPlaybackActivity.putExtra(MediaPlayerService.SERVICE_BINDER_KEY, serviceBundle);
                intentOpenPlaybackActivity.putExtra(MediaPlayerService.TRACK_BUNDLE_KEY, MediaPlayerService.createTrackBundle(t));
                startActivity(intentOpenPlaybackActivity);

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
        if (a.getTracks() != null && a.getTracks().size() > 0) {
            for (Track t : a.getTracks()) {
                trackAdapter.addTrack(t);
            }
        } else {
            Log.d("FillTracks", "Artist had no tracks!");
        }
    }
}

