/*
 * 
 * 
 * 
 */
package mediafileparsers;

import parsers.Misc.VorbisComments;
import parsers.flac.FlacParser;
import parsers.flac.FlacWriter;
import parsers.flac.MetadataBlock;
import parsers.flac.MetadataBlockCollection;

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
//        File testFile = new File("C:/temp/MacCunn  - Part 2.flac");
        File testFile = new File("C:/temp/02 Rusted From The Rain.flac");
        FlacWriter flacWriter = new FlacWriter(null, testFile);

        HashMap<String, ArrayList<String>> testiMap = new HashMap<>();
        testiMap.put("TESTI", new ArrayList<String>(){{add("YKSI");}});
        testiMap.put("TESTI2", new ArrayList<String>(){{add("KAKSI");}});
        testiMap.put("TESTI3", new ArrayList<String>(){{add("KOLME");}});
        testiMap.put("TESTI4", new ArrayList<String>(){{add("NELJÄ");}});

        FlacParser parser1 = new FlacParser(null, testFile);

        VorbisComments testi = new VorbisComments(new FlacParser(null, testFile));

        byte[] array = testi.getAsByteArray();
        BufferedOutputStream bout = null;
        try {
            OutputStream out = new FileOutputStream("C:/temp/bittitesti.bin");
            bout = new BufferedOutputStream(out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        if (bout == null) return;
        for (int i = 0; i < array.length; i++) {
            try {
                bout.write(array[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            bout.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (true)
            return;

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