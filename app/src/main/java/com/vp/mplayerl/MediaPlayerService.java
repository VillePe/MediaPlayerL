package com.vp.mplayerl;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.vp.mplayerl.activities.MainActivity;
import com.vp.mplayerl.activities.PlaybackActivity;
import com.vp.mplayerl.misc.Logger;
import com.vp.mplayerl.misc.OnMediaEventListener;
import com.vp.mplayerl.misc.PlaybackBroadcastReceiver;
import com.vp.mplayerl.misc.Playlist;
import com.vp.mplayerl.misc.Track;

import java.io.IOException;
import java.util.Stack;

/**
 * Created by Ville on 28.10.2016.
 */

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener {
    public static final String ACTION_PLAY_ON_PREPARED = "com.vp.mplayerl.START_ON_PREPARED";
    public static final String ACTION_PLAY = "com.vp.mplayerl.PLAY";
    public static final String ACTION_PLAY_PAUSE = "com.vp.mplayerl.PLAY_PAUSE";
    public static final String ACTION_PAUSE = "com.vp.mplayerl.PAUSE";
    public static final String ACTION_STOP = "com.vp.mplayerl.STOP";
    public static final String ACTION_NEXT = "com.vp.mplayerl.NEXT";
    public static final String ACTION_PREVIOUS = "com.vp.mplayerl.PREVIOUS";
    public static final String SERVICE_BINDER_KEY = "com.vp.media_player_binder";
    public static final String TRACK_BUNDLE_KEY = "com.vp.track";

    public static final int DELETE_NOTIFICATION_ID = 103;

    private final int notificationID = this.getClass().hashCode()+20;


    private MediaPlayer mediaPlayer;
    private HeadPhonePLuggingReceiver broadcastReceiver;
    private final IBinder binder = new LocalBinder();
    private Intent intent;
    private boolean startAndPlayOnPrepared;
    private boolean isMediaPrepared;
    private boolean isStopped;
    private Track currentTrack;
    private Playlist playlist;

    NotificationManager notificationManager;
    private Stack<OnMediaEventListener> onMediaEventListeners = new Stack<>();
    private Context mainActivityContext;

    public MediaPlayerService() {
        this.playlist = new Playlist();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastReceiver = new HeadPhonePLuggingReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        this.intent = intent;
        initializeMediaPlayer();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Logger.log("Intent binded");

        return binder;
    }

    public Binder getBinder() {
        return (LocalBinder)binder;
    }

    public void performAction(String action, Track track) {
        if (currentTrack == null) {
            currentTrack = track;
        }
        if (action.equals(ACTION_PLAY_PAUSE)) {
            action = isMediaPlaying() ? ACTION_PAUSE : ACTION_PLAY;
        }
        switch(action) {
            case ACTION_PLAY_ON_PREPARED:
                Logger.log("ACTION_PLAY_ON_PREPARED");
                startAndPlayOnPrepared = true;
                initializeMediaPlayer();
                handleActionStart(track);
                break;
            case ACTION_PLAY:
                playMedia(track);
                break;
            case ACTION_PAUSE:
                pauseMedia();
                break;
            case ACTION_STOP:
                Logger.log("ACTION_STOP");
                releaseMediaPlayer();
                isStopped = true;
                break;
            case ACTION_NEXT:
                nextMedia();
                break;
            case ACTION_PREVIOUS:
                previousMedia();
                break;
            default:
                Logger.log("Unknown action!");
        }

        if (onMediaEventListeners.size() > 0) {
            Logger.log("Media event listeners size: " + onMediaEventListeners.size());
            for (OnMediaEventListener listener : this.onMediaEventListeners) {
                listener.onPlayerAction(action);
            }
        }
    }

    private void previousMedia() {
        Logger.log("ACTION_PREVIOUS");
        Track newTrack = null;
        if (playlist != null) {
            if (playlist.getSuffle()) {
                newTrack = playlist.getRandomTrack();
            } else {
                newTrack = playlist.getPreviousTrack();
            }
            setCurrentTrack(newTrack);
            changeTrack(newTrack);
        } else {
            Logger.log("Playlist was empty or null!");
        }
    }

    private void nextMedia() {
        Logger.log("ACTION_NEXT");
        Track newTrack = null;
        if (playlist != null) {
            if (playlist.getSuffle()) {
                newTrack = playlist.getRandomTrack();
            } else {
                newTrack = playlist.getNextTrack();
            }
            setCurrentTrack(newTrack);
            changeTrack(newTrack);
        } else {
            Logger.log("Playlist was empty or null!");
        }
    }

    private void playMedia(Track track) {
        Logger.log("ACTION_PLAY");
        boolean trackChanged = false;
        if (currentTrack == null) {
            currentTrack = track;
        } else {
            trackChanged = !currentTrack.equals(track);
            if (trackChanged) {
                currentTrack = track;
                initializeMediaPlayer();
            }
        }

        if (currentTrack == null && track == null) return;
        startForeground(notificationID, createNotification());
        if (trackChanged) {
            changeTrack(track);
        } else {
            mediaPlayer.start();
        }
    }

    private void pauseMedia() {
        Logger.log("ACTION_PAUSE");
        if (currentTrack != null) {
            notificationManager.notify(notificationID, createNotification());
            mediaPlayer.pause();
        }
        stopForeground(false);
    }

    public boolean isMediaPlayerNull() {
        return mediaPlayer == null;
    }

    public boolean isMediaPlaying() {
        try {
            if (isMediaPlayerNull()) {
                return false;
            }
            return mediaPlayer.isPlaying();
        } catch (IllegalStateException ex) {
            Logger.log(ex);
            return false;
        }
    }

    public boolean isMediaPrepared() {return isMediaPrepared;}

    public Track getCurrentTrack() {return currentTrack;}

    public MediaPlayer getMediaPlayer() {return mediaPlayer;}

    public void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        Logger.log("Media player service destroyed");
    }

    public void changeTrack(Track track) {
        if (!isStopped && track != null) {
            Logger.log("Changing track to: " + track.getTitle());
            currentTrack = track;
            startAndPlayOnPrepared = true;
            isMediaPrepared = false;
            handleActionStart(track);
        }
        if (onMediaEventListeners.size() > 0) {
            for (OnMediaEventListener listener : onMediaEventListeners) {
                listener.onPlayerAction(ACTION_PLAY);
            }
        }
    }

    public void setCurrentTrack(Track track) {
        this.currentTrack = track;
    }

    public void addOnTrackChangedListener(OnMediaEventListener listener) {
        if (!this.onMediaEventListeners.contains(listener)) {
            this.onMediaEventListeners.add(listener);
        }
    }

    public void removeOnTrackChangedListener(OnMediaEventListener listener) {
        if (this.onMediaEventListeners.contains(listener)) {
            this.onMediaEventListeners.remove(listener);
        }
    }

    private void initializeMediaPlayer() {
        releaseMediaPlayer();
        Logger.log("Initializing media player");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnErrorListener(new MediaPlayerErrorListener());
        mediaPlayer.setOnCompletionListener(new MediaPlayerOnCompletionListener());
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            notificationManager.cancel(notificationID);
            stopForeground(true);
            isMediaPrepared = false;
            Logger.log("Media player released!");
        } else {
            Logger.log("Could not release media player - it was null!");
        }
    }

    public static Bundle createBinderBundle(Binder binder) {
        Bundle bundle = new Bundle();
        bundle.putBinder(SERVICE_BINDER_KEY, binder);
        return bundle;
    }

    public static Bundle createTrackBundle(Track track) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(TRACK_BUNDLE_KEY, track);
        return bundle;
    }

    private void handleActionStart(Track track) {
        if (track == null) return;
        try {
            Logger.log("Preparing media player");
            if (isMediaPlayerNull()) {
                initializeMediaPlayer();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(track.getTrackFile().getAbsolutePath()));
            mediaPlayer.prepareAsync();

            if (playlist.size() < 1) {
                playlist.addTrack(track);
            }

            if (playlist.getTrackList().contains(track)) {
                int trackIndex = playlist.getTrackList().indexOf(track);
                playlist.setCurrentTrackNumber(trackIndex);
            }

            setCurrentTrack(track);
            initializeNotification();
        } catch (IOException ex) {
            Logger.log(ex);
        }
    }

    private void initializeNotification() {
        Notification notification = createNotification();
        startForeground(this.getClass().hashCode()+20, notification);
        notificationManager.notify(notificationID, notification);
        Logger.log(currentTrack.getTitle() + " " + currentTrack.getArtist());
        Logger.log("Notification initialized");
    }

    public Notification createNotification() {
        Intent intent = new Intent(getApplicationContext(), PlaybackActivity.class);

        intent.putExtra(TRACK_BUNDLE_KEY, createTrackBundle(currentTrack));
        intent.putExtra(SERVICE_BINDER_KEY, createBinderBundle(getBinder()));

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent artistListIntent = new Intent(getApplicationContext(), MainActivity.class);
        artistListIntent.putExtra(SERVICE_BINDER_KEY, createBinderBundle(getBinder()));

        PendingIntent deleteNotificationIntent = PendingIntent.getBroadcast(this, DELETE_NOTIFICATION_ID,
                RemoteViewPlaybackNotification.createBroadcastIntent(this, this, ACTION_STOP), PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViewPlaybackNotification remoteView = new RemoteViewPlaybackNotification(getApplicationContext(), getPackageName(), R.layout.remote_view_playback_notification);
        int playButtonResId = R.drawable.button_selector_play;
        if (!isMediaPlaying()) {
            playButtonResId = R.drawable.button_selector_pause;
        }
        remoteView.attachMediaPlayerService(this, playButtonResId);

        PendingIntent pi = TaskStackBuilder.create(getApplicationContext())
                .addNextIntent(artistListIntent)
                .addNextIntent(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(currentTrack.getTitle())
                .setContentText(currentTrack.getArtist())
                .setSmallIcon(R.mipmap.note_black_250x250)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setContent(remoteView)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build();
        notification.bigContentView = remoteView;
        notification.deleteIntent = deleteNotificationIntent;
        return notification;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (startAndPlayOnPrepared) {
            mp.start();
            startAndPlayOnPrepared = false;
            isMediaPrepared = true;
            isStopped = false;
            if (onMediaEventListeners.size() > 0) {
                for (OnMediaEventListener listener : this.onMediaEventListeners) {
                    listener.onPlayerStart();
                }
            }
        }
    }

    public Playlist getPlaylist() {
        if (playlist == null) {
            playlist = new Playlist();
        }
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public int getNotificationID() {
        return notificationID;
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MediaPlayerService.this;
        }
    }

    public class MediaPlayerErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            mediaPlayer.reset();
            isMediaPrepared = false;
            return false;
        }
    }

    public class MediaPlayerOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            performAction(MediaPlayerService.ACTION_NEXT, null);
        }
    }

    public class HeadPhonePLuggingReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentTrack == null || isMediaPlayerNull() || isStopped) {
                Logger.log("Headphones were plugged in or out, but no action was performed");
            }
            if (intent.getAction().compareTo(Intent.ACTION_HEADSET_PLUG) == 0) {
                switch (intent.getIntExtra("state", -1)) {
                    case 0:
                        Logger.log("Headphones plugged out");
                        if (!isMediaPlayerNull()) {
                            performAction(ACTION_PAUSE, currentTrack);
                        }
                        break;
                    case 1:
                        Logger.log("Headphones plugged in");
                        if (!isMediaPlayerNull()) {
                            performAction(ACTION_PLAY, currentTrack);
                        }
                }
            }
        }
    }
}
