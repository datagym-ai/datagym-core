package ai.datagym.application.media.common;

import com.eforce21.lib.bin.file.model.BinFileConsumer;
import com.eforce21.lib.exception.SystemException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Consumer that reads an image and returns an {@link BufferedImage}
 * For supported image types take a look at
 * <a href="https://github.com/haraldk/TwelveMonkeys">https://github.com/haraldk/TwelveMonkeys</a>
 */
public class BufferedImageConsumer implements BinFileConsumer {

    private BufferedImage bufferedImage;

    @Override
    public void onMetaData(String filename, String mime, long size) {

    }

    @Override
    public void onStream(InputStream is) {
        try {
            this.bufferedImage = ImageIO.read(is);
        } catch (IOException e) {
            throw new SystemException("Failed to read image " + e.getMessage(), e);
        }
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
}

