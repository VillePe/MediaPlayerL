package com.vp.parsers.Misc;

import com.vp.mediafileparsers.Utils;
import com.vp.parsers.flac.FlacParser;
import com.vp.parsers.flac.MetadataBlockCollection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by vp on 22.6.2017.
 */
public class VorbisComments {

    // For Vorbis comments documentation see: https://www.xiph.org/vorbis/doc/v-comment.html

    private LinkedHashMap<String, ArrayList<String>> comments;
    private String vendor;

    public VorbisComments(FlacParser parser) {
        MetadataBlockCollection collection = new MetadataBlockCollection(parser);
        String vorbisComments = collection.getVorbisCommentsBlock().getDataAsString();
        this.comments = getVorbisCommentsListed(vorbisComments);
        setVorbisCommentVendor(vorbisComments);
    }

    public VorbisComments(String vorbisComments, String vendor) {
        this.comments = getVorbisCommentsListed(vorbisComments);
        this.vendor = vendor;
    }

    public VorbisComments(LinkedHashMap<String, ArrayList<String>> vorbisComments, String vendor) {
        this.comments = vorbisComments;
        this.vendor = vendor;
    }

    public ArrayList<Byte> getAsByteArrayList() {
        ArrayList<Byte> result = new ArrayList<>();
        ArrayList<SingleVorbisComment> vorbisComments = getVorbisCommentsAsSingleCommentArray();

        // Write the info before comments
        byte[] vendorLength = Utils.create32BitByteArrayLE(vendor.length());
        byte[] commentsCount = Utils.create32BitByteArrayLE(vorbisComments.size());

        for (int i = 0; i < 4; i++) {
            result.add(vendorLength[i]);
        }
        for (int i = 0; i < vendor.length(); i++) {
            result.add((byte)vendor.charAt(i));
        }
        for (int i = 0; i < 4; i++) {
            result.add(commentsCount[i]);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("VALUES:\n");

        // Write the comments
        for (SingleVorbisComment singleVorbisComment : vorbisComments) {
            byte[] commentLength = Utils.create32BitByteArrayLE(singleVorbisComment.getLength());
            for (int i = 0; i < 4; i++) {
                result.add(commentLength[i]);
            }
            for (int i = 0; i < singleVorbisComment.getLength(); i++) {
                if (i == singleVorbisComment.getKey().length()) {
                    result.add((byte)'=');
                    sb.append("\n\n");
                } else if (i < singleVorbisComment.getKey().length()) {
                    result.add((byte)singleVorbisComment.getKey().charAt(i));
                } else {
                    result.add((byte)singleVorbisComment.getValue().charAt(i - singleVorbisComment.getKey().length() - 1));
                    sb.append(singleVorbisComment.getValue().charAt(i - singleVorbisComment.getKey().length() - 1));
                }
            }
        }
        return result;
    }

    public byte[] getAsByteArray() {
        int count = 0;
        ArrayList<SingleVorbisComment> commentsList = new ArrayList<SingleVorbisComment>();
        int byteArraySize = 0;

        // Vendor length (int32) and comment list length (int32)
        byteArraySize += 8;
        // Vendors length
        byteArraySize += vendor.length();

        for (String key : this.comments.keySet()) {
            for (String value : this.comments.get(key)) {
                commentsList.add(new SingleVorbisComment(value, key));
                // 4 bytes for size in 32 bit integer and 1 byte for the equals (=) sign
                byteArraySize += 5 + key.length() + value.length();
            }
        }

        byte[] result = new byte[byteArraySize];

        // Write the info before comments
        byte[] vendorLength = Utils.create32BitByteArrayLE(vendor.length());
        byte[] commentsCount = Utils.create32BitByteArrayLE(commentsList.size());

        for (int i = 0; i < 4; i++) {
            result[count] = vendorLength[i];
            count++;
        }
        for (int i = 0; i < vendor.length(); i++) {
            result[count] = (byte)vendor.charAt(i);
            count++;
        }
        for (int i = 0; i < 4; i++) {
            result[count] = commentsCount[i];
            count++;
        }

        // Write the comments
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

    public ArrayList<SingleVorbisComment> getVorbisCommentsAsSingleCommentArray() {
        ArrayList<SingleVorbisComment> commentsList = new ArrayList<>();
        for (String key : this.comments.keySet()) {
            for (String value : this.comments.get(key)) {
                commentsList.add(new SingleVorbisComment(value, key));
            }
        }
        return commentsList;
    }

    public void setCommentsWithSingleCommentItems(ArrayList<SingleVorbisComment> singleVorbisComments) {
        this.comments.clear();
        for (SingleVorbisComment comment : singleVorbisComments) {
            if (!comments.containsKey(comment.getKey())) {
                comments.put(comment.getKey(), new ArrayList<String>());
            }
            comments.get(comment.getKey()).add(comment.getValue());
        }
    }

    public void setValue(String key, String value) {
        ArrayList<SingleVorbisComment> vorbisComments = getVorbisCommentsAsSingleCommentArray();
        boolean lyricsCommentFound = false;
        for (SingleVorbisComment comment : vorbisComments) {
            if (comment.getKey().equals(key)) {
                comment.setValue(value);
                lyricsCommentFound = true;
                break;
            }
        }
        if (!lyricsCommentFound) {
            SingleVorbisComment lyricsComment = new SingleVorbisComment(value, key);
        }
        setCommentsWithSingleCommentItems(vorbisComments);
    }

    public void setLyrics(String lyrics) {
        setValue("LYRICS", lyrics);
    }

    public static LinkedHashMap<String, ArrayList<String>> getVorbisCommentsListed(FlacParser parser) {
        MetadataBlockCollection collection = new MetadataBlockCollection(parser);
        String vorbisComments = collection.getVorbisCommentsBlock().getDataAsString();
        long vendorVectorLength = getVorbisCommentVendorLength(vorbisComments);
        long vorbisCommentCount = getVorbisCommentListLength(vorbisComments);
        return getVorbisCommentsListed(vorbisComments, vendorVectorLength, vorbisCommentCount);
    }

    public static LinkedHashMap<String, ArrayList<String>> getVorbisCommentsListed(String vorbisComments, long vendorVectorLength, long vorbisCommentCount) {
        LinkedHashMap<String, ArrayList<String>> result = new LinkedHashMap<>();
        String plainComments = vorbisComments.substring(8 + (int)vendorVectorLength);
        for (int i = 0; i < vorbisCommentCount; i++) {
            long commentLength = Utils.read32BitIntegerLE(plainComments);
            String key = plainComments.substring(4, plainComments.indexOf('='));
            String value = plainComments.substring(plainComments.indexOf('=') + 1, 4 + (int)commentLength);
            plainComments = plainComments.substring(4 + (int)commentLength);
            if (!result.containsKey(key)) {
                result.put(key, new ArrayList<String>());
                result.get(key).add(value);
            } else {
                result.get(key).add(value);
            }
        }
        return result;
    }

    public LinkedHashMap<String, ArrayList<String>> getVorbisCommentsListed(String vorbisComments) {
        long vendorVectorLength = getVorbisCommentVendorLength(vorbisComments);
        long vorbisCommentCount = getVorbisCommentListLength(vorbisComments);
        return getVorbisCommentsListed(vorbisComments, vendorVectorLength, vorbisCommentCount);
    }

    private void setVorbisCommentVendor(String vorbisComments) {
        int length = (int)getVorbisCommentVendorLength(vorbisComments);
        this.vendor = vorbisComments.substring(4, length);
    }

    public static String getVorbisCommentVendorString(String vorbisComments) {
        long length = getVorbisCommentVendorLength(vorbisComments);
        return vorbisComments.substring(4, (int)length);
    }

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
