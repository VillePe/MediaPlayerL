package com.vp.mplayerl;

import android.Manifest;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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

    SeekBar seekBar;
    TextView currentTime;
    TextView lyrics;
    Button bPlay;
    Button bPrevious;
    Button bNext;
    Button bStop;
    Track track;
    MediaPlayerService mediaPlayerService;
    boolean serviceBound;
    private MediaPlayerService.LocalBinder binder;
    private Timer playerTimer;
    private TimerListener timerListener;
    private boolean timerCancelled;
    private boolean timerScheduled;
    private LyricHandler lyricHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_playback);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);


        binder = (MediaPlayerService.LocalBinder) getIntent()
                .getBundleExtra(MediaPlayerService.SERVICE_BINDER_KEY)
                .getBinder(MediaPlayerService.SERVICE_BINDER_KEY);
        mediaPlayerService = binder.getService();
        mediaPlayerService.setOnTrackChangedListener(this);
        serviceBound = true;

        currentTime = ((TextView) this.findViewById(R.id.playback_current_time));
        lyrics = (TextView) this.findViewById(R.id.playback_lyrics);
        bPlay = (Button) this.findViewById(R.id.playback_b_play);
        bPrevious = (Button) this.findViewById(R.id.playback_b_previous);
        bNext = (Button) this.findViewById(R.id.playback_b_next);
        bStop = (Button) this.findViewById(R.id.playback_b_stop);
        seekBar = (SeekBar) findViewById(R.id.playback_seekbar);

        this.track = Track.getTrackFromIntent(getIntent());
        if (track == null) {
            Log.e("PlaybackActivity", "Track was null!");
            Toast.makeText(this, "Parsing track data failed", Toast.LENGTH_LONG).show();
            finish();
        } else {
            setTrack(this.track);
            seekBar.setMax(track.getLengthInSeconds());
            setListenersOnSeekbar(seekBar);

            setListenersOnPlayButton(bPlay);
            setListenersOnTrackSwitchButton(bPrevious, "previous");
            setListenersOnTrackSwitchButton(bNext, "next");
            setListenersOnStopButton(bStop);

            setPlayButtonState();

            initializeLyrics();

            //bindToMPService();

            Logger.log("Activity " + this.getLocalClassName() + " created");

        }
    }

    @Override
    protected void onResume() {
        startTimer();
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.log("Activity started");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.log("Activity stopped");
    }

    public void setMediaPlayerService(MediaPlayerService service) {
        this.mediaPlayerService = service;
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
                showTrackInfo(this.track);
                break;
            case R.id.action_playlist:
                if (mediaPlayerService != null) {
                    startActivity(PlaylistActivity.createPlaylistActivityIntent(this, mediaPlayerService));
                }
                break;
            case R.id.action_search_lyrics:
                searchLyrics();
                break;
            case R.id.action_search_lyrics_extended:
                startActivity(ExtendedLyricSearching.createExtendedLyricSearchIntent(this, track));
                break;
            case R.id.action_lyric_search_debug:
                openLyricHandlerDialog(this.lyricHandler);
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

        this.lyricHandler = new LyricHandler();
        try {
            InputStream iStream = getAssets().open("lyricApiConfig.txt");
            AsyncLyricGetter searcher = new AsyncLyricGetter(getApplicationContext(), this.lyricHandler, iStream, track, lyrics);
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
        Logger.log("Playback changing track to " + nextTrack.getTitle());
    }

    @Override
    public void onPlayerAction(String action) {
        if (action.equals(MediaPlayerService.ACTION_PLAY)) {
            startTimer();
        }
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
        if (serviceBound) {
            if (mediaPlayerService.isMediaPlayerNull()) {
            } else {
                if (mediaPlayerService.isMediaPlaying()) {
                    if (mediaPlayerService.getCurrentTrack().equals(track)) {
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
        this.track = track;
        ((TextView) this.findViewById(R.id.playback_artist)).setText(track.getArtist());
        ((TextView) this.findViewById(R.id.playback_title)).setText(track.getTitle());
        ((TextView) this.findViewById(R.id.playback_track_length)).setText(Utils.convertSecondsToMinutesString(track.getLengthInSeconds()));
        if (seekBar != null) {
            seekBar.setMax(track.getLengthInSeconds());
        }
        currentTime.setText("0:00");
        startTimer();
    }

    private void startTimer() {
        playerTimer = new Timer();
        if (timerListener == null || timerCancelled) {
            timerListener = new TimerListener(seekBar, mediaPlayerService);
            timerScheduled = false;
        }
        if (seekBar != null && !timerScheduled) {
            playerTimer.schedule(timerListener, 1000, 1000);
            timerScheduled = true;
            timerCancelled = false;
        }
    }

    private void stopTimer() {
        Logger.log("Stopping timer");
        timerCancelled = true;
        timerScheduled = false;
        playerTimer.cancel();
        timerListener.cancel();
    }

    private void initializeLyrics() {
        track.setLyrics(ParseController.getLyricsFromFile(track.getTrackFile()));
        lyrics.setText(track.getLyrics());
    }

    private void setListenersOnSeekbar(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentTime.setText(Utils.convertSecondsToMinutesString(progress));
                MediaPlayer mPlayer = mediaPlayerService.getMediaPlayer();
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
                Logger.log("Service bound: " + serviceBound);
                startTimer();
                if (serviceBound) {
                    if (mediaPlayerService.isMediaPlayerNull() || !mediaPlayerService.isMediaPrepared()) {
                        Logger.log("Mediaplayer is NULL or it is not prepared");
                        mediaPlayerService.performAction(MediaPlayerService.ACTION_PLAY_ON_PREPARED, track);
                        bPlay.setBackground(getDrawable(R.drawable.button_selector_pause));
                    } else {
                        Logger.log("Media is playing: " + mediaPlayerService.isMediaPlaying());
                        if (mediaPlayerService.isMediaPlaying()) {
                            if (mediaPlayerService.getCurrentTrack().equals(track)) {
                                mediaPlayerService.performAction(MediaPlayerService.ACTION_PAUSE, track);
                                bPlay.setBackground(getDrawable(R.drawable.button_selector_play));
                            } else {
                                Logger.log("Playback track is different than media player track");
                                mediaPlayerService.performAction(MediaPlayerService.ACTION_PLAY_ON_PREPARED, track);
                                bPlay.setBackground(getDrawable(R.drawable.button_selector_pause));
                            }
                        } else {
                            mediaPlayerService.performAction(MediaPlayerService.ACTION_PLAY, track);
                            bPlay.setBackground(getDrawable(R.drawable.button_selector_pause));
                        }
                    }

                } else {
                    Logger.log("No service bound");
                }
            }
        });
    }

    private void setListenersOnTrackSwitchButton(final Button b, String direction) {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.log("Service bound: " + serviceBound);
                if (serviceBound) {
                    if (mediaPlayerService.getPlaylist() == null || mediaPlayerService.getPlaylist().size() <= 1) {
                        Toast.makeText(getApplicationContext(), R.string.no_more_tracks, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (b.equals(bNext)) {
                        mediaPlayerService.performAction(MediaPlayerService.ACTION_NEXT, track);
                    } else {
                        mediaPlayerService.performAction(MediaPlayerService.ACTION_PREVIOUS, track);
                    }
                }
            }
        });
    }

    private void setListenersOnStopButton(Button b) {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.log("Service bound: " + serviceBound);
                if (serviceBound) {
                    mediaPlayerService.performAction(MediaPlayerService.ACTION_STOP, track);
                    bPlay.setBackground(getDrawable(R.drawable.button_selector_play));
                }
            }
        });
    }


    public class TimerListener extends TimerTask {

        private SeekBar seekBar;
        private MediaPlayerService mediaPlayerService;
        private MediaPlayer mediaPlayer;

        public TimerListener(SeekBar seekbar, MediaPlayerService mediaPlayerService) {
            this.seekBar = seekbar;
            this.mediaPlayerService = mediaPlayerService;
            this.mediaPlayer = mediaPlayerService.getMediaPlayer();
        }

        @Override
        public void run() {
            int currentTimeInSeconds = 0;
            if (mediaPlayerService.isMediaPlaying() && mediaPlayerService.isMediaPrepared()) {
                MediaPlayer mPlayer = mediaPlayerService.getMediaPlayer();
                if (mPlayer != null && seekBar != null ) {
                    if (track.equals(mediaPlayerService.getCurrentTrack())) {
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
