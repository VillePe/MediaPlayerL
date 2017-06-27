package com.vp.parsers.flac;

import com.vp.parsers.Misc.VorbisComments;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ville on 15.6.2017.
 */
public class FlacWriter {

    // For flac format documentation see https://xiph.org/flac/format.html

    private File file;
    private FlacParser flacParser;

    public FlacWriter(File file) {
        this.file = file;
        this.flacParser = new FlacParser(file);
    }

    public FlacWriter(FlacParser flacParser) {
        this.flacParser = flacParser;
        this.file = flacParser.getFile();
    }

    public boolean writeVorbisCommentsToFile(File outFile, VorbisComments comments) throws FlacException {
        MetadataBlockCollection collection = new MetadataBlockCollection(flacParser);
        int vorbisCommentsIndex = collection.indexOf(4);
        if (vorbisCommentsIndex <= -1) {
            throw new FlacException("Could not find vorbis comments!");
        }
        MetadataBlock vorbisBlock = collection.getVorbisCommentsBlock();
        vorbisBlock.setData(comments.getAsByteArrayList());

        try {
            WriteFileCopy(file, outFile, collection);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean writeLyricsToFile(File outFile, String lyrics) throws FlacException {
        VorbisComments comments = new VorbisComments(flacParser);
        comments.setLyrics(lyrics);
        return writeVorbisCommentsToFile(outFile, comments);
    }

    public static void WriteFileCopy(File fIn, File fOut,  MetadataBlockCollection blocks) throws IOException {
        final InputStream in = new BufferedInputStream(new FileInputStream(fIn));
        final OutputStream out = new BufferedOutputStream(new FileOutputStream(fOut));
        long skipped = in.skip(blocks.getTotalSize()+4);
        if (skipped <= 0) {
            throw new IOException("Skipped 0 bytes! An unknown error occured or something is wrong with given file");
        }
        out.write("fLaC".getBytes());
        for (MetadataBlock block : blocks) {
            out.write(block.getHeaderAsBytes());
            for (int i = 0; i < block.getLength(); i++) {
                out.write(block.getData().get(i));
            }
        }

        int oneChar = 0;
        while ((oneChar = in.read()) != -1) {
            out.write(oneChar);
        }
        out.flush();
        out.close();
        in.close();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public FlacParser getFlacParser() {
        return flacParser;
    }

    public void setFlacParser(FlacParser flacParser) {
        this.flacParser = flacParser;
    }

    public class FlacException extends IOException {

        public FlacException() {
            super();
        }

        public FlacException(String message) {
            super(message);
        }

    }
}