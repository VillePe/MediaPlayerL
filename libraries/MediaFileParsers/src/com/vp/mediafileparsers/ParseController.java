package com.vp.mediafileparsers;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Ville on 27.10.2016.
 */

public class ParseController {

    public static String getLyricsFromFile(File file) {
        try {
            FileInputStream fInput = new FileInputStream(file);
            DataInputStream dInput = new DataInputStream(fInput);
            char c = (char)dInput.read();
            if (c == 'f') {
                com.vp.parsers.flac.FlacParser fParser = new com.vp.parsers.flac.FlacParser(file);
                String lyrics =  fParser.getLyricsFromFile();
                fParser.close();
                return lyrics;
            } else if (c == 'I') {
                com.vp.parsers.mp3.Mp3Parser mp3Parser = new com.vp.parsers.mp3.Mp3Parser(file);
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
    
    private static class Log {

        public Log() {
        }

        public static void i(String s1, String s2) {
            System.out.println(s1 + ": " + s2);
        }

        public static void w(String s1, String s2) {
            System.out.println(s1 + ": " + s2);
        }

        public static void e(String s1, String s2) {
            System.out.println(s1 + ": " + s2);
        }

        public static void d(String s1, String s2) {
            System.out.println(s1 + ": " + s2);
        }
    }

}
