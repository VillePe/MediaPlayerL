package parsers.flac;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import mediafileparsers.Context;
import mediafileparsers.Utils;

/**
 * Created by Ville on 24.10.2016.
 */
public class FlacParser {

    private final int BLOCK_SIZE = 4;
    private File file;
    private Context context;
    private FileInputStream fInput;
    private BufferedInputStream bInput;
    private boolean isValidFlacFormat = true;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public FlacParser(Context context, File file) {
        this.file = file;
        this.context = context;
        try {
            reset();
        } catch (IOException ex) {
            Log.w("STREAM OPENING", "" + ex.getMessage());
        }
        try {
            isValidFlacFormat = fileIsFlacFile(bInput);
        } catch (IOException e) {
            Log.w("STREAM ERROR", "" + e.getMessage());
        }
    }

    public void reset() throws FileNotFoundException {
        fInput = new FileInputStream(file);
        bInput = new BufferedInputStream(fInput);
    }

    public String getLyricsFromFile() {

        if (bInput == null) {
            Log.w("STREAM", "Stream is NULL");
            return "No lyrics";
        }

        String lyrics = "";

        MetadataBlockCollection collection = new MetadataBlockCollection(getMetadataBlocks());

        String vorbisComments = collection.getVorbisCommentsBlock().getDataAsString();

        lyrics = parseLyricsFromVorbisCommentString(vorbisComments);

        return lyrics;
    }

    public boolean close() {
        if (bInput == null) {
            Log.w("STREAM", "Stream is NULL");
            return false;
        }

        boolean exceptionThrown = false;
        if (fInput != null) {
            try {
                fInput.close();
            } catch (IOException e) {
                exceptionThrown = true;
                Log.w("CLOSING STREAM", "" + e.getMessage());
            }

        }
        if (bInput != null) {
            try {
                bInput.close();
            } catch (IOException e) {
                exceptionThrown = true;
                Log.w("CLOSING STREAM", "" + e.getMessage());
            }
        }
        if (exceptionThrown) {
            return false;
        } else {
            return true;
        }
    }

    public ArrayList<MetadataBlock> getMetadataBlocks() {
        ArrayList<MetadataBlock> result = new ArrayList<>();
        try {
            reset();
            fileIsFlacFile(bInput);
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }

        try {
            long integer = 0, blockLength = 0;
            boolean isLastBlock = false;
            int blockType, i = 0;

            int[] byteArray = new int[BLOCK_SIZE];

            do {

                // Read the second 32 bits in the file (METADATA_BLOCK_HEADER)
                integer = read32BitInteger(byteArray);

                if ((byteArray[0] & 0x80) == 0x80) {
                    isLastBlock = true;
                    // Convert the first 1 to 0 so block type is easier to get
                    System.out.println(byteArray[0]);
                    byteArray[0] = byteArray[0] & 0x7F;
                }
                // Parse the last 24 bits from previously read 32 bit byte array into an integer which is
                // the blocks length in bytes excluding header
                blockLength = (byteArray[1] << 16) + (byteArray[2] << 8) + (byteArray[3]);
                blockType = byteArray[0];
                if (integer != -1) {
                    // Skip the bytes calculated from the last 24 bits gotten from the metadata block header
                    MetadataBlock block = new MetadataBlock(isLastBlock, blockType, blockLength, i);
                    result.add(block);
                    for (int j = 0; j < blockLength; j++) {
                        block.addData((byte)bInput.read());
                    }
                    i++;
                }
            } while (integer != -1 && !isLastBlock);
        } catch (FileNotFoundException fnfEx) {
            Log.w("FileNotFound", "" + fnfEx.getMessage());
        } catch (IOException ioEx) {
            Log.w("IOException", "" + ioEx.getMessage());
        }

        return result;
    }

    // Reads a 32 bit integer from the file
    private long read32BitInteger(int[] byteArray) throws IOException {
        return Utils.read32BitIntegerLE(bInput, byteArray);
    }

    // Parses the vorbis comments from file. The File cursor needs to be pointing at the beginning of the vorbis comments
    private String parseVorbisComments(long lengthInBytes) throws IOException {
        //Log.d("ParseVorbisComments", lengthInBytes + "");
        StringBuilder stringBuilder = new StringBuilder("");
        int oneByte = 0;

        // Read the bytes one by one and every time check that reading is successful
        for (int i = 0; i < lengthInBytes; i++) {
            oneByte = bInput.read();
            if (oneByte == -1) {
                break;
            }
            stringBuilder.append((char) oneByte);
        }

        return stringBuilder.toString();
    }

    // Parses the lyrics from vorbis comment string
    private String parseLyricsFromVorbisCommentString(String vorbisComments) {
        //Log.d("ParseLyrics", vorbisComments);
        StringBuilder lyrics = new StringBuilder();

        // Find the key "LYRICS" from the string
        int lyricsStart = vorbisComments.indexOf("LYRICS=");

        // If the file doesn't have lyrics, return
        if (lyricsStart == -1) {
            return "No lyrics";
        }
        for (int i = lyricsStart + "LYRICS=".length(); i < vorbisComments.length() && vorbisComments.charAt(i) != '\0'; i++) {
            lyrics.append(vorbisComments.charAt(i));
        }
        //Log.d("LYRICS", lyrics.toString());
        return lyrics.toString();
    }

    public long getVorbisCommentVendorLength(String vorbisComments) {
        return Utils.read32BitIntegerBE(vorbisComments);
    }

    public long getVorbisCommentListLength(String vorbisComments) {
        long vendorLength = getVorbisCommentVendorLength(vorbisComments);
        return Utils.read32BitIntegerBE(vorbisComments.substring(4 + (int)vendorLength));
    }

    public HashMap<String, String> getVorbisCommentsListed(String vorbisComments) {
        System.out.println("Length: " + vorbisComments.length());
        HashMap<String, String> result = new HashMap<>();
        long vendorVectorLength = getVorbisCommentVendorLength(vorbisComments);
        long vorbisCommentCount = getVorbisCommentListLength(vorbisComments);
        String plainComments = vorbisComments.substring(8 + (int)vendorVectorLength);
        for (int i = 0; i < vorbisCommentCount; i++) {
            long commentLength = Utils.read32BitIntegerBE(plainComments);
            String key = plainComments.substring(4, plainComments.indexOf('='));
            String value = plainComments.substring(plainComments.indexOf('=') + 1, 4 + (int)commentLength);
            plainComments = plainComments.substring(4 + (int)commentLength);
            if (!result.containsKey(key)) {
                result.put(key, value);
            }
        }

        return result;
    }

    private boolean fileIsFlacFile(BufferedInputStream bIS) throws IOException {
        if (bIS == null) {
            return false;
        }
        int[] byteArray = new int[BLOCK_SIZE];
        read32BitInteger(byteArray);
        if (byteArray[0] == 'f'
                && byteArray[1] == 'L'
                && byteArray[2] == 'a'
                && byteArray[3] == 'C') {
            return true;
        } else {
            return false;
        }

    }

    private static class Log {

        public Log() {
        }

        public static void i(String s1, String s2) {
            System.out.println(s1 + ": " + s2);
        }

        public static void w(String s1, String s2) {
            System.out.println(s1 + ": " + s2);
        }

        public static void e(String s1, String s2) {
            System.out.println(s1 + ": " + s2);
        }

        public static void d(String s1, String s2) {
            System.out.println(s1 + ": " + s2);
        }
    }
}
