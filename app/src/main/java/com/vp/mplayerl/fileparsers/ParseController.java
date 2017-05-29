package com.vp.mplayerl.fileparsers;

import android.content.Context;
import android.util.Log;

import com.vp.mplayerl.R;
import com.vp.mplayerl.fileparsers.flac.FlacParser;
import com.vp.mplayerl.fileparsers.mp3.Mp3Parser;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Ville on 27.10.2016.
 */

public class ParseController {

    public static String getLyricsFromFile(Context ctx, File file) {
        try {
            FileInputStream fInput = new FileInputStream(file);
            DataInputStream dInput = new DataInputStream(fInput);
            char c = (char)dInput.read();
            if (c == 'f') {
                FlacParser fParser = new FlacParser(ctx, file);
                String lyrics =  fParser.getLyricsFromFile();
                fParser.close();
                return lyrics;
            } else if (c == 'I') {
                Mp3Parser mp3Parser = new Mp3Parser(ctx, file);
                String lyrics =  mp3Parser.getUnSyncedLyrics();
                mp3Parser.close();
                return lyrics;
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
        return "No lyrics";
    }

}
