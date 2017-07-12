package com.vp.parsers.mp3;

import com.vp.mediafileparsers.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Ville on 8.7.2017.
 */
public class Id3V2Collection implements IIdCollection{

    private ArrayList<Id3V2Frame> frames;
    private Id3V2Header header;
    private boolean isRightFileFormat;

    public Id3V2Collection(BufferedInputStream bInput) throws IllegalArgumentException {
        header = getMetadataHeader(bInput);
        if (header.getFileIdentifier().equals("ID3")) {
            frames = getFileMetadata(bInput);
            this.isRightFileFormat = true;
        } else {
            this.isRightFileFormat = false;
        }
    }

    public String getUnsyncedLyrics() {
        StringBuilder sb = new StringBuilder();
        Id3V2Frame frame = getFrame("USLT");
        if (frame != null) {
            int[] bytes = frame.getData();
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
        return "No lyrics";
    }

    public ByteArrayInputStream getAttachedPicture() {
        ByteArrayInputStream resultStream = null;
        Id3V2Frame frame = getFrame("APIC");
        if (frame != null) {
            int mimeLength = 0;
            int descriptionLength = 0;
            // Start from second byte because first was consumed by text encoding
            for (int i = 1; frame.getData()[i] != 0 && i < frame.getData().length; i++) {
                mimeLength++;
            }
            // Offset is three because the null terminator consumes one extra byte which is not taken into account in mime length
            for (int i = 3 + mimeLength; frame.getData()[i] != 0 && i < frame.getData().length; i++) {
                descriptionLength++;
            }
            // 2 bytes from text encoding and picture type, and 2 bytes from Mimes and descriptions text string null terminators
            int offset = 4 + mimeLength + descriptionLength;
            try {
                byte[] byteArray = Arrays.copyOfRange(frame.getDataAsBytes(), offset, frame.getData().length - offset);
                resultStream = new ByteArrayInputStream(byteArray);
            } catch (IllegalArgumentException ex) {
                Utils.printException(ex);
            }

        }
        return resultStream;
    }

    private Id3V2Header getMetadataHeader(BufferedInputStream bInput) {
        header = new Id3V2Header(bInput);
        return header;
    }

    public Id3V2Frame getFrame(String id) {
        if (frames.size() > 0) {
            for (Id3V2Frame frame : frames) {
                if (frame.getFrameId().equals(id)) {
                    return frame;
                }
            }
        }
        return null;
    }

    @Override
    public IIdHeader getFileHeader() {
        return this.header;
    }

    @Override
    public boolean isRightFileFormat() {
        return isRightFileFormat;
    }

    public ArrayList<Id3V2Frame> getFileMetadata(BufferedInputStream bInput) {

        ArrayList<Id3V2Frame> result = new ArrayList<>();

        if (bInput == null) {
            System.err.println("File was invalid. Could not setup input stream");
        }

        // If header doesn't have Extended_Header included, go straight to parsing
        // frames
        if (!header.getFlags()[Id3V2Header.Flags.EXT_HEADER.getIndex()]) {
            Id3V2Frame frame = null;
            do {
                frame = Id3V2Frame.FrameBuilder.createFrame(bInput);
                if (frame != null) {
                    result.add(frame);
                }
            } while (frame != null);
        }
        return result;
    }

}
