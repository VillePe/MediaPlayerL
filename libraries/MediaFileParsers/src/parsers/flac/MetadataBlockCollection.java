package parsers.flac;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Created by Ville on 21.6.2017.
 */
public class MetadataBlockCollection implements Iterable<MetadataBlock> {

    private ArrayList<MetadataBlock> blocks;

    public MetadataBlockCollection(ArrayList<MetadataBlock> blocks) {
        this.blocks = blocks;
    }

    public MetadataBlockCollection(FlacParser parser) {
        this(parser.getMetadataBlocks());
    }

    public int size() {
        return blocks.size();
    }

    public void add(MetadataBlock block) {
        blocks.add(block);
    }

    public MetadataBlock get(int i) {
        return blocks.get(i);
    }

    public long getTotalSize() {
        int headerSize = 4;
        int totalSize = 0;
        for (int i = 0; i < blocks.size(); i++) {
            totalSize += headerSize;
            totalSize += blocks.get(i).getLength();
        }
        return totalSize;
    }

    public ArrayList<MetadataBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(ArrayList<MetadataBlock> blocks) {
        this.blocks = blocks;
    }

    public MetadataBlock getStreamInfoBlock() {
        for (MetadataBlock block : blocks) {
            if (block.getBlockType() == 0) {
                return block;
            }
        }
        return null;
    }

    public MetadataBlock getVorbisCommentsBlock() {
        for (MetadataBlock block : blocks) {
            if (block.getBlockType() == 4) {
                return block;
            }
        }
        return null;
    }

    public MetadataBlock getPictureBlock() {
        for (MetadataBlock block : blocks) {
            if (block.getBlockType() == 6) {
                return block;
            }
        }
        return null;
    }

    @Override
    public Iterator<MetadataBlock> iterator() {
        return blocks.iterator();
    }

    @Override
    public void forEach(Consumer<? super MetadataBlock> consumer) {
        for (MetadataBlock block : blocks) {
            consumer.accept(block);
        }
    }

    @Override
    public Spliterator<MetadataBlock> spliterator() {
        return blocks.spliterator();
    }
}
