package ai.datagym.application.media.service;

import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.models.viewModels.MediaViewModel;
import com.eforce21.lib.bin.file.entity.BinFileEntity;
import com.eforce21.lib.exception.NotFoundException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

public interface MediaService {
    /**
     * Is streaming the media file or creating a pre-signed aws url (e.g. for video projects)
     *
     * @param mediaId  The specific media id to stream.
     * @param response Instance of {@link HttpServletResponse}
     * @return Either returns a url or streams the image
     * @throws NotFoundException
     */
    String streamMediaFile(String mediaId, HttpServletResponse response, boolean downloadFile) throws IOException;

    URL createUrl(String urlString) throws MalformedURLException;

    /**
     * Sets isDeleted value of the current media to true or false
     * depending on the {@code deleteMedia} value.
     *
     * @param mediaId
     * @param deleteMedia
     * @return {@link MediaViewModel}
     * @throws NotFoundException
     */
    MediaViewModel deleteMediaFile(String mediaId, boolean deleteMedia);

    /**
     * Deletes an media file. The media will be deleted: <blockquote><pre>
     * 1. from the dataset with id {@code datasetId}.
     * 2. from the bin_file_image datatable.
     * 3. from the bin_data datatable, where the image is saved as binary.
     * 4. also the {@link BinFileEntity} with the current image will be deleted from bin_file table.
     * 5. from the s3 storage if it is an video
     * </pre></blockquote> <p>
     *
     * @param mediaId
     * @throws NotFoundException
     */
    void permanentDeleteMediaFile(String mediaId);

    /**
     * Deletes permanent an media from all Datasets and from the database
     */
    void permanentDeleteMediaFile(Media media, boolean isCronJob);

    /**
     * Sets isDeleted value of the all media in the {@param mediaIdSet} to true or false
     * depending on the {@code deleteMedia} value.
     *
     * @param mediaIdSet
     * @param deleteMedia
     * @throws com.eforce21.lib.exception.GenericException
     */
    void deleteMediaFileList(Set<String> mediaIdSet, boolean deleteMedia);
}
