package com.eforce21.lib.bin.file.service;

import com.eforce21.lib.bin.file.entity.BinFileEntity;
import com.eforce21.lib.bin.file.model.BinFileConsumer;
import com.eforce21.lib.bin.file.model.BinFileUpdate;

import java.io.InputStream;
import java.util.Collection;

/**
 * Service for binary and metadata handling.
 * Use {@link BinFileEntity} to reference "file handles" via JPA / FKs.
 */
public interface BinFileService {

    /**
     * Create/Upload a new binary.
     *
     * @param filename
     * @param is
     * @param allowedMimeTypesAntPatterns A bunch of ant-path-patterns as mime type whitelist or null to allow all kinds of files.
     * @return
     */
    BinFileEntity create(String filename, InputStream is, Collection<String> allowedMimeTypesAntPatterns);

    /**
     * Read binary via consumer.
     *
     * @param binFile
     * @param consumer
     */
    void consume(BinFileEntity binFile, BinFileConsumer consumer);

    /**
     * Delete binary.
     *
     * @param binFile
     */
    void delete(BinFileEntity binFile);

    /**
     * Update modifiable attributes of a file.
     *
     * @param binFile
     * @param update
     */
    void update(BinFileEntity binFile, BinFileUpdate update);
}
