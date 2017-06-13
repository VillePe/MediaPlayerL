package com.vp.mplayerl.misc;

import android.util.Log;

/**
 * Created by Ville on 5.6.2017.
 */

public class Logger {

    public static void log(String s) {
        Log.d("VP", s);
    }

    public static void log(Throwable t) {
        if (t.getMessage() != null) {
            Log.e("VP", t.getMessage());
        }
        Log.e("VP", Log.getStackTraceString(t));
    }

}
