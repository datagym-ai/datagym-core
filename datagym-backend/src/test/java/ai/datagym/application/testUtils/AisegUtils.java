package ai.datagym.application.testUtils;

import ai.datagym.application.aiseg.model.aiseg.AiSegCalculate;
import ai.datagym.application.aiseg.model.aiseg.AiSegResponse;

import java.util.ArrayList;

import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;

public class AisegUtils {

    public static AiSegCalculate createTestAiSegCalculate() {
        return new AiSegCalculate() {{
            setImageId(IMAGE_ID);
            setNegativePoints(new ArrayList<>());
            setPositivePoints(new ArrayList<>());
            setNumPoints(10);
            setEnvironment("dev");
        }};
    }

    public static AiSegResponse createTestAiSegResponse() {
        return new AiSegResponse() {{
            setImageId(IMAGE_ID);
            setResult(new ArrayList<>());
        }};
    }
}
