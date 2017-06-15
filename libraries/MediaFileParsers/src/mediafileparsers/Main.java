/*
 * 
 * 
 * 
 */
package mediafileparsers;

import parsers.flac.FlacWriter;

import java.io.File;

/**
 *
 * @author Ville
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                new TestWindow().setVisible(true);
//            }
//        });
        File testFile = new File("C:\\temp\\01-Big Red Gun.flac");
        FlacWriter flacWriter = new FlacWriter(null, testFile);
        flacWriter.writeLyricsToFile("LYRIIKAT DÄDÄDÄDÄDÄDÄ");
    }
    
}
