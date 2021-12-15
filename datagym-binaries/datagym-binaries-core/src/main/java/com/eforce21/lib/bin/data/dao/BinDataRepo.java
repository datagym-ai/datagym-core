package com.eforce21.lib.bin.data.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Repository to write or read binary data in a streaming way.
 * May be backed by the filesystem, a database or whatever, depending on chosen implementation.
 */
public interface BinDataRepo {

    /**
     * Store data given by the stream.
     *
     * @param in
     * @return Binary id.
     * @throws IOException
     */
    String write(InputStream in) throws IOException;

    /**
     * Get data as InputStream to read from.
     * Note that some backends cannot pass streams out of their scope (transaction, whatever)
     * and might throw an {@link UnsupportedOperationException}.
     * Always prefer the consumer-version of read instead.
     *
     * @param id Binary id.
     * @return
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    InputStream read(String id) throws IOException, UnsupportedOperationException;

    /**
     * Get data and pump to given consumer.
     *
     * @param id       Binary id.
     * @param consumer
     * @throws IOException
     */
    void read(String id, Consumer<InputStream> consumer) throws IOException;

    /**
     * Delete a binary.
     *
     * @param id Binary id.
     * @throws IOException
     */
    void delete(String id) throws IOException;

    /**
     * Get size of a binary.
     *
     * @param id
     * @return
     * @throws IOException
     */
    long size(String id) throws IOException;

}
