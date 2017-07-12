/*
 * 
 * 
 * 
 */

package com.vp.parsers.mp3;

import com.vp.mediafileparsers.Utils;

import java.io.*;

/**
 * Created by Ville on 24.10.2016.
 */
public class Mp3Parser {

    // ID3v2 documentation http://id3.org/id3v2.3.0

    private boolean isFileValidFormat = true;
    private File file;
    private FileInputStream fInput;
    private BufferedInputStream bInput;
    private IIdCollection collection;

    public Mp3Parser(File f) {
        this.file = f;
        collection = getMetadata(f);
    }

    public boolean close() {
        boolean noExceptionThrown = true;
        if (fInput != null) {
            try {
                fInput.close();
            } catch (IOException e) {
                noExceptionThrown = false;
                Log.w("CLOSING STREAM", e.getMessage());
            }

        }
        if (bInput != null) {
            try {
                bInput.close();
            } catch (IOException e) {
                noExceptionThrown = false;
                Log.w("CLOSING STREAM", e.getMessage());
            }
        }
        return noExceptionThrown;
    }

    private IIdCollection getMetadata(File f) {
        // TODO: implement more versions of MP3 metadata formats
        try {
            fInput = new FileInputStream(f);
            bInput = new BufferedInputStream(fInput);
        } catch (FileNotFoundException e) {
            Utils.printException(e);
        }
        Id3V2Collection collection = new Id3V2Collection(bInput);
        if (!collection.isRightFileFormat()) throw new IllegalArgumentException("Could not parse metadata from file!");
        return collection;
    }

    // Reads a 32 bit integer from the file
    private int read32BitInteger(int[] byteArray) throws IOException {
        int oneChar = 0;
        for (int i = 0; i < byteArray.length; i++) {

            // Read one byte
            oneChar = bInput.read();

            // If byte is -1 (0xFFFFFFFF) break the loop
            // TODO: This needs better implementation!
            if (oneChar == -1) {
                break;
            }

            // Take one byte to check that the reading was successful
            byteArray[i] = oneChar;
        }
        if (oneChar == -1) {
            return -1;
        }
        int integer = 0;

        // Combine the bytes into a single 32 bit integer
        for (int i = 0; i < byteArray.length - 1; i++) {
            integer += byteArray[i] << 24 - (i * 8);
        }

        // Add the last byte into the result integer
        integer += byteArray[byteArray.length - 1];
        return integer;
    }

    public String getUnSyncedLyrics() {
        return collection.getUnsyncedLyrics();
    }

    public ByteArrayInputStream getAttachedPicture() {
        return collection.getAttachedPicture();
    }

    private static class Log {

        public Log() {
        }

        public static void i(String s1, String s2) {

        }

        public static void w(String s1, String s2) {

        }

        public static void e(String s1, String s2) {

        }

        public static void d(String s1, String s2) {

        }
    }
}



