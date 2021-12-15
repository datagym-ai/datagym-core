package com.eforce21.lib.bin.file.service;

import com.eforce21.lib.bin.data.dao.BinDataRepo;
import com.eforce21.lib.bin.file.dao.BinFileRepository;
import com.eforce21.lib.bin.file.entity.BinFileEntity;
import com.eforce21.lib.bin.file.model.BinFileConsumer;
import com.eforce21.lib.bin.file.model.BinFileUpdate;
import com.eforce21.lib.exception.SystemException;
import com.eforce21.lib.exception.ValidationException;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Consumer;


public class BinFileServiceImpl implements BinFileService {

    private static final Logger L = LoggerFactory.getLogger(BinFileServiceImpl.class);

    @Autowired
    private BinDataRepo binRepo;

    @Autowired
    private BinFileRepository binFileRepository;

    private Tika tika = new Tika();

    @Override
    public BinFileEntity create(String filename, InputStream is, Collection<String> allowedMimeTypesAntPatterns) {
        BinFileValidator.validate(filename, is);

        String dataId = null;
        long dataSize = 0;
        TikaStreamConsumer tsc = new TikaStreamConsumer();

        // Write file, check size and analyse mime type via tika.
        try {
            dataId = binRepo.write(is);
            dataSize = binRepo.size(dataId);
            binRepo.read(dataId, tsc);
        } catch (IOException e) {
            if (dataId != null) {
                try {
                    binRepo.delete(dataId);
                } catch (IOException e2) { /* Dont care about the unreferenced binary chillin around. */}
            }
            throw new SystemException("Failed to store/analyse binary. " + e.getMessage(), e);
        }

        // Validate mime type
        try {
            BinFileValidator.validateMimes(tsc.getMimeType(), allowedMimeTypesAntPatterns);
        } catch (ValidationException ve) {
            try {
                binRepo.delete(dataId);
            } catch (IOException e) { /* Dont care about the unreferenced binary chillin around. */}
            throw ve;
        }

        // Create entity
        BinFileEntity f = new BinFileEntity();
        f.setDataId(dataId);
        f.setSize(dataSize);
        f.setName(filename);
        f.setMime(tsc.getMimeType());
        f.setTsCreate(System.currentTimeMillis());
        f.setCover(false);

        return binFileRepository.save(f);
    }

    @Override
    public void consume(BinFileEntity binFile, BinFileConsumer consumer) {
        consumer.onMetaData(binFile.getName(), binFile.getMime(), binFile.getSize());
        try {
            binRepo.read(binFile.getDataId(), consumer::onStream);
        } catch (IOException e) {
            throw new SystemException("Failed to consume binary. " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(BinFileEntity binFile) {
        binFileRepository.delete(binFile);
        try {
            binRepo.delete(binFile.getDataId());
        } catch (IOException e) { /* Dont care about the unreferenced binary chillin around. Maybe it already has been deleted. */}
    }

    @Override
    public void update(BinFileEntity binFile, BinFileUpdate update) {
        BinFileValidator.validate(update);
        binFile.setName(update.getName());
        binFile.setCover(update.isCover());
    }

    private class TikaStreamConsumer implements Consumer<InputStream> {
        private String mimeType;

        public String getMimeType() {
            return mimeType;
        }

        @Override
        public void accept(InputStream inputStream) {
            try {
                mimeType = tika.detect(inputStream);
            } catch (IOException e) {
                L.warn("Tika file detection failed. {}.", e.getMessage(), e);
            }
        }
    }
}
