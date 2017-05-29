package com.vp.mplayerl;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vp.mplayerl.fileparsers.ParseController;
import com.vp.mplayerl.misc.Track;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ville on 9.10.2016.
 */

public class PlaybackActivity extends AppCompatActivity {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_playback);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        playerTimer = new Timer();


        binder = (MediaPlayerService.LocalBinder) getIntent()
                .getBundleExtra(MediaPlayerService.SERVICE_BINDER_KEY)
                .getBinder(MediaPlayerService.SERVICE_BINDER_KEY);
        mediaPlayerService = binder.getService();
        serviceBound = true;

        currentTime = ((TextView) this.findViewById(R.id.playback_current_time));
        lyrics = (TextView) this.findViewById(R.id.playback_lyrics);
        bPlay = (Button) this.findViewById(R.id.playback_b_play);
        bPrevious = (Button) this.findViewById(R.id.playback_b_previous);
        bNext = (Button) this.findViewById(R.id.playback_b_next);
        bStop = (Button) this.findViewById(R.id.playback_b_stop);

        this.track = setTrack();
        if (track == null) {
            Log.e("PlaybackActivity", "Track was null!");
            Toast.makeText(this, "Parsing track data failed", Toast.LENGTH_LONG).show();
            finish();
        } else {
            seekBar = (SeekBar) findViewById(R.id.playback_seekbar);
            seekBar.setMax(track.getLengthInSeconds());
            setListenersOnSeekbar(seekBar);

            setListenersOnPlayButton(bPlay);
            setListenersOnTrackSwitchButton(bPrevious, "previous");
            setListenersOnTrackSwitchButton(bNext, "next");
            setListenersOnStopButton(bStop);

            setPlayButtonState();

            initializeLyrics();

            //bindToMPService();

            Log.d("PlaybackActivity", "Activity " + this.getLocalClassName() + " created");

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("PlaybackActivity", "Activity started");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("PlaybackActivity", "Activity stopped");
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
        }
        return true;
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
            Log.d("PlaybackActivity", "No service bound");
        }
    }

    private Track setTrack() {
        if (this.getIntent().getExtras() != null) {
            Bundle bundle = this.getIntent().getExtras().getBundle(MediaPlayerService.TRACK_BUNDLE_KEY);
            Track t = (Track) bundle.getSerializable(MediaPlayerService.TRACK_BUNDLE_KEY);
            if (t != null) {
                ((TextView) this.findViewById(R.id.playback_artist)).setText(t.getArtist());
                ((TextView) this.findViewById(R.id.playback_title)).setText(t.getTitle());
                ((TextView) this.findViewById(R.id.playback_track_length)).setText(Utils.convertSecondsToMinutesString(t.getLengthInSeconds()));
                currentTime.setText("0:00");
                return t;
            } else {
                Log.i("PlaybackActivity", "Could not deserialize track!");
                return null;
            }

        } else {
            Log.w("PlaybackActivity", "Extras in intent was NULL");
            return null;
        }
    }

    private void initializeLyrics() {
        track.setLyrics(ParseController.getLyricsFromFile(getApplicationContext(), track.getTrackFile()));
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
                    Log.d("TST", "FROM USER!");
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
                Log.d("PlaybackActivity", "Service bound: " + serviceBound);
                if (serviceBound) {
                    if (mediaPlayerService.isMediaPlayerNull()) {
                        Log.d("PlaybackActivity", "Mediaplayer is NULL");
                        mediaPlayerService.performAction(MediaPlayerService.ACTION_PLAY_ON_PREPARED, track);
                        playerTimer.schedule(new TimerListener(seekBar, mediaPlayerService), 1000, 1000);
                        bPlay.setBackground(getDrawable(R.drawable.button_selector_pause));
                    } else if (!mediaPlayerService.isMediaPrepared()) {
                        Log.d("PlaybackActivity", "Mediaplayer is not prepared");
                        mediaPlayerService.performAction(MediaPlayerService.ACTION_PLAY_ON_PREPARED, track);
                        playerTimer.schedule(new TimerListener(seekBar, mediaPlayerService), 1000, 1000);
                        bPlay.setBackground(getDrawable(R.drawable.button_selector_pause));
                    } else {
                        Log.d("PlaybackActivity", "Media is playing: " + mediaPlayerService.isMediaPlaying());
                        if (mediaPlayerService.isMediaPlaying()) {
                            if (mediaPlayerService.getCurrentTrack().equals(track)) {
                                mediaPlayerService.performAction(MediaPlayerService.ACTION_PAUSE, track);
                                bPlay.setBackground(getDrawable(R.drawable.button_selector_play));
                            } else {
                                Log.d("PlaybackActivity", "Playback track is different than media player track");
                                mediaPlayerService.performAction(MediaPlayerService.ACTION_PLAY_ON_PREPARED, track);
                                playerTimer.schedule(new TimerListener(seekBar, mediaPlayerService), 1000, 1000);
                                bPlay.setBackground(getDrawable(R.drawable.button_selector_pause));
                            }
                        } else {
                            mediaPlayerService.performAction(MediaPlayerService.ACTION_PLAY, track);
                            playerTimer.schedule(new TimerListener(seekBar, mediaPlayerService), 1000, 1000);
                            bPlay.setBackground(getDrawable(R.drawable.button_selector_pause));
                        }
                    }

                } else {
                    Log.d("PlaybackActivity", "No service bound");
                }
            }
        });
    }

    private void setListenersOnTrackSwitchButton(Button b, String direction) {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PlaybackActivity", "Service bound: " + serviceBound);
                if (serviceBound) {

                }
            }
        });
    }

    private void setListenersOnStopButton(Button b) {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PlaybackActivity", "Service bound: " + serviceBound);
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
            MediaPlayer mPlayer = mediaPlayerService.getMediaPlayer();
            if (mPlayer != null && seekBar != null && mPlayer.isPlaying()) {
                currentTimeInSeconds = mPlayer.getCurrentPosition()/1000;
                seekBar.setProgress(currentTimeInSeconds);
            }
        }
    }
}
