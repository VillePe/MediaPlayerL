/*
 * 
 * 
 * 
 */
package com.vp.mediafileparsers;

import com.vp.parsers.Misc.VorbisComments;
import com.vp.parsers.flac.FlacParser;
import com.vp.parsers.flac.FlacWriter;
import com.vp.parsers.flac.MetadataBlock;
import com.vp.parsers.flac.MetadataBlockCollection;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Ville
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                new TestWindow().setVisible(true);
//            }
//        });
//        File inFile = new File("C:/temp/MacCunn  - Part 2.flac");
//        File inFile = new File("C:/temp/02 Rusted From The Rain.flac");
        File inFile = new File("C:/temp/01-Big Red Gun.flac");
        File outFile = new File("C:/temp/Klooni.flac");
        FlacWriter flacWriter = new FlacWriter(inFile);

        try {
            VorbisComments comments = new VorbisComments(flacWriter.getFlacParser());
            comments.setValue("ARTIST", "Michael Jackson");
            comments.setValue("TITLE", "Bad");
            comments.setValue("LYRICS", "Lorem ipsum dolor sit amet, consectetur adipisci elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua.\n" +
                    "        Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid ex ea commodi consequat.\n" +
                    "        Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.\n" +
                    "        Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n" +
                    "        Lorem ipsum dolor sit amet, consectetur adipisci elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua.\n" +
                    "        Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid ex ea commodi consequat.\n" +
                    "        Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.\n" +
                    "        Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
            flacWriter.writeVorbisCommentsToFile(outFile, comments);
        } catch (FlacWriter.FlacException e) {
            e.printStackTrace();
        }
//        FlacParser parser = new FlacParser(null, inFile);
//        MetadataBlockCollection blocks = new MetadataBlockCollection(parser);
//        System.out.println("Blocks count: " + blocks.size());
//        System.out.println("Block total size: " + blocks.getTotalSize());
//        System.out.println();
//        for (MetadataBlock block : blocks) {
//            System.out.println("Block type: " + block.getBlockType());
//            if (MetadataBlock.blockHeaders.containsKey(block.getBlockType())) {
//                System.out.println(MetadataBlock.blockHeaders.get(block.getBlockType()));
//            }
//            System.out.println("Index: " + block.getIndex());
//            System.out.println("Length: " + block.getLength());
//            if (block.getBlockType() == 4) {
//                System.out.println("Data:");
//                System.out.println(block.getDataAsString());
//            }
//            System.out.println("Is last: " + block.isLastBlock());
//            System.out.println();
//        }
    }

}