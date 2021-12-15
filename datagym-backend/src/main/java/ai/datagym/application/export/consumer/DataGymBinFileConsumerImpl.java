package ai.datagym.application.export.consumer;

import com.eforce21.lib.exception.GenericException;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DataGymBinFileConsumerImpl implements DataGymBinFileConsumer {
    private HttpServletResponse response;
    private boolean downloadFile;

    public DataGymBinFileConsumerImpl(HttpServletResponse response, boolean downloadFile) {
        this.response = response;
        this.downloadFile = downloadFile;
    }

    @Override
    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public void onMetaData(String filename, String mime) {
        response.setHeader("Content-Type", mime);
        if (this.downloadFile) {
            try {
                filename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
                throw new GenericException("file_export", null, null);
            }
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        }
    }

    public void onStream(InputStream is) {
        try {
            StreamUtils.copy(is, response.getOutputStream());
        } catch (IOException e) {
            throw new GenericException("file_export", null, null);
        }
    }
}
