package com.vp.parsers.flac;

import com.vp.mediafileparsers.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Ville on 24.10.2016.
 */
public class FlacParser {

    // For flac format documentation see https://xiph.org/flac/format.html

    private final int BLOCK_SIZE = 4;
    private File file;
    private FileInputStream fInput;
    private BufferedInputStream bInput;
    private boolean isValidFlacFormat = true;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public FlacParser(File file) {
        this.file = file;
    }

    public void reset() throws FileNotFoundException {
        fInput = new FileInputStream(file);
        bInput = new BufferedInputStream(fInput);
    }

    public String getLyricsFromFile() throws IOException {
        reset();
        if (bInput == null) {
            return "No lyrics";
        }

        String lyrics = "";

        MetadataBlockCollection collection = new MetadataBlockCollection(getMetadataBlocks());

        String vorbisComments = collection.getVorbisCommentsBlock().getDataAsString();

        lyrics = parseLyricsFromVorbisCommentString(vorbisComments);

        return lyrics;
    }

    public boolean close() throws IOException {
        if (bInput == null) {
            return false;
        }

        boolean exceptionThrown = false;
        if (fInput != null) {
            try {
                fInput.close();
            } catch (IOException e) {
                exceptionThrown = true;
                throw e;
            }

        }
        if (bInput != null) {
            try {
                bInput.close();
            } catch (IOException e) {
                exceptionThrown = true;
                throw e;
            }
        }
        return !exceptionThrown;
    }

    public ArrayList<MetadataBlock> getMetadataBlocks() throws IOException {
        ArrayList<MetadataBlock> result = new ArrayList<>();
        try {
            reset();
            if (!fileIsFlacFile(bInput)) {
                throw new IOException("File was in wrong format!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }

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
                    block.addData((byte) bInput.read());
                }
                i++;
            }
        } while (integer != -1 && !isLastBlock);

        return result;
    }

    public ByteArrayInputStream getTrackPicture(MetadataBlock block) {
        int offset = 0;
        long pictureType = Utils.read32BitIntegerBE(block.getData());
        offset += 4;
        long mimeLength = Utils.read32BitIntegerBE(block.getData(), offset);
        offset += 4;

        for (int i = 0; i < mimeLength; i++) {
        }

        offset += mimeLength;
        long descriptionLength = Utils.read32BitIntegerBE(block.getData(), offset);
        offset += 4;

        for (int i = 0; i < descriptionLength; i++) {
        }
        offset += descriptionLength;

        long imageWidth = Utils.read32BitIntegerBE(block.getData(), offset);
        offset += 4;

        long imageHeight = Utils.read32BitIntegerBE(block.getData(), offset);
        offset += 4;

        long imageColorDepth = Utils.read32BitIntegerBE(block.getData(), offset);
        offset += 4;

        long imageColorsUsed = Utils.read32BitIntegerBE(block.getData(), offset);
        offset += 4;

        long imageDataLength = Utils.read32BitIntegerBE(block.getData(), offset);
        offset += 4;

        byte[] byteArray = Utils.arrayListToByteArray(block.getData(), offset, (int)imageDataLength);
        if (byteArray != null) {
            return new ByteArrayInputStream(byteArray);
        } else {
            return null;
        }
    }

    // Reads a 32 bit integer from the file
    private long read32BitInteger(int[] byteArray) throws IOException {
        return Utils.read32BitIntegerBE(bInput, byteArray);
    }

    // Parses the lyrics from vorbis comment string
    private String parseLyricsFromVorbisCommentString(String vorbisComments) {
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
        return lyrics.toString();
    }

    private boolean fileIsFlacFile(BufferedInputStream bIS) throws IOException {
        if (bIS == null) {
            return false;
        }
        int[] byteArray = new int[BLOCK_SIZE];
        read32BitInteger(byteArray);
        return byteArray[0] == 'f'
                && byteArray[1] == 'L'
                && byteArray[2] == 'a'
                && byteArray[3] == 'C';

    }
}