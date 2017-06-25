package parsers.flac;

import mediafileparsers.Context;
import parsers.Misc.VorbisComments;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ville on 15.6.2017.
 */
public class FlacWriter {

    private Context context;
    private File file;
    private File tempFile;
    private FlacParser flacParser;

    public FlacWriter(Context context, File file) {
        this.context = context;
        this.file = file;
        this.flacParser = new FlacParser(context, file);
    }

    public boolean writeLyricsToFile(String lyrics) {
        tempFile = new File("C:/temp/klooni.flac");

        MetadataBlockCollection collection = new MetadataBlockCollection(flacParser);
        String vorbisComments = collection.getVorbisCommentsBlock().getDataAsString();
        System.out.println("VORBIS_COMMENTS:");
        System.out.println(vorbisComments);
        System.out.println("VORBIS_COMMENTS LENGTH: ");
        System.out.println(VorbisComments.getVorbisCommentListLength(vorbisComments));
        System.out.println();
        System.out.printf("VORBIS_COMMENTS LISTED: ");
        HashMap<String, ArrayList<String>> vorbisCommentsListed = VorbisComments.getVorbisCommentsListed(vorbisComments);


        try {
            WriteFileCopy(file, tempFile, collection);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void WriteFileCopy(File fIn, File fOut,  MetadataBlockCollection blocks) throws IOException {
        final InputStream in = new BufferedInputStream(new FileInputStream(fIn));
        final OutputStream out = new BufferedOutputStream(new FileOutputStream(fOut));
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
        System.out.println("File size: " + fIn.length());
        System.out.println("Clone created!");
    }
}