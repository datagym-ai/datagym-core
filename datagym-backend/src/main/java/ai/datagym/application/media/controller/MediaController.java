package ai.datagym.application.media.controller;

import ai.datagym.application.media.service.MediaService;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/media")
@Validated
public class MediaController {
    private final MediaService mediaService;

    @Autowired
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    // Get Image or presigned uri
    @GetMapping("/{mediaId}")
    public String streamMediaFile(HttpServletResponse response,
                                  @NotBlank @Length(min = 1) @PathVariable("mediaId") String mediaId,
                                  @RequestParam(value = "dl", defaultValue = "false") boolean downloadFile)
            throws IOException {
        return mediaService.streamMediaFile(mediaId, response, downloadFile);
    }

    // Sets isDeleted to true
    @DeleteMapping("/{mediaId}")
    public void deleteMedia(@NotBlank @Length(min = 1) @PathVariable("mediaId") String mediaId) {
        mediaService.deleteMediaFile(mediaId, true);
    }

    // Sets isDeleted to true to all media in the mediaIdSet
    @DeleteMapping("/list")
    public void deleteMediaList(@RequestBody @Valid Set<String> mediaIdSet) {
        mediaService.deleteMediaFileList(mediaIdSet, true);
    }

    // Sets isDeleted to false
    @DeleteMapping("/{mediaId}/restore")
    public void restoreMedia(@NotBlank @Length(min = 1) @PathVariable("mediaId") String mediaId) {
        mediaService.deleteMediaFile(mediaId, false);
    }

    // Deletes permanent media
    @DeleteMapping("/{mediaId}/deleteFromDb")
    public void permanentDeleteMedia(@NotBlank @Length(min = 1) @PathVariable("mediaId") String mediaId) {
        mediaService.permanentDeleteMediaFile(mediaId);
    }
}
