package com.vp.mplayerl.misc;

/**
 * Created by Ville on 5.6.2017.
 */

public interface OnMediaEventListener {

    void onTrackChanged(Track NextTrack);
    void onPlayerAction(String action);
    void onPlayerStart();
}
