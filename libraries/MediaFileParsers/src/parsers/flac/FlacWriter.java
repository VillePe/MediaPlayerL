package parsers.flac;

import mediafileparsers.Context;

import java.io.File;

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
        tempFile = file;
        MetadataBlockCollection collection = new MetadataBlockCollection(flacParser);
        String vorbisComments = collection.getVorbisCommentsBlock().getDataAsString();
        System.out.println(vorbisComments);
        System.out.println(flacParser.getVorbisCommentListLength(vorbisComments));
        flacParser.getVorbisCommentsListed(vorbisComments);

        return false;
    }
}
