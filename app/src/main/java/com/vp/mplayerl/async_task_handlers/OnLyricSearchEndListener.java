package com.vp.mplayerl.async_task_handlers;

import java.util.ArrayList;

import vp.lyrics.Lyric;

/**
 * Created by Ville on 14.6.2017.
 */

public interface OnLyricSearchEndListener {
    void searchFinished(ArrayList<Lyric> lyricsList);
}
