package com.vp.mediafileparsers;

import com.vp.parsers.flac.FlacParser;
import com.vp.parsers.flac.MetadataBlock;
import com.vp.parsers.flac.MetadataBlockCollection;
import com.vp.parsers.mp3.Mp3Parser;

import java.io.*;

/**
 * Created by Ville on 27.10.2016.
 */

public class ParseController {

    public static String getLyricsFromFile(File file) {
        try {
            char c = getAudioFileType(file);
            if (c == 'f') {
                com.vp.parsers.flac.FlacParser fParser = new com.vp.parsers.flac.FlacParser(file);
                String lyrics =  fParser.getLyricsFromFile();
                fParser.close();
                return lyrics;
            } else if (c == 'm') {
                com.vp.parsers.mp3.Mp3Parser mp3Parser = new com.vp.parsers.mp3.Mp3Parser(file);
                String lyrics =  mp3Parser.getUnSyncedLyrics();
                if (lyrics.isEmpty()) System.err.println("Parsecontroller: File had no lyrics or reading failed");
                mp3Parser.close();
                return lyrics;
            }
        } catch (IllegalArgumentException | IOException ex) {
            if (ex.getMessage() != null) System.out.println(ex.getMessage());
            ex.printStackTrace(System.err);
        }
        return "No lyrics";
    }

    public static ByteArrayInputStream getPictureFromFile(File file) {
        char c = getAudioFileType(file);
        ByteArrayInputStream resultStream = null;
        if (c == 'f') {
            FlacParser flacParser = new FlacParser(file);
            MetadataBlockCollection collection = new MetadataBlockCollection(flacParser);
            MetadataBlock picBlock = collection.getPictureBlock();
            if (picBlock != null) {
                return flacParser.getTrackPicture(picBlock);
            }
        } else if (c == 'm') {
            Mp3Parser mp3Parser = new Mp3Parser(file);
            return mp3Parser.getAttachedPicture();
        }
        return null;
    }

    public static char getAudioFileType(File file) {
        try {
            FileInputStream fInput = new FileInputStream(file);
            DataInputStream dInput = new DataInputStream(fInput);
            char c = (char)dInput.read();
            if (c == 'f') return 'f';
            else if (c == 'I') return 'm';
        } catch (IOException ex) {
            if (ex.getMessage() != null) System.out.println(ex.getMessage());
            ex.printStackTrace(System.err);
        }
        return '0';
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
