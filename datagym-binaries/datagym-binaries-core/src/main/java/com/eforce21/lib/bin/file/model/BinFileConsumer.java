package com.eforce21.lib.bin.file.model;

import java.io.InputStream;

/**
 * Consumer for binaries and its metadata.
 * Note that MetaData must/is always called first to allow writing headers.
 */
public interface BinFileConsumer {

    void onMetaData(String filename, String mime, long size);

    void onStream(InputStream is);
}
