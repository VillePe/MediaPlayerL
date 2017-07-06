package com.vp.mplayerl.activities;

import android.Manifest;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vp.mplayerl.MediaPlayerService;
import com.vp.mplayerl.R;
import com.vp.mplayerl.Utils;
import com.vp.mplayerl.async_task_handlers.AsyncLyricGetter;
import com.vp.mediafileparsers.ParseController;
import com.vp.mplayerl.misc.Logger;
import com.vp.mplayerl.misc.OnMediaEventListener;
import com.vp.mplayerl.misc.Track;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import vp.lyrics.LyricApi;
import vp.lyrics.LyricHandler;

/**
 * Created by Ville on 9.10.2016.
 */

public class PlaybackActivity extends AppCompatActivity implements OnMediaEventListener {

    SeekBar mSeekBar;
    TextView mCurrentTime;
    TextView mLyrics;
    ImageView mTrackImage;
    Button bPlay;
    Button bPrevious;
    Button bNext;
    Button bStop;
    Track mTrack;
    MediaPlayerService mMediaPlayerService;
    boolean mServiceBound;
    private Timer mPlayerTimer;
    private TimerListener timerListener;
    private boolean mTimerCancelled;
    private boolean mTimerScheduled;
    private LyricHandler mLyricHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_playback);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);


        MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) getIntent()
                .getBundleExtra(MediaPlayerService.SERVICE_BINDER_KEY)
                .getBinder(MediaPlayerService.SERVICE_BINDER_KEY);
        if (binder != null) {
            mMediaPlayerService = binder.getService();
            Logger.log("Media player service gotten from binder in intent extras");
        } else {
            Toast.makeText(this, "Could not get service with binder!", Toast.LENGTH_SHORT).show();
            Logger.log("Could not get service with binder");
            finish();
        }
        mMediaPlayerService.addOnTrackChangedListener(this);
        mServiceBound = true;

        mCurrentTime = ((TextView) this.findViewById(R.id.playback_current_time));
        mLyrics = (TextView) this.findViewById(R.id.playback_lyrics);
        mTrackImage = (ImageView) this.findViewById(R.id.playback_album_image);
        bPlay = (Button) this.findViewById(R.id.playback_b_play);
        bPrevious = (Button) this.findViewById(R.id.playback_b_previous);
        bNext = (Button) this.findViewById(R.id.playback_b_next);
        bStop = (Button) this.findViewById(R.id.playback_b_stop);
        mSeekBar = (SeekBar) findViewById(R.id.playback_seekbar);

        this.mTrack = Track.getTrackFromIntent(getIntent());
        if (mTrack == null) {
            Logger.log("Track was null!");
            Toast.makeText(this, "Parsing mTrack data failed", Toast.LENGTH_LONG).show();
            if (mMediaPlayerService.isMediaPlaying()) {
                mTrack = mMediaPlayerService.getCurrentTrack();
            } else {
                finish();
            }
        } else {
            setTrack(this.mTrack);
            mSeekBar.setMax(mTrack.getLengthInSeconds());
            setListenersOnSeekbar(mSeekBar);

            setListenersOnPlayButton(bPlay);
            setListenersOnTrackSwitchButton(bPrevious);
            setListenersOnTrackSwitchButton(bNext);
            setListenersOnStopButton(bStop);

            setPlayButtonState();

            initializeLyrics(mTrack);
            initializePicture(mTrack);

            Logger.log("Activity " + this.getLocalClassName() + " created");

        }
    }

    @Override
    protected void onResume() {
        startTimer();
        Logger.log("Playback activity resumed");
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.log("Playback activity started");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.log("Playback activity paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTimer();
        Logger.log("Playback activity stopped");
    }

    public void setmMediaPlayerService(MediaPlayerService service) {
        this.mMediaPlayerService = service;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.playback_track_info:
                showTrackInfo(this.mTrack);
                break;
            case R.id.action_playlist:
                if (mMediaPlayerService != null) {
                    startActivity(PlaylistActivity.createPlaylistActivityIntent(this, mMediaPlayerService));
                }
                break;
            case R.id.action_search_lyrics:
                searchLyrics();
                break;
            case R.id.action_search_lyrics_extended:
                startActivity(ExtendedLyricSearching.createExtendedLyricSearchIntent(this, mTrack));
                break;
            case R.id.action_lyric_search_debug:
                openLyricHandlerDialog(this.mLyricHandler);
                break;
        }
        return true;
    }

    private void searchLyrics() {
        int permissionInternet = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);
        Log.d("VP", "PERMISSION READ: " + permissionInternet);
        if (permissionInternet == PermissionChecker.PERMISSION_DENIED) {
            Logger.log("Aksing permission!");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    MainActivity.PERMISSIONS_INTERNET);

        }

        this.mLyricHandler = new LyricHandler();
        try {
            InputStream iStream = getAssets().open("lyricApiConfig.txt");
            AsyncLyricGetter searcher = new AsyncLyricGetter(getApplicationContext(), this.mLyricHandler, iStream, mTrack, mLyrics);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            searcher.registerOutputStream(new PrintStream(baos), baos);
            searcher.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openLyricHandlerDialog(LyricHandler handler) {
        if (handler == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("LyriApis: ").append("\n");
        ArrayList<LyricApi> lyricApis = handler.getLyricApis();
        for (int i = 0; i < handler.getLyricApis().size(); i++) {
            try {
                sb.append("    - ").append(lyricApis.get(i).getApiName()).append("\n");
            } catch (LyricApi.ApiException ignored) { }
        }
        sb.append("Search queries: ").append("\n");
        for (int i = 0; i < handler.getLyricSearchQueries().size(); i++) {
            sb.append(handler.getLyricSearchQueries().get(i)).append("\n");
        }
        sb.append("Get queries: ").append("\n");
        for (int i = 0; i < handler.getLyricSearchQueries().size(); i++) {
            sb.append(handler.getLyricGetQueries().get(i)).append("\n");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(PlaybackActivity.this)
                .setTitle("Etsinnän tiedot")
                .setMessage(sb.toString());
        builder.show();
    }

    @Override
    public void onTrackChanged(Track nextTrack) {
        setTrack(nextTrack);
        Logger.log("Playback changing mTrack to " + nextTrack.getTitle());
    }

    @Override
    public void onPlayerAction(String action) {
        if (action.equals(MediaPlayerService.ACTION_PLAY)) {
            startTimer();
        } else if (action.equals(MediaPlayerService.ACTION_NEXT) || action.equals(MediaPlayerService.ACTION_PREVIOUS)) {
            Track newTrack = mMediaPlayerService.getCurrentTrack();
            Logger.log("PlaybackActivity: Action next or previous performed!");
            if (newTrack != null) {
                setTrack(newTrack);
            }
        }
    }

    @Override
    public void onPlayerStart() {

    }

    private void showTrackInfo(Track track) {
        StringBuilder sb = new StringBuilder();
        boolean lyricsFound = true;
        if (track.getLyrics() == null || track.getLyrics().isEmpty() || Objects.equals(track.getLyrics(), getString(R.string.no_lyrics))) {
            lyricsFound = false;
        }
        sb.append("Artist: ").append(track.getArtist()).append("\n")
        .append("Album: ").append(track.getAlbum()).append("\n")
        .append("Track title: ").append(track.getTitle()).append("\n")
        .append("Track length: ").append(Utils.convertSecondsToMinutesString(track.getLengthInSeconds())).append("\n")
        .append("Filetype: ").append(Utils.getTrackAudioFileType(getApplicationContext(), track)).append("\n")
        .append("Lyrics found: ").append(lyricsFound);
        AlertDialog.Builder builder = new AlertDialog.Builder(PlaybackActivity.this)
                .setTitle("Kappaleen tiedot")
                .setMessage(sb.toString());
        builder.show();
    }

    private void setPlayButtonState() {
        if (mServiceBound) {
            if (mMediaPlayerService.isMediaPlayerNull()) {
            } else {
                if (mMediaPlayerService.isMediaPlaying()) {
                    if (mMediaPlayerService.getCurrentTrack().equals(mTrack)) {
                        bPlay.setBackground(getDrawable(R.drawable.button_selector_pause));
                    } else {
                        bPlay.setBackground(getDrawable(R.drawable.button_selector_play));
                    }
                } else {
                    bPlay.setBackground(getDrawable(R.drawable.button_selector_play));
                }
            }

        } else {
            Logger.log("No service bound");
        }
    }

    private void setTrack(Track track) {
        this.mTrack = track;
        ((TextView) this.findViewById(R.id.playback_artist)).setText(track.getArtist());
        ((TextView) this.findViewById(R.id.playback_title)).setText(track.getTitle());
        ((TextView) this.findViewById(R.id.playback_track_length)).setText(Utils.convertSecondsToMinutesString(track.getLengthInSeconds()));
        if (mSeekBar != null) {
            mSeekBar.setMax(track.getLengthInSeconds());
        }
        initializeLyrics(track);
        mCurrentTime.setText("0:00");
        startTimer();
    }

    private void startTimer() {
        mPlayerTimer = new Timer();
        if (timerListener == null || mTimerCancelled) {
            timerListener = new TimerListener(mSeekBar, mMediaPlayerService);
            mTimerScheduled = false;
        }
        if (mSeekBar != null && !mTimerScheduled) {
            mPlayerTimer.schedule(timerListener, 50, 1000);
            Logger.log("Timer started");
            mTimerScheduled = true;
            mTimerCancelled = false;
        } else {
            Logger.log("Timer starting cancelled");
            Logger.log("Seekbar is null: " + (mSeekBar == null));
            Logger.log("Timer is scheduled: " + mTimerScheduled);

        }
    }

    private void stopTimer() {
        Logger.log("Stopping timer");
        mTimerCancelled = true;
        mTimerScheduled = false;
        mPlayerTimer.cancel();
        timerListener.cancel();
    }

    private void initializeLyrics(Track track) {
        if (track.getLyrics() == null || track.getLyrics().isEmpty() || track.getLyrics().toLowerCase().equals("no lyrics")) {
            Logger.log("Parsing lyrics from file " + track.getTitle() + "...");
            String lyrics = ParseController.getLyricsFromFile(track.getTrackFile());
            track.setLyrics(lyrics);
        }
        mLyrics.setText(track.getLyrics());
    }

    private void initializePicture(Track track) {
        Bitmap trackMap = track.getSmallBitmap();
        if (trackMap != null) {
            mTrackImage.setImageBitmap(trackMap);
        } else {
            Logger.log("Could not initialize picture. Bitmap was null");
        }
    }

    private void setListenersOnSeekbar(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurrentTime.setText(Utils.convertSecondsToMinutesString(progress));
                MediaPlayer mPlayer = mMediaPlayerService.getMediaPlayer();
                if (mPlayer != null && fromUser) {
                    mPlayer.seekTo(progress*1000);
                    Logger.log("FROM USER!");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setListenersOnPlayButton(Button b) {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.log("Service bound: " + mServiceBound);
                if (mServiceBound) {
                    if (mMediaPlayerService.isMediaPlayerNull() || !mMediaPlayerService.isMediaPrepared()) {
                        Logger.log("Mediaplayer is NULL or it is not prepared");
                        mMediaPlayerService.performAction(MediaPlayerService.ACTION_PLAY_ON_PREPARED, mTrack);
                        bPlay.setBackground(getDrawable(R.drawable.button_selector_pause));
                    } else {
                        Logger.log("Media is playing: " + mMediaPlayerService.isMediaPlaying());
                        if (mMediaPlayerService.isMediaPlaying()) {
                            if (mMediaPlayerService.getCurrentTrack().equals(mTrack)) {
                                mMediaPlayerService.performAction(MediaPlayerService.ACTION_PAUSE, mTrack);
                                bPlay.setBackground(getDrawable(R.drawable.button_selector_play));
                            } else {
                                Logger.log("Playback mTrack is different than media player mTrack");
                                mMediaPlayerService.performAction(MediaPlayerService.ACTION_PLAY_ON_PREPARED, mTrack);
                                bPlay.setBackground(getDrawable(R.drawable.button_selector_pause));
                            }
                        } else {
                            mMediaPlayerService.performAction(MediaPlayerService.ACTION_PLAY, mTrack);
                            bPlay.setBackground(getDrawable(R.drawable.button_selector_pause));
                        }
                    }
                    startTimer();

                } else {
                    Logger.log("No service bound");
                }
            }
        });
    }

    private void setListenersOnTrackSwitchButton(final Button b) {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.log("Service bound: " + mServiceBound);
                if (mServiceBound) {
                    if (mMediaPlayerService.getPlaylist() == null || mMediaPlayerService.getPlaylist().size() <= 1) {
                        Toast.makeText(getApplicationContext(), R.string.no_more_tracks, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (b.equals(bNext)) {
                        mMediaPlayerService.performAction(MediaPlayerService.ACTION_NEXT, mTrack);
                    } else {
                        mMediaPlayerService.performAction(MediaPlayerService.ACTION_PREVIOUS, mTrack);
                    }
                }
            }
        });
    }

    private void setListenersOnStopButton(Button b) {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.log("Service bound: " + mServiceBound);
                if (mServiceBound) {
                    mMediaPlayerService.performAction(MediaPlayerService.ACTION_STOP, mTrack);
                    bPlay.setBackground(getDrawable(R.drawable.button_selector_play));
                }
            }
        });
    }

    public class TimerListener extends TimerTask {

        private SeekBar seekBar;
        private MediaPlayerService mediaPlayerService;

        TimerListener(SeekBar seekbar, MediaPlayerService mediaPlayerService) {
            this.seekBar = seekbar;
            this.mediaPlayerService = mediaPlayerService;
        }

        @Override
        public void run() {
            int currentTimeInSeconds;
            if (mediaPlayerService.isMediaPlaying() && mediaPlayerService.isMediaPrepared()) {
                MediaPlayer mPlayer = mediaPlayerService.getMediaPlayer();
                if (mPlayer != null && seekBar != null ) {
                    if (mTrack.equals(mediaPlayerService.getCurrentTrack())) {
                        currentTimeInSeconds = mPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentTimeInSeconds);
                    }
                } else {
                    Logger.log("Mediaplayer or seekbar was null! Could not set seekbar value");
                    Logger.log("Mediaplayer is null: " + (mPlayer == null));
                    Logger.log("Seekbar is null: " + (seekBar == null));
                    stopTimer();
                }
            } else {
                Logger.log("Mediaplayer is not playing or it isn't prepared! Could not set seekbar value");
                stopTimer();
            }

        }
    }
}
