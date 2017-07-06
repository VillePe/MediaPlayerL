/*
 * 
 * 
 * 
 */
package com.vp.parsers.mp3;

import java.io.BufferedInputStream;
import java.io.IOException;

public class Id3V2Frame {

    private String frameID = "";
    private int size;
    private int[] data;
    private boolean[] flags = new boolean[16];
    private BufferedInputStream bInput;
    private int textEncoding;
    private String language;

    private int maxDataSize = 20 * 1024 * 1024;

    public Id3V2Frame(BufferedInputStream bInput) {
        this.bInput = bInput;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Frame ID: ").append(frameID).append("\n");
        sb.append("Size: ").append(size).append("\n");
        for (int i = 0; i < flags.length; i++) {
            sb.append("Flag ").append(i).append(". : ").append(flags[i]).append("\n");
        }
        return sb.toString();
    }

    // For more info see http://id3.org/id3v2.3.0
    public boolean parseFrame() {
        if (bInput == null) {
            return false;
        }
        try {
            if (!parseFrameID()) {
                return false;
            }
            parseSize();
            parseFlags();
            fillBytes();
        } catch (Exception e) {
            if (e.getMessage() != null) System.err.println(e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }

        return true;
    }

    private boolean parseFrameID() throws IOException {
        for (int i = 0; i < 4; i++) {
            char oneChar = (char) bInput.read();
            if (oneChar >= '0' && oneChar <= 'Z') {
                frameID += oneChar;
            } else {
                return false;
            }
        }
        return true;
    }

    private void parseSize() throws IOException {
        int[] tempBytes = new int[4];
        for (int i = 0; i < 4; i++) {
            tempBytes[i] = bInput.read();
        }
        size = (tempBytes[0] << 24) | (tempBytes[1] << 16) | (tempBytes[2] << 8) | (tempBytes[3]);
    }

    private void parseFlags() throws IOException {
        int[] tempBytes = new int[2];
        for (int i = 0; i < 2; i++) {
            tempBytes[i] = bInput.read();
        }
    }

    private void fillBytes() throws IOException {
        if (size > maxDataSize) {
            throw new DataSizeException();
        }
        data = new int[size];

        if (frameID.equals("APIC")) {
            for (int i = 0; i < size; i++) {
                data[i] = bInput.read();
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (!frameID.equals("APIC")) {
                    data[i] = this.bInput.read();
                }
            }
        }
    }

    public int getTextEncoding() {
        return textEncoding;
    }

    public void setTextEncoding(int textEncoding) {
        this.textEncoding = textEncoding;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getFrameID() {
        return frameID;
    }

    public void setFrameID(String frameID) {
        this.frameID = frameID;
    }

    public int getSize() {
        return size;
    }

    public int[] getData() {
        return data;
    }

    public byte[] getDataAsBytes() {
        byte[] result = new byte[data.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte)data[i];
        }
        return result;
    }

    public void setData(int[] data) {
        this.data = data;
    }

    public boolean[] getFlags() {
        return flags;
    }

    public void setFlags(boolean[] flags) {
        this.flags = flags;
    }

    public BufferedInputStream getdInput() {
        return bInput;
    }

    public void setdInput(BufferedInputStream dInput) {
        this.bInput = dInput;
    }

    public static class FrameBuilder {

        public static Id3V2Frame createFrame(BufferedInputStream dInput) {
            Id3V2Frame frame = new Id3V2Frame(dInput);
            if (frame.parseFrame()) {
                return frame;
            } else {
                return null;
            }
        }
    }

    public class DataSizeException extends IOException {

        public DataSizeException() {
            super("Could not load data. Size was too big!");
        }
    }
}
