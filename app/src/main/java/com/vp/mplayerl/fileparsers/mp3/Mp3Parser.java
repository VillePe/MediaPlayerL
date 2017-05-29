/*
 * 
 * 
 * 
 */

package com.vp.mplayerl.fileparsers.mp3;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import android.content.Context;

/**
 * Created by Ville on 24.10.2016.
 */
public class Mp3Parser {

    private boolean isFileValidFormat = true;
    private File file;
    private Id3V2Header header;
    private ArrayList<Id3V2Frame> frames;
    private Context context;
    private FileInputStream fInput;
    private BufferedInputStream bInput;

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


    public Mp3Parser(Context context, File file) throws IllegalArgumentException{
        this.file = file;
        this.context = context;
        frames = new ArrayList<>();
        try {
            fInput = new FileInputStream(file);
            bInput = new BufferedInputStream(fInput);
        } catch (IOException ex) {
            Log.w("STREAM OPENING", ex.getMessage());
        }
        isFileValidFormat = getMetadataHeader();
        if (isFileValidFormat) {
            getFileMetadata();
        } else {
            throw new IllegalArgumentException("File format is not valid!");
        }
    }

    public String getUnSyncedLyrics() {
        StringBuilder sb = new StringBuilder();
        if (frames.size() > 0) {
            for (Id3V2Frame frame : frames) {
                if (frame.getFrameID().equals("USLT")) {
                    int[] bytes = frame.getBytes();
                    int textEncoding = bytes[0];
                    String language = "";
                    for (int i = 1; i < 4; i++) {
                        language += (char)bytes[i];
                    }
                    for (int i = 5; i < bytes.length; i++) {
                        sb.append((char)bytes[i]);
                    }
                    return sb.toString();
                }
            }
        }
        return "No lyrics";
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

    private boolean getMetadataHeader() {
        header = new Id3V2Header();
        int[] bytes = new int[10];
        for (int i = 0; i < 10; i++) {
            bytes[i] = readOneByte();
        }
        return header.parseHeader(bytes);
    }

    private void getFileMetadata() {

        if (bInput == null) {
            Log.w("STREAM", "Stream is NULL");
        }

        // If header doesn't have Extended_Header included, go straight to parsing
        // frames
        if (header.getFlags()[Id3V2Header.Flags.EXT_HEADER.getIndex()] == false) {
            Id3V2Frame frame = null;
            do {
                frame = Id3V2Frame.FrameBuilder.createFrame(bInput);
                if (frame != null) {
////                    System.out.println(frame.getFrameID());
                    frames.add(frame);
                }
            } while (frame != null);
        }
    }

    private int readOneByte() {
        try {
            return bInput.read();
        } catch (IOException e) {
            Log.w("ReadByte", e.getMessage());
        }
        return -1;
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
}



