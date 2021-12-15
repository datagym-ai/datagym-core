package ai.datagym.application.dataset.service.video;

import ai.datagym.application.dataset.models.video.ExtractedVideoMetadataTO;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class FFProbeDeserializer extends StdDeserializer<ExtractedVideoMetadataTO> {

    public FFProbeDeserializer() {
        this(null);
    }

    public FFProbeDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ExtractedVideoMetadataTO deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {

        JsonNode node = p.getCodec().readTree(p);

        JsonNode streams = node.get("streams").elements().next();
        JsonNode format = node.get("format");

        String codecName = streams.get("codec_name").asText();
        String rFrameRate = streams.get("r_frame_rate").asText();
        Integer width = streams.get("width").asInt();
        Integer height = streams.get("height").asInt();
        Long frames = streams.get("nb_frames").asLong();
        Double duration = streams.get("duration").asDouble();
        String formatName = format.get("format_name").asText();
        Long size = format.get("size").asLong();

        return new ExtractedVideoMetadataTO(height, width, frames, duration, codecName, rFrameRate, formatName, size);
    }
}
