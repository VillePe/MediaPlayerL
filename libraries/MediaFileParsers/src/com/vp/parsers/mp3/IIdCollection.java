package com.vp.parsers.mp3;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Created by Ville on 8.7.2017.
 */
public interface IIdCollection {

    IIdFrame getFrame(String id);
    IIdHeader getFileHeader();
    boolean isRightFileFormat();
    String getUnsyncedLyrics();
    ByteArrayInputStream getAttachedPicture();
}
