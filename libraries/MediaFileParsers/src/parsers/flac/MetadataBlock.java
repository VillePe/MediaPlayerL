package parsers.flac;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ville on 21.6.2017.
 */
public class MetadataBlock {

    public static HashMap<Integer, String> blockHeaders;

    private boolean lastBlock;
    private int blockType;
    private long length;
    private int index;
    ArrayList<Byte> data = new ArrayList<>();

    public MetadataBlock(boolean lastBlock, int blockType, long length, int index) {
        this.lastBlock = lastBlock;
        this.blockType = blockType;
        this.length = length;
        this.index = index;
    }

    public MetadataBlock(boolean lastBlock, int blockType, ArrayList<Byte> data) {
        this.lastBlock = lastBlock;
        this.blockType = blockType;
        this.data = data;
        this.length = data.size();
    }

    public void addData(Byte b) {
        this.data.add(b);
    }

    public ArrayList<Byte> getData() {
        return data;
    }

    public byte[] getHeaderAsBytes() {
        byte[] result = new byte[4];

        // Insert the block type into the array (digits 1-7)
        result[0] = (byte)blockType;
        // Insert the length of the block into the array (digits 8-23)
        for (int i = 0; i < 3; i++) {
            result[3 - i] = (byte)(length >> (i * 8));
        }
        // If the block is last, convert the first digit (0) to 1 with a mask
        if (isLastBlock()) {
            result[0] |= 0x80;
        }

        return result;
    }

    public String getDataAsString() {
        StringBuilder sb = new StringBuilder();
        for (Byte aData : data) {
            sb.append((char) aData.byteValue());
        }
        return sb.toString();
    }

    public void setData(ArrayList<Byte> data) {
        this.data = data;
        this.length = data.size();
    }

    public boolean isLastBlock() {
        return lastBlock;
    }

    public void setLastBlock(boolean lastBlock) {
        this.lastBlock = lastBlock;
    }

    public int getBlockType() {
        return blockType;
    }

    public void setBlockType(int blockType) {
        this.blockType = blockType;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    static {
        blockHeaders = new HashMap<>();
        blockHeaders.put(0, "STREAMINFO");
        blockHeaders.put(1, "PADDING");
        blockHeaders.put(2, "APPLICATION");
        blockHeaders.put(3, "SEEKTABLE");
        blockHeaders.put(4, "VORBIS_COMMENT");
        blockHeaders.put(5, "CUESHEET");
        blockHeaders.put(6, "PICTURE");
    }
}