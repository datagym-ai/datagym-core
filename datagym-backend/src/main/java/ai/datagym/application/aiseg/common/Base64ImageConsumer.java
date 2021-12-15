package ai.datagym.application.aiseg.common;

import com.eforce21.lib.bin.file.model.BinFileConsumer;
import com.eforce21.lib.exception.NotFoundException;
import org.apache.tika.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class Base64ImageConsumer implements BinFileConsumer {

    private String filename;
    private String base64Image;

    @Override
    public void onMetaData(String filename, String mime, long size) {
        this.filename = filename;
    }

    @Override
    public void onStream(InputStream is) {
        try {
            this.base64Image = Base64.getEncoder().encodeToString(IOUtils.toByteArray(is));
        } catch (IOException e) {
            throw new NotFoundException("data", "filename", "" + filename);
        }

    }

    public String getBase64Image() {
        return base64Image;
    }

}
