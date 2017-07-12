package com.vp.mplayerl.receivers;

import android.content.Intent;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import com.vp.mplayerl.MediaPlayerService;
import com.vp.mplayerl.misc.Logger;

/**
 * Created by Ville on 12.7.2017.
 */

public class MediaSessionCallbackHandler extends MediaSessionCompat.Callback {

    private MediaPlayerService mMediaPlayerService;

    public MediaSessionCallbackHandler(MediaPlayerService service) {
        mMediaPlayerService = service;
    }

    @Override
    public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
        boolean superWorked = super.onMediaButtonEvent(mediaButtonEvent);
        if (superWorked) {
            Logger.log("MediaSessionCallback - Super worked!");
            return true;
        }
        Logger.log("CallBackHandler - Super did not work -> handling action " + mediaButtonEvent.getAction());
        if (mediaButtonEvent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent != null && keyEvent.getAction() == 1) {
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY: onPlay(); return true;
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: onPlayPause(); return true;
                    case KeyEvent.KEYCODE_MEDIA_NEXT: onSkipToNext(); return true;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS: onSkipToPrevious(); return true;
                    case KeyEvent.KEYCODE_MEDIA_STOP: onStop(); return true;
                }
            } else {
                Logger.log("Key event was null!");
            }
        }
        return false;
    }

    public void onPlayPause() {
        mMediaPlayerService.performAction(MediaPlayerService.ACTION_PLAY_PAUSE, null);
    }

    @Override
    public void onPlay() {
        mMediaPlayerService.performAction(MediaPlayerService.ACTION_PLAY, null);
    }

    @Override
    public void onPause() {
        mMediaPlayerService.performAction(MediaPlayerService.ACTION_PAUSE, null);
    }

    @Override
    public void onSkipToNext() {
        mMediaPlayerService.performAction(MediaPlayerService.ACTION_NEXT, null);
    }

    @Override
    public void onSkipToPrevious() {
        mMediaPlayerService.performAction(MediaPlayerService.ACTION_PREVIOUS, null);
    }

    @Override
    public void onStop() {
        mMediaPlayerService.performAction(MediaPlayerService.ACTION_STOP, null);
    }
}
