package com.vp.parsers.mp3;

/**
 * Created by Ville on 8.7.2017.
 */
public interface IIdFrame {

    String getFrameId();
    int getSize();
    boolean[] getFlags();
    int[] getData();


}
