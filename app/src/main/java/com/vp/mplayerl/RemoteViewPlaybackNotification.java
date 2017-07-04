package com.vp.mplayerl;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.vp.mplayerl.misc.Logger;
import com.vp.mplayerl.misc.PlaybackBroadcastReceiver;
import com.vp.mplayerl.misc.Track;

/**
 * Created by Ville on 9.11.2016.
 */

public class RemoteViewPlaybackNotification extends RemoteViews {

    private Context context;
    private Button mPlayButton;
    private Button mPreviousButton;
    private Button mNextButton;
    private MediaPlayerService mediaPlayerService;
    private Track track;

    public RemoteViewPlaybackNotification(Context context, String packageName, int layoutId) {
        super(packageName, layoutId);
        this.context = context;
        Logger.log("RVPlaybackNotification: Notification initialized!");
    }



    public void onReceive(Context context, Intent intent) {

    }

    public void attachMediaPlayerService(MediaPlayerService mediaPlayerService, int playButtonResId) {
        this.mediaPlayerService = mediaPlayerService;
        setTextViewText(R.id.remote_view_title, mediaPlayerService.getCurrentTrack().getTitle());
        setTextViewText(R.id.remote_view_artist, mediaPlayerService.getCurrentTrack().getArtist());
        setImageViewResource(R.id.remote_view_play_button, playButtonResId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 100, createBroadcastIntent(context, mediaPlayerService, MediaPlayerService.ACTION_PLAY_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT);
        setOnClickPendingIntent(R.id.remote_view_play_button, pendingIntent);

        pendingIntent = PendingIntent.getBroadcast(context, 101, createBroadcastIntent(context, mediaPlayerService, MediaPlayerService.ACTION_PREVIOUS), PendingIntent.FLAG_UPDATE_CURRENT);
        setOnClickPendingIntent(R.id.remote_view_previous_button, pendingIntent);

        pendingIntent = PendingIntent.getBroadcast(context, 102, createBroadcastIntent(context, mediaPlayerService, MediaPlayerService.ACTION_NEXT), PendingIntent.FLAG_UPDATE_CURRENT);
        setOnClickPendingIntent(R.id.remote_view_next_button, pendingIntent);

    }

    public static Intent createBroadcastIntent(Context ctx, MediaPlayerService service, String action) {
        Intent intent = new Intent(ctx, PlaybackBroadcastReceiver.class);
        intent.setAction(action);
        intent.putExtras(MediaPlayerService.createBinderBundle(service.getBinder()));
        return intent;
    }

    private void setListenersOnPlayButton(Button b) {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayerService.isMediaPlayerNull()) {
                    Log.d("RVPlaybackNotification", "Mediaplayer is NULL");
                } else if (!mediaPlayerService.isMediaPrepared()) {
                    Log.d("RVPlaybackNotification", "Mediaplayer is not prepared");
                } else {
                    Log.d("RVPlaybackNotification", "Media is playing: " + mediaPlayerService.isMediaPlaying());
                    if (mediaPlayerService.isMediaPlaying()) {
                        if (mediaPlayerService.getCurrentTrack().equals(track)) {
                            mediaPlayerService.performAction(MediaPlayerService.ACTION_PAUSE, track);
                            mPlayButton.setBackground(context.getDrawable(R.drawable.button_selector_play));
                        } else {
                            Log.d("RVPlaybackNotification", "Playback track is different than media player track");
                        }
                    } else {
                        mediaPlayerService.performAction(MediaPlayerService.ACTION_PLAY, track);
                        mPlayButton.setBackground(context.getDrawable(R.drawable.button_selector_pause));
                    }
                }
            }
        });
    }

    public class PlaybackNotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.log("Notification received intent!");
        }
    }
}
