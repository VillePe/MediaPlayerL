package parsers.Misc;

import mediafileparsers.Utils;
import parsers.flac.FlacParser;
import parsers.flac.MetadataBlockCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by vp on 22.6.2017.
 */
public class VorbisComments {

    private LinkedHashMap<String, ArrayList<String>> comments;

    public VorbisComments(FlacParser parser) {
        MetadataBlockCollection collection = new MetadataBlockCollection(parser);
        this.comments = getVorbisCommentsListed(collection.getVorbisCommentsBlock().getDataAsString());
    }

    public VorbisComments(String vorbisComments) {
        this.comments = getVorbisCommentsListed(vorbisComments);
    }

    public VorbisComments(LinkedHashMap<String, ArrayList<String>> vorbisComments) {
        this.comments = vorbisComments;
    }

    public byte[] getAsByteArray() {
        int count = 0;
        ArrayList<SingleVorbisComment> commentsList = new ArrayList<SingleVorbisComment>();
        int arraySize = 0;
        for (String key : this.comments.keySet()) {
            for (String value : this.comments.get(key)) {
                commentsList.add(new SingleVorbisComment(value, key));
                // 4 bytes for size in 32 bit integer and 1 byte for the equals (=) sign
                arraySize += 5 + key.length() + value.length();
            }
        }

        byte[] result = new byte[arraySize];
        for (SingleVorbisComment singleVorbisComment : commentsList) {
            byte[] commentLength = Utils.create32BitByteArrayLE(singleVorbisComment.getLength());
            for (int i = 0; i < 4; i++) {
                result[count] = commentLength[i];
                count++;
            }
            for (int i = 0; i < singleVorbisComment.getLength(); i++) {
                if (i == singleVorbisComment.getKey().length()) {
                   result[count] = '=';
                   count++;
                } else if (i < singleVorbisComment.getKey().length()) {
                    result[count] = (byte)singleVorbisComment.getKey().charAt(i);
                    count++;
                } else {
                    result[count] = (byte)singleVorbisComment.getValue().charAt(i - singleVorbisComment.getKey().length() - 1);
                    count++;
                }

            }
        }

        return result;

    }

    public static LinkedHashMap<String, ArrayList<String>> getVorbisCommentsListed(String vorbisComments) {
        System.out.println("Length: " + vorbisComments.length());
        LinkedHashMap<String, ArrayList<String>> result = new LinkedHashMap<>();
        long vendorVectorLength = getVorbisCommentVendorLength(vorbisComments);
        long vorbisCommentCount = getVorbisCommentListLength(vorbisComments);
        String plainComments = vorbisComments.substring(8 + (int)vendorVectorLength);
        for (int i = 0; i < vorbisCommentCount; i++) {
            long commentLength = Utils.read32BitIntegerLE(plainComments);
            String key = plainComments.substring(4, plainComments.indexOf('='));
            System.out.println(key + ": " + commentLength);
            String value = plainComments.substring(plainComments.indexOf('=') + 1, 4 + (int)commentLength);
            System.out.println(value);
            System.out.println();
            plainComments = plainComments.substring(4 + (int)commentLength);
            if (!result.containsKey(key)) {
                result.put(key, new ArrayList<>());
                result.get(key).add(value);
            } else {
                result.get(key).add(value);
            }
        }

        return result;
    }

    // For Vorbis comments documentation see: https://www.xiph.org/vorbis/doc/v-comment.html
    public static long getVorbisCommentVendorLength(String vorbisComments) {
        return Utils.read32BitIntegerLE(vorbisComments);
    }

    public static long getVorbisCommentListLength(String vorbisComments) {
        long vendorLength = getVorbisCommentVendorLength(vorbisComments);
        return Utils.read32BitIntegerLE(vorbisComments.substring(4 + (int)vendorLength));
    }

    private class SingleVorbisComment {
        private int length;
        private String value;
        private String key;

        public SingleVorbisComment(String value, String key) {
            this.length = value.length() + key.length() + 1;
            this.value = value;
            this.key = key;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

}
