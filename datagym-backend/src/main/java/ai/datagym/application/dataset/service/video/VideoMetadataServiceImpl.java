package ai.datagym.application.dataset.service.video;

import ai.datagym.application.dataset.models.video.ExtractedVideoMetadataTO;
import com.eforce21.lib.exception.SystemException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class VideoMetadataServiceImpl implements VideoMetadataService {

    @Value("${ffprobe.path}")
    private String ffprobePath;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() {
        if (Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            return;
        }
        if (!Files.isExecutable(Paths.get(ffprobePath))) {
            throw new SystemException("Invalid ffprobe path!", null);
        }

        checkFFProbeFunctionality();
    }

    /**
     * Check version to detect a invalid software-path, unexpected exit code or
     * other problems that later will interrupt the video information extraction.
     */
    private void checkFFProbeFunctionality() {
        String[] arguments = {ffprobePath, "-show_program_version"};
        try {
            Process exec = Runtime.getRuntime().exec(arguments);
            int exitCode = exec.waitFor();
            if (exitCode != 0) {
                throw new SystemException(
                        "Error by checking ffprobe version! Expected exit code: 0 " +
                                "- Returned Exit code: " + exitCode, null);
            }
        } catch (InterruptedException | IOException e) {
            throw new SystemException("Error by checking ffprobe version! ", e);
        }
    }

    @Override
    public ExtractedVideoMetadataTO fetchMetaDataFromUrl(String uri) throws IOException {
        String[] arguments = {ffprobePath, "-loglevel", "error",
                // Show information about each media stream
                "-show_streams",
                // Print the output in json format
                "-print_format", "json",
                // Only choose the video stream
                "-select_streams", "v:0",
                // Only show specific stream entries
                "-show_entries", "stream=width,height,nb_frames,duration,codec_name,r_frame_rate",
                // Show specific container infos
                "-show_format_entry", "format_name,size",
                uri};


        Process exec = Runtime.getRuntime().exec(arguments);

        BufferedReader lineReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));

        // Pare output from ffprobe
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ExtractedVideoMetadataTO.class, new FFProbeDeserializer());
        objectMapper.registerModule(module);

        return objectMapper.readValue(lineReader,
                                      ExtractedVideoMetadataTO.class);

    }
}
