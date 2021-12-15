package com.eforce21.lib.bin.data.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * {@link BinDataRepo} storing all files flat in a local disk path.
 */
public class BinDataRepoFs implements BinDataRepo {

    private Path storagePath;

    public BinDataRepoFs(Path storagePath) {
        this.storagePath = storagePath;

        if (Files.notExists(storagePath) || !Files.isDirectory(storagePath)) {
            throw new IllegalArgumentException("BinDataRepoFs init failed. Path not existing/directory/writeable: " + storagePath);
        }
    }

    @Override
    public String write(InputStream in) throws IOException {
        String id = UUID.randomUUID().toString();
        Files.copy(in, idToPath(id));
        return id;
    }

    @Override
    public InputStream read(String id) throws IOException {
        return new FileInputStream(idToPath(id).toFile());
    }

    @Override
    public void read(String id, Consumer<InputStream> consumer) throws IOException {
        consumer.accept(new FileInputStream(idToPath(id).toFile()));
    }

    @Override
    public void delete(String id) throws IOException {
        Files.delete(idToPath(id));
    }

    @Override
    public long size(String id) throws IOException {
        return Files.size(idToPath(id));
    }

    private Path idToPath(String id) {
        return storagePath.resolve(id);
    }

}
