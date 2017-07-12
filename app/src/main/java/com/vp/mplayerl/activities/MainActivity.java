package com.vp.mplayerl.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vp.mplayerl.ArtistListMethods;
import com.vp.mplayerl.MediaPlayerService;
import com.vp.mplayerl.R;
import com.vp.mplayerl.Utils;
import com.vp.mplayerl.misc.Artist;
import com.vp.mplayerl.misc.ArtistAdapter;
import com.vp.mplayerl.misc.Logger;
import com.vp.mplayerl.misc.OnMediaEventListener;
import com.vp.mplayerl.misc.Playlist;
import com.vp.mplayerl.misc.Track;
import com.vp.mplayerl.misc.TrackAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMediaEventListener {

    public final static int PERMISSIONS_READ_EXT_STORAGE = 1;
    public final static int PERMISSIONS_WRITE_EXT_STORAGE = 2;
    public final static int PERMISSIONS_INTERNET = 3;

    private View mPlaybackPopup;
    ArrayList<Artist> mArtists;
    private MediaPlayerService mMediaPlayerService;
    private boolean mServiceBound;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private Intent mpServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mArtists = ArtistListMethods.initializeTracksFromStorage(getContentResolver());
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.main_pager);
        mTabLayout.addTab(mTabLayout.newTab().setText("Artistit"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Kappaleet"));
        mOnPageChangeListener = new TabLayout.TabLayoutOnPageChangeListener(mTabLayout);
        MainActivityTabHandlingListeners listeners = new MainActivityTabHandlingListeners(mTabLayout, mViewPager);
        mTabLayout.setOnTabSelectedListener(listeners);
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);

        mPlaybackPopup = findViewById(R.id.playback_popup_layout);
        setOnPopupClickListener(mPlaybackPopup);

        if (getIntent().getBundleExtra(MediaPlayerService.SERVICE_BINDER_KEY) == null) {
            Logger.log("Could not find binder from intent extra, binding to service...");
            mpServiceIntent = new Intent(this, MediaPlayerService.class);
            startService(mpServiceIntent);
            bindToMPService(mpServiceIntent, connection);
        } else {
            Logger.log("Service binder found from intent extras. Binding to service...");
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder)getIntent().getBundleExtra(MediaPlayerService.SERVICE_BINDER_KEY).getBinder(MediaPlayerService.SERVICE_BINDER_KEY);
            if (binder != null) {
                mMediaPlayerService = binder.getService();
            } else {
                Logger.log("MainActivity: Binder was null!");
                finish();
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager.setAdapter(new TabFragmentAdapter(getSupportFragmentManager()));
        setFabClickListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        togglePlaybackPopup();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.log("MainActivity stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindService();
        Logger.log("MainActivity destroyed");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_playback:
                openPlaybackActivity();
                break;
            case R.id.action_playlist:
                if (mMediaPlayerService != null) {
                    final Intent intentOpenPlaylistActivity = new Intent(this, PlaylistActivity.class);
                    intentOpenPlaylistActivity.putExtra(MediaPlayerService.SERVICE_BINDER_KEY, MediaPlayerService.createBinderBundle(mMediaPlayerService.getBinder()));
                    startActivity(intentOpenPlaylistActivity);
                }
                break;
            case R.id.action_play_all:
                playAll();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void unBindService() {
        Logger.log("MainActivity: Service is bound: " + mServiceBound);
        Logger.log("Unbinding service...");
        if (mServiceBound) {
            unbindService(connection);
            mServiceBound = false;
            Logger.log("Service unbound");
        } else {
            Logger.log("Service already unbound");
        }

        Logger.log("Stopping service...");
        if (mMediaPlayerService != null && !mMediaPlayerService.isMediaPlaying()) {
            stopService(mpServiceIntent);
            Logger.log("Service stopped");
        } else {
            Logger.log("Media player still playing, stopping service cancelled");
        }
    }

    private void onMediaPlayerServiceInitialization(MediaPlayerService mediaPlayerService) {
        this.mMediaPlayerService = mediaPlayerService;
        this.mMediaPlayerService.addOnTrackChangedListener(this);
        togglePlaybackPopup();
    }

    public ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.log("Service is connected!");
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            onMediaPlayerServiceInitialization(binder.getService());
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.log("Service is disconnected!");
            mServiceBound = false;
        }
    };

    /**
     * Opens the playback activity. Method gets the track for playback activity from mediaplayer service.
     * If there is no track initialized in service, nothing will happen and method returns false
     * @return True if opening was successful. False otherwise
     */
    private boolean openPlaybackActivity() {
        if (mMediaPlayerService != null) {
            final Intent intentOpenPlaybackActivity = new Intent(this, PlaybackActivity.class);
            intentOpenPlaybackActivity.putExtra(MediaPlayerService.SERVICE_BINDER_KEY, MediaPlayerService.createBinderBundle(mMediaPlayerService.getBinder()));
            Track currentTrack = mMediaPlayerService.getCurrentTrack();
            if (currentTrack != null) {
                intentOpenPlaybackActivity.putExtra(MediaPlayerService.TRACK_BUNDLE_KEY, MediaPlayerService.createTrackBundle(mMediaPlayerService.getCurrentTrack()));

                startActivity(intentOpenPlaybackActivity);
                return true;
            }
        }
        return false;
    }

    private boolean openPlaybackActivity(Track track) {
        if (mMediaPlayerService != null) {
            mMediaPlayerService.setCurrentTrack(track);
            return openPlaybackActivity();
        }
        return false;
    }

    private void setFabClickListener() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_play_all);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAll();
            }
        });
    }

    private void playAll() {
        if (mMediaPlayerService != null && mArtists != null && mArtists.size() > 0) {
            Playlist playlist = mMediaPlayerService.getPlaylist();
            if (playlist != null) {
                playlist.clear();
                for (int i = 0; i < mArtists.size(); i++) {
                    Artist a = mArtists.get(i);
                    addArtistToPlaylist(a, playlist);
                }
                playlist.setSuffle(true);
                mMediaPlayerService.performAction(MediaPlayerService.ACTION_PLAY_ON_PREPARED, playlist.getRandomTrack());
//                openPlaybackActivity();
            }
        } else {
            Toast.makeText(this, "Tiedostojen toistaminen ei onnistunut, koita hetken päästä uudelleen", Toast.LENGTH_LONG).show();
            Logger.log("Could not play all files. Mediaplayer was null or mTrack list had no items!");
        }
    }

    private void bindToMPService(Intent serviceIntent, ServiceConnection sConnection) {
        boolean bindingSuccessful = bindService(serviceIntent, sConnection, Context.BIND_AUTO_CREATE);
        Logger.log("Binded to service: " + bindingSuccessful);
    }

    private void addArtistToPlaylist(Artist artist, Playlist playlist) {
        for (Track t : artist.getTracks()) {
            playlist.addTrack(t);
        }
    }

    private void startPlaybackWithArtist(Artist artist) {
        Playlist playlist = mMediaPlayerService.getPlaylist();
        playlist.clear();
        addArtistToPlaylist(artist, playlist);
        Intent intent = new Intent(this, PlaybackActivity.class);
        intent.putExtra(MediaPlayerService.TRACK_BUNDLE_KEY, MediaPlayerService.createTrackBundle(playlist.getTrack(0)));
        intent.putExtra(MediaPlayerService.SERVICE_BINDER_KEY, MediaPlayerService.createBinderBundle(mMediaPlayerService.getBinder()));
        startActivity(intent);
    }

    private void setOnPopupClickListener(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlaybackActivity();
            }
        });
    }

    private void togglePlaybackPopup( ) {
        if (mMediaPlayerService != null && mMediaPlayerService.isMediaPlaying()) {
            Track currentTrack = mMediaPlayerService.getCurrentTrack();
            TextView artistText = (TextView) findViewById(R.id.playback_popup__artist);
            TextView titleText = (TextView) findViewById(R.id.playback_popup__title);
            ImageView imgView = (ImageView) findViewById(R.id.playback_popup_album_image);
            Utils.setTrackImageToImageView(currentTrack, imgView, true);
            if (!currentTrack.getArtist().equals("<unknown>")) {
                artistText.setText(currentTrack.getArtist());
            } else {
                artistText.setText("");
            }
            titleText.setText(currentTrack.getTitle());

            mPlaybackPopup.setVisibility(View.VISIBLE);
            Logger.log("Showing popup!");
        } else {
            mPlaybackPopup.setVisibility(View.GONE);
            Logger.log("Mediaplayer service is null: " + (mMediaPlayerService == null));
            if (mMediaPlayerService != null) {
                Logger.log("Mediaplayer is playing: " + mMediaPlayerService.isMediaPlaying());
            }
            Logger.log("Hiding popup!");
        }
    }

    @Override
    public void onTrackChanged(Track NextTrack) {
        togglePlaybackPopup();
    }

    @Override
    public void onPlayerAction(String action) {
        togglePlaybackPopup();
    }

    @Override
    public void onPlayerStart() {
        togglePlaybackPopup();
    }


    private class TabFragmentAdapter extends FragmentPagerAdapter {

        public TabFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Logger.log("POSITIO: " + position);
            switch (position) {
                case 0:
                    return new ArtistFragment();
                case 1:
                    return new TrackListFragment();
            }
            return new ArtistFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @SuppressLint("ValidFragment")
    public class ArtistFragment extends ListFragment {

        @Override
        public ListAdapter getListAdapter() {
            ArtistAdapter adapter = new ArtistAdapter(getContext(), getLayoutInflater(null));
            adapter.setArtists(mArtists);
            Logger.log("RETURNING ADAPTER");
            return adapter;
        }

        @Override
        public ListView getListView() {
            return super.getListView();
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);
            getMenuInflater().inflate(R.menu.context_menu_artist, menu);
        }

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Artist pickedArtist = (Artist)getListAdapter().getItem(info.position);
            switch (item.getItemId()) {
                case R.id.action_add_artist_to_playlist:
                    addArtistToPlaylist(pickedArtist, mMediaPlayerService.getPlaylist());
                    break;
                case R.id.action_add_artist_to_playlist_and_play:
                    startPlaybackWithArtist(pickedArtist);

            }
            return super.onContextItemSelected(item);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.listview_artist_list, container, false);
            setListAdapter(getListAdapter());
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            registerForContextMenu(getListView());
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Bundle artistBundle = new Bundle();
            Bundle serviceBundle = new Bundle();
            Artist a = (Artist) l.getAdapter().getItem(position);
            artistBundle.putSerializable("artist", a);
            serviceBundle.putBinder(MediaPlayerService.SERVICE_BINDER_KEY, mMediaPlayerService.getBinder());

            Intent intent = new Intent(getApplicationContext(), TracksListActivity.class);
            intent.putExtra("artist", artistBundle);
            intent.putExtra(MediaPlayerService.SERVICE_BINDER_KEY, serviceBundle);
            startActivity(intent);
        }
    }

    public class TrackListFragment extends ListFragment {

        @Override
        public ListAdapter getListAdapter() {
            TrackAdapter adapter = new TrackAdapter(getContext(), getLayoutInflater(null));
            adapter.fillWithArtistsList(mArtists);
            TrackAdapter.putTrackImagesAsync(adapter);
            Logger.log("RETURNING ADAPTER");
            return adapter;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);
            getMenuInflater().inflate(R.menu.context_menu_track, menu);
        }

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Track pickedTrack = (Track)getListAdapter().getItem(info.position);
            switch (item.getItemId()) {
                case R.id.action_add_track_to_playlist:
                    mMediaPlayerService.getPlaylist().addTrack(pickedTrack);
                    break;
                case R.id.action_add_track_to_playlist_and_play:
                    mMediaPlayerService.getPlaylist().addTrack(pickedTrack);
                    mMediaPlayerService.performAction(MediaPlayerService.ACTION_PLAY_ON_PREPARED, pickedTrack);
                    openPlaybackActivity(pickedTrack);
            }
            return super.onContextItemSelected(item);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            registerForContextMenu(getListView());
        }

        @Override
        public ListView getListView() {
            return super.getListView();
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.listview_track_list, container, false);
            setListAdapter(getListAdapter());
            return rootView;
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Bundle serviceBundle = new Bundle();

            Track t = (Track) getListAdapter().getItem(position);

            serviceBundle.putBinder(MediaPlayerService.SERVICE_BINDER_KEY, mMediaPlayerService.getBinder());

            Logger.log("Opening new activity");

            final Intent intentOpenPlaybackActivity = new Intent(getApplicationContext(), PlaybackActivity.class);
            intentOpenPlaybackActivity.putExtra(MediaPlayerService.SERVICE_BINDER_KEY, serviceBundle);
            intentOpenPlaybackActivity.putExtra(MediaPlayerService.TRACK_BUNDLE_KEY, MediaPlayerService.createTrackBundle(t));
            startActivity(intentOpenPlaybackActivity);
        }
    }
}
