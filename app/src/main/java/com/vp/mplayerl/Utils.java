package com.vp.mplayerl;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ImageView;

import com.vp.mplayerl.misc.Artist;
import com.vp.mplayerl.misc.TrackAdapter;
import com.vp.parsers.mp3.Mp3Parser;
import com.vp.mplayerl.misc.Logger;
import com.vp.mplayerl.misc.Track;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Ville on 19.10.2016.
 */

public class Utils {

    public static boolean isExtStorageReadable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
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

    public static void setTrackImageToImageView(Track currentTrack, ImageView imgView, boolean useSmallImage) {
        Bitmap bitmap = null;
        if (useSmallImage) {
            bitmap = currentTrack.getSmallBitmap();
        } else {
            bitmap = currentTrack.getLargeBitmap();
        }
        if (bitmap != null) {
            imgView.setImageBitmap(bitmap);
        } else {
            imgView.setImageResource(R.mipmap.noimagefound);
        }
    }

    public enum AudioFileTypes {
        MP3("MP3"),
        FLAC("FLAC"),
        UNKNOWN("UNKNOWN");

        private String type;

        AudioFileTypes(String s) {
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
        return suffix.equals("jpg") || suffix.equals("png") || suffix.equals("bmp");
    }

    public static boolean isImageFile(File f) {
        String s = f.getAbsolutePath();
        String suffix = s.substring(s.lastIndexOf(".") + 1);
        suffix = suffix.toLowerCase();
        Log.i("VP", "File suffix: " + suffix);
        return suffix.equals("jpg") || suffix.equals("png") || suffix.equals("bmp");
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

    public static Bitmap getThumbnailBitmapFromStream(InputStream s) {
        Logger.log("Decoding file " + s + " to bitmap...");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 2;

        Rect dummy = new Rect();
        Bitmap bMap = BitmapFactory.decodeStream(s, dummy, options);
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

    /**
     * Creates an alert dialog with given title and message and shows it
     * @param ctx
     * @param title
     * @param message
     */
    public static void createAlertDialog(Context ctx, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setMessage(message);
        builder.show();
    }

    public static ArrayList<Track> getTracksFromArtistsList(ArrayList<Artist> artists) {
        ArrayList<Track> tracks = new ArrayList<>();
        for (Artist a : artists) {
            for (Track t : a.getTracks()) {
                tracks.add(t);
            }
        }
        return tracks;
    }

    public static class ArtistComparator implements Comparator<Artist> {

        @Override
        public int compare(Artist artist, Artist t1) {
            if (artist.getName() == null) {
                return 1;
            } else if (t1.getName() == null) {
                return -1;
            }
            return artist.getName().compareTo(t1.getName());
        }
    }

    public static class TrackComparator implements Comparator<Track> {

        @Override
        public int compare(Track track, Track t1) {
            if (track.getTitle() == null) {
                return 1;
            } else if (t1.getTitle() == null) {
                return -1;
            }
            return track.getTitle().compareTo(t1.getTitle());
        }
    }

}
