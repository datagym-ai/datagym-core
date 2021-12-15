package com.eforce21.lib.bin.file.model;

import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Consume binary and write to HttpResponse including headers.
 */
public class BinFileConsumerHttp implements BinFileConsumer {

    private HttpServletResponse response;
    private boolean downloadFile;

    public BinFileConsumerHttp(HttpServletResponse response) {
        this.response = response;
    }

    public BinFileConsumerHttp(HttpServletResponse response, boolean downloadFile) {
        this.response = response;
        this.downloadFile = downloadFile;
    }


    public void onMetaData(String filename, String mime, long size) {
        response.setContentLengthLong(size);
        response.setHeader("Content-Type", mime);
        if (this.downloadFile) {
            try {
                filename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
                // Kann nicht passieren
            }
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        }
    }

    public void onStream(InputStream is) {
        try {
            StreamUtils.copy(is, response.getOutputStream());
        } catch (IOException e) {
            // TODO handle exception property, i.e. silent handle on client driven exceptions.
            e.printStackTrace();
        }
    }
}
