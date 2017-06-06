package com.vp.mplayerl.misc;

/**
 * Created by Ville on 5.6.2017.
 */

public interface OnMediaEventListener {

    public void onTrackChanged(Track NextTrack);
    public void onPlayerAction(String action);
}
