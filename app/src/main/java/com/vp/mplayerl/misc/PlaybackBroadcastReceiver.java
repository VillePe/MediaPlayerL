package com.vp.mplayerl.misc;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import com.vp.mplayerl.MediaPlayerService;
import com.vp.mplayerl.R;

/**
 * Created by Ville on 14.11.2016.
 */

public class PlaybackBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            Logger.log("MEDIA BUTTON");
        }
        Logger.log("Playback broadcast receiver - action received: " + intent.getAction());
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder)bundle.getBinder(MediaPlayerService.SERVICE_BINDER_KEY);
            MediaPlayerService mediaPlayerService = binder.getService();

            String action = intent.getAction();
            Track currentTrack = mediaPlayerService.getCurrentTrack();
            Notification notification = mediaPlayerService.createNotification();
//            notification.notify();
            mediaPlayerService.getNotificationManager().notify(mediaPlayerService.getNotificationID(), notification);

            if (action != null && currentTrack != null) {
                mediaPlayerService.performAction(action, currentTrack);
                Logger.log("Action performed! " + action);
            } else {
                Logger.log("Intent had no action or there was no track initialized");
            }
        } else {
            Logger.log("Bundle was null");
        }
    }
}
