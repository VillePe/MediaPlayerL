package com.vp.mplayerl;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.vp.mplayerl.fileparsers.mp3.Mp3Parser;
import com.vp.mplayerl.misc.Track;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Ville on 19.10.2016.
 */

public class Utils {

    public static boolean isExtStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            return true;
        } else {
            return false;
        }
    }

    public static String convertSecondsToMinutesString(int seconds) {
        String result = "";
        if (seconds > 0) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds - (60 * (seconds / 60));
            if (remainingSeconds < 10) {
                result += minutes + ":0" + remainingSeconds;
            } else {
                result += minutes + ":" + remainingSeconds;
            }
        } else if (seconds == 0) {
            result = "0:00";
        }

        return result;
    }

    public static AudioFileTypes getTrackAudioFileType(Context ctx, Track track) {
        File trackFile = track.getTrackFile();
        return getAudioFileType(ctx, trackFile);
    }

    public static AudioFileTypes getAudioFileType(Context ctx, File file, DataInputStream dInput) {
        try {
            char c = (char)dInput.read();
            if (c == 'f') {
                return AudioFileTypes.FLAC;
            } else if (c == 'I') {
                return AudioFileTypes.MP3;
            }

        } catch (IOException e) {
            if (e.getMessage() != null) {
                Log.w("Lyrics", e.getMessage());
            } else {
                Log.w("Lyrics", "IOException thrown! No message included");
            }

        } catch (IllegalArgumentException e) {
            Log.w("Lyrics", e.getMessage());
        }
        return AudioFileTypes.UNKNOWN;
    }

    public static AudioFileTypes getAudioFileType(Context ctx, File file) {
        try {
            FileInputStream fInput = new FileInputStream(file);
            DataInputStream dInput = new DataInputStream(fInput);
            return getAudioFileType(ctx, file, dInput);
        } catch (IOException ex) {
            if (ex.getMessage() != null) {
                Log.w("Lyrics", ex.getMessage());
            } else {
                Log.w("Lyrics", "IOException thrown! No message included");
            }
        }
        return AudioFileTypes.UNKNOWN;
    }

    public enum AudioFileTypes {
        MP3("MP3"),
        FLAC("FLAC"),
        UNKNOWN("UNKNOWN");

        private String type;

        private AudioFileTypes(String s) {
            type = s;
        }

        public String getAudioTypeString() {return type;}

    }

}
