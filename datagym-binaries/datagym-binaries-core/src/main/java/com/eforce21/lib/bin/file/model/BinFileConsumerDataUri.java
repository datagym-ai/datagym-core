package com.eforce21.lib.bin.file.model;

import com.eforce21.lib.exception.NotFoundException;
import org.apache.tika.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class BinFileConsumerDataUri implements BinFileConsumer {
    private String mime;
    private String filename;
    private StringBuilder dataUri = new StringBuilder();

    @Override
    public void onMetaData(String filename, String mime, long size) {
        this.mime = mime;
        this.filename = filename;
    }

    @Override
    public void onStream(InputStream is) {
        try {
            dataUri.append("data:");
            dataUri.append(this.mime); // append mime-type
            dataUri.append(";base64,"); // add base64 encoding
            dataUri.append(Base64.getEncoder().encodeToString(IOUtils.toByteArray(is)));
        } catch (IOException e) {
            throw new NotFoundException("data", "filename", "" + filename);
        }
    }

    public String getDataUri() {
        return dataUri.toString();
    }
}
