package ai.datagym.application.aiseg.controller;

import ai.datagym.application.aiseg.model.aiseg.AiSegCalculate;
import ai.datagym.application.aiseg.model.aiseg.AiSegResponse;
import ai.datagym.application.aiseg.service.AiSegService;
import ai.datagym.application.errorHandling.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/aiseg")
public class AiSegController {

    private final Optional<AiSegService> aiSegService;

    public AiSegController(@Autowired(required = false) Optional<AiSegService> aiSegService) {
        this.aiSegService = aiSegService;
    }

    @PostMapping("/prepare/{mediaId}")
    public void prepareImage(@PathVariable("mediaId") String mediaId) {
        this.aiSegService.orElseThrow(() -> new ServiceUnavailableException("ai")).prepare(mediaId, null, null);
    }

    @PostMapping("/prepare/{mediaId}/{frameNumber}")
    public void prepareImage(@PathVariable("mediaId") String mediaId,
                             @PathVariable(value = "frameNumber", required = false) Integer frameNumber,
                             @RequestBody(required = false) String dataUri) {
        this.aiSegService.orElseThrow(() -> new ServiceUnavailableException("ai")).prepare(mediaId, frameNumber, dataUri);
    }


    @PostMapping("/calculate")
    public AiSegResponse calculateImage(@RequestBody AiSegCalculate aiSegCalculate) {
        return this.aiSegService.orElseThrow(() -> new ServiceUnavailableException("ai")).calculate(aiSegCalculate);
    }

    @DeleteMapping("/finish/{imageId}")
    public void finishImage(@PathVariable("imageId") String imageId) {
        this.aiSegService.orElseThrow(() -> new ServiceUnavailableException("ai")).finish(imageId);
    }

    @DeleteMapping("/finishUserSession/{userSessionUUID}")
    public void finishUserSession(@PathVariable("userSessionUUID") String userSessionUUID) {
        this.aiSegService.orElseThrow(() -> new ServiceUnavailableException("ai")).finishUserSession(userSessionUUID);
    }

    @DeleteMapping("/finishFrame/{mediaId}/{frameNumber}")
    public void finishFrameImage(@PathVariable("mediaId") String mediaId,
                                 @PathVariable(value = "frameNumber") Integer frameNumber) {
        this.aiSegService.orElseThrow(() -> new ServiceUnavailableException("ai")).finishFrameImage(mediaId, frameNumber);
    }

}
