/*
 * 
 * 
 * 
 */
package mediafileparsers;

import parsers.flac.FlacParser;
import parsers.flac.FlacWriter;
import parsers.flac.MetadataBlock;
import parsers.flac.MetadataBlockCollection;

import java.io.*;

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
        FlacParser parser = new FlacParser(null, testFile);
        MetadataBlockCollection blocks = new MetadataBlockCollection(parser);
        System.out.println("Blocks count: " + blocks.size());
        System.out.println("Block total size: " + blocks.getTotalSize());
        System.out.println();
        for (MetadataBlock block : blocks) {
            System.out.println("Block type: " + block.getBlockType());
            if (MetadataBlock.blockHeaders.containsKey(block.getBlockType())) {
                System.out.println(MetadataBlock.blockHeaders.get(block.getBlockType()));
            }
            System.out.println("Index: " + block.getIndex());
            System.out.println("Length: " + block.getLength());
            System.out.println("Is last: " + block.isLastBlock());
//            if (block.getLength() < 5000) {
//                System.out.println("Data: ");
//                System.out.println(block.getDataAsString());;
//            } else {
//                System.out.println("Block length over 1000, skipping data...");
//            }
            System.out.println();
        }
        try {
            WriteFileCopy(testFile, blocks);
            throw new IOException();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void WriteFileCopy(File f, MetadataBlockCollection blocks) throws IOException {
        File tempFile = new File("C:/temp/Klooni.flac");
        final InputStream in = new BufferedInputStream(new FileInputStream(f));
        final OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
        int bytesWritten = 0;
        in.skip(blocks.getTotalSize());
        System.out.println("Blocks total size: " + blocks.getTotalSize());
        out.write("fLaC".getBytes());
        bytesWritten += 4;
        for (MetadataBlock block : blocks) {
            out.write(block.getHeaderAsBytes());
            bytesWritten += 4;
            System.out.println(block.getLength());
            for (int i = 0; i < block.getLength(); i++) {
                out.write(block.getData().get(i));
                bytesWritten++;
            }
            System.out.println("Bytes written: " + bytesWritten);
        }

        System.out.println("Metadata written. Bytes written: " + bytesWritten);
//        bytesWritten = 0;
        int oneChar = 0;
        while ((oneChar = in.read()) != -1) {
            out.write(oneChar);
            bytesWritten++;
        }
        out.flush();
        out.close();
        in.close();
        System.out.println("Bytes written: " + bytesWritten);
        System.out.println("File size: " + f.length());
        System.out.println("Clone created!");
    }
    
}
