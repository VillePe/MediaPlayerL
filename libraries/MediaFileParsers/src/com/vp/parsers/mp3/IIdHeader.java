package com.vp.parsers.mp3;

/**
 * Created by Ville on 8.7.2017.
 */
public interface IIdHeader {String getFileIdentifier();

    int getVersionMajor();

    int getVersionMinor();

    boolean[] getFlags();

    long getSize();
}
