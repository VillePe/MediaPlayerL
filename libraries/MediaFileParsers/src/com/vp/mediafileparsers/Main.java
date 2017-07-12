/*
 * 
 * 
 * 
 */
package com.vp.mediafileparsers;

import com.vp.parsers.flac.FlacParser;
import com.vp.parsers.flac.MetadataBlockCollection;
import com.vp.parsers.mp3.Mp3Parser;

import java.io.*;

/**
 * @author Ville
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        File f = new File("C:/temp/01-Big Red Gun.flac");
        File f1 = new File("C:/temp/01 - Monarchy Of Roses.mp3");
        File f2 = new File("C:/temp/01-Big Red Gun.flac");
        File f3 = new File("C:/temp/3. Everywhere I Go.mp3");

        File bf1 = new File("C:/temp/kuva1.jpeg");
        File bf2 = new File("C:/temp/kuva2.jpeg");
        File bf3 = new File("C:/temp/kuva3.jpeg");

        File lf1 = new File("C:/temp/lyrics1.txt");
        File lf2 = new File("C:/temp/lyrics2.txt");
        File lf3 = new File("C:/temp/lyrics3.txt");

        writeImageAndLyrics(f1, bf1, lf1);
        writeImageAndLyrics(f2, bf2, lf2);
        writeImageAndLyrics(f3, bf3, lf3);
    }

    public static void writeImageAndLyrics(File in, File outImg, File outLyrics) {
        try {
            ByteArrayInputStream image = ParseController.getPictureFromFile(in);
            if (image == null) return;
            FileOutputStream fOut = new FileOutputStream(outImg);
            byte[] buffer = new byte[1024];
            int read = 0;
            while (true) {
                read = image.read(buffer, 0, buffer.length);
                if (read == -1) break;
                fOut.write(buffer);
            }
            fOut.flush();

            FileWriter writer = new FileWriter(outLyrics);
            writer.write(ParseController.getLyricsFromFile(in));
            writer.flush();

            fOut.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}