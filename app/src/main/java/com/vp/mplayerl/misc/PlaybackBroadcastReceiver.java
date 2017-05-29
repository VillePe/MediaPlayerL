package com.vp.mplayerl.misc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.vp.mplayerl.MediaPlayerService;

/**
 * Created by Ville on 14.11.2016.
 */

public class PlaybackBroadcastReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder)bundle.getBinder(MediaPlayerService.SERVICE_BINDER_KEY);
            MediaPlayerService mediaPlayerService = binder.getService();

            String action = intent.getAction();
            Track currentTrack = mediaPlayerService.getCurrentTrack();
            if (action != null && currentTrack != null) {
                mediaPlayerService.performAction(action, currentTrack);
                Log.d("PlaybackBroadReceiver", "Action performed! " + action);
            } else {
                Log.w("PlaybackBroadReceiver", "Intent had no action or there was no track initialized");
            }
        } else {
            Log.w("PlaybackBroadReceiver", "Bundle was null");
        }
    }
}
