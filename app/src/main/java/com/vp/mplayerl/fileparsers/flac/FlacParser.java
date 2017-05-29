package com.vp.mplayerl.fileparsers.flac;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.content.Context;

/**
 * Created by Ville on 24.10.2016.
 */
public class FlacParser {

    private final int BLOCK_SIZE = 4;
    private File file;
    private Context context;
    private FileInputStream fInput;
    private DataInputStream dInput;
    private boolean isValidFlacFormat = true;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
    

    public FlacParser(Context context, File file) {
        this.file = file;
        this.context = context;
        try {
            fInput = new FileInputStream(file);
            dInput = new DataInputStream(fInput);
        } catch (IOException ex) {
            Log.w("STREAM OPENING", ""+ ex.getMessage());
        }
        try {
            isValidFlacFormat = fileIsFlacFile(dInput);
        } catch (IOException e) {
            Log.w("STREAM ERROR", "" + e.getMessage());
        }
    }

    public String getLyricsFromFile() {

        if (dInput == null) {
            Log.w("STREAM", "Stream is NULL");
            return "No lyrics";
        }

        String lyrics = "";

        String vorbisComments = getVorbisComments();

        lyrics = parseLyricsFromVorbisCommentString(vorbisComments);

        return lyrics;
    }

    public boolean close() {
        if (dInput == null) {
            Log.w("STREAM", "Stream is NULL");
            return false;
        }

        boolean exceptionThrown = false;
        if (fInput != null) {
            try {
                fInput.close();
            } catch (IOException e) {
                exceptionThrown = true;
                Log.w("CLOSING STREAM", ""+ e.getMessage());
            }

        }
        if (dInput != null) {
            try {
                dInput.close();
            } catch (IOException e) {
                exceptionThrown = true;
                Log.w("CLOSING STREAM", ""+ e.getMessage());
            }
        }
        if (exceptionThrown) {
            return false;
        } else {
            return true;
        }

    }

    public String getVorbisComments() {

        if (dInput == null) {
            Log.w("STREAM", "Stream is NULL");
            return "No lyrics";
        }

        String vorbisComments = "";

        try {
            long integer = 0, toBeSkipped = 0;

            int[] byteArray = new int[BLOCK_SIZE];

            do {

                // Read the second 32 bits in the file (METADATA_BLOCK_HEADER)
                integer = read32BitInteger(byteArray);

                // Parse the last 24 bits from previously read 32 bit byte array into an integer which controls
                // how many bytes is needed to be skipped afterwards
                toBeSkipped = (byteArray[1] << 16) + (byteArray[2] << 8) + (byteArray[3]);

                // If the first byte is 0x04 (BLOCK_TYPE 4: VORBIS_COMMENT) we have found the vorbis comments from file
                // See https://xiph.org/flac/format.html
                if (byteArray[0] == 4) {

                    // First parse the vorbis comments fully
                    vorbisComments = parseVorbisComments(toBeSkipped);

                    return vorbisComments;
                }
                if (integer != -1) {

                    // Skip the bytes calculated from the last 24 bits gotten from the metadata block header
                    dInput.skip(toBeSkipped);
                }
            } while (integer != -1);
        } catch (FileNotFoundException fnfEx) {
            Log.w("FileNotFound", "" + fnfEx.getMessage());
        } catch (IOException ioEx) {
            Log.w("IOException", "" + ioEx.getMessage());
        }
        return vorbisComments;
    }

    // Reads a 32 bit integer from the file
    private long read32BitInteger(int[] byteArray) throws IOException {
        int oneChar = 0;
        for (int i = 0; i < BLOCK_SIZE; i++) {

            // Read one byte
            oneChar = dInput.read();

            // If byte is -1 break the loop
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
        long integer = 0;

        // Combine the bytes into a single 32 bit integer
        for (int i = 0; i < BLOCK_SIZE - 1; i++) {
            integer += byteArray[i] << 24 - (i * 8);
        }

        // Add the last byte into the result integer
        integer += byteArray[BLOCK_SIZE - 1];
        return integer;
    }

    // Parses the vorbis comments from file. The File cursor needs to be pointing at the beginning of the vorbis comments
    private String parseVorbisComments(long lengthInBytes) throws IOException {
        //Log.d("ParseVorbisComments", lengthInBytes + "");
        StringBuilder stringBuilder = new StringBuilder("");
        int oneByte = 0;

        // Read the bytes one by one and every time check that reading is successful
        for (int i = 0; i < lengthInBytes; i++) {
            oneByte = dInput.read();
            if (oneByte == -1) {
                break;
            }
            stringBuilder.append((char) oneByte);
        }

        return stringBuilder.toString();
    }

    // Parses the lyrics from vorbis comment string
    private String parseLyricsFromVorbisCommentString(String vorbisComments) {
        //Log.d("ParseLyrics", vorbisComments);
        StringBuilder lyrics = new StringBuilder();

        // Find the key "LYRICS" from the string
        int lyricsStart = vorbisComments.indexOf("LYRICS=");

        // If the file doesn't have lyrics, return
        if (lyricsStart == -1) {
            return "No lyrics";
        }
        for (int i = lyricsStart + "LYRICS=".length(); i < vorbisComments.length() && vorbisComments.charAt(i) != '\0'; i++) {
            lyrics.append(vorbisComments.charAt(i));
        }
        //Log.d("LYRICS", lyrics.toString());
        return lyrics.toString();
    }

    private boolean fileIsFlacFile(DataInputStream bIS) throws IOException {
        if (bIS == null) {
            return false;
        }
        int[] byteArray = new int[BLOCK_SIZE];
        read32BitInteger(byteArray);
        if (byteArray[0] == 'f'
                && byteArray[1] == 'L'
                && byteArray[2] == 'a'
                && byteArray[3] == 'C') {
            return true;
        } else {
            return false;
        }

    }
}
