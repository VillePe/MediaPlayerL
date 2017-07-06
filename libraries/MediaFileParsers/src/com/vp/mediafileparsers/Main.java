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
        File f = new File("C:/temp/01 - Monarchy Of Roses.mp3");


        ByteArrayInputStream image = ParseController.getPictureFromFile(f);
        if (image == null) return;

        File bf = new File("C:/temp/kuva.jpeg");
        try {
            FileOutputStream fOut = new FileOutputStream(bf);
            byte[] buffer = new byte[1024];
            int read = 0;
            while (true) {
                read = image.read(buffer, 0, buffer.length);
                if (read == -1) break;
                fOut.write(buffer);
            }
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}