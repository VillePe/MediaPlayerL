package com.vp.mplayerl;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.vp.mplayerl.fileparsers.mp3.Mp3Parser;
import com.vp.mplayerl.misc.Logger;
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

    public static int convertDpToPx(Context ctx, int dp) {
        final float scale = ctx.getResources().getDisplayMetrics().density;
        int pixels = (int) (dp * scale + 0.5f);
        return pixels;
    }

    public static int convertPxToDp(Context ctx, int px) {
        final float scale = ctx.getResources().getDisplayMetrics().density;
        int dps = (int) (px / scale - 0.5f);
        return dps;
    }

    public static boolean isImageFile(String s) {
        String suffix = s.substring(s.lastIndexOf(".") + 1);
        suffix = suffix.toLowerCase();
        Log.i("VP", "File suffix: " + suffix);
        if (suffix.equals("jpg") || suffix.equals("png") || suffix.equals("bmp")) {
            return true;
        }
        return false;
    }

    public static boolean isImageFile(File f) {
        String s = f.getAbsolutePath();
        String suffix = s.substring(s.lastIndexOf(".") + 1);
        suffix = suffix.toLowerCase();
        Log.i("VP", "File suffix: " + suffix);
        if (suffix.equals("jpg") || suffix.equals("png") || suffix.equals("bmp")) {
            return true;
        }
        return false;
    }

    public static String getDataFilesAbsolutePath(Context ctx) {
        PackageManager packageManager = ctx.getPackageManager();
        PackageInfo info = null;
        try {
            info = packageManager.getPackageInfo(ctx.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.log(e);
            return "";
        }
        String imagePath = info.applicationInfo.dataDir + "/files/";
        return imagePath;
    }

    public static boolean isFile(String s) {
        if (s == null) return false;
        try {
            File f = new File(s);
            if (!f.isFile()) {
                return false;
            }
        } catch (SecurityException ex) {
            Logger.log(ex);
            return false;
        }
        return true;
    }

    public static boolean isDirectory(String s) {
        if (s == null) return false;
        try {
            File f = new File(s);
            if (!f.isDirectory()) {
                return false;
            }
        } catch (SecurityException ex) {
            Logger.log(ex);
            return false;
        }
        return true;
    }

    public static Bitmap getThumbnailBitmapFromPath(String s) {
        Logger.log("Decoding file " + s + " to bitmap...");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 2;
        Bitmap bMap = BitmapFactory.decodeFile(s, options);
        if (bMap == null) {
            Logger.log("Something wrong with decoding! Decoded bitmap was null!");
            return null;
        }
        // Scale the bitmaps to save memory
        Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, 300,300, false);
        bMap.recycle();
        bMap = null;
        if (bMapScaled == null) {
            Logger.log("Something wrong with decoding! Decoded bitmap was null!");
        } else {
            Logger.log("Decoding done succesfully!");
        }
        return bMapScaled;
    }

    public static void createAlertDialog(Context ctx, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setMessage(message);
        builder.show();
    }

}
