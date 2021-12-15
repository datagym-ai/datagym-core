package ai.datagym.application.export.service;

import ai.datagym.application.export.util.ImageSize;
import ai.datagym.application.export.util.ImageSizeReader;
import ai.datagym.application.export.util.SegmentationBitmap;
import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.geometry.LcEntryImageSegmentationValue;
import ai.datagym.application.labelIteration.entity.geometry.PointCollection;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static ai.datagym.application.utils.constants.CommonMessages.*;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class ExportSegmentationServiceImpl implements ExportSegmentationService {

    private static final String LABEL_TASK_PLACEHOLDER = "task";
    private static final String LC_ENTRY_PLACEHOLDER = "lcEntry";
    private static final String IMAGE_PLACEHOLDER = "image";
    private static final String WRONG_LABEL_TASK_STATE = "wrong_label_task_state";

    private final LcEntryValueRepository lcEntryValueRepository;
    private final LabelTaskRepository labelTaskRepository;
    private final Tika tika;

    public ExportSegmentationServiceImpl(
            LcEntryValueRepository lcEntryValueRepository,
            LabelTaskRepository labelTaskRepository,
            Tika tika
    ) {
        this.lcEntryValueRepository = lcEntryValueRepository;
        this.labelTaskRepository = labelTaskRepository;
        this.tika = tika;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE})
    @Override
    public void streamSegmentationBitmap(String taskId, String lcEntryKey, HttpServletResponse response) throws IOException {

        // Permission check: authenticated user
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE, OAUTH_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE);

        List<LabelTaskState> labelTaskStateList = new ArrayList<>();
        labelTaskStateList.add(LabelTaskState.COMPLETED);
        labelTaskStateList.add(LabelTaskState.SKIPPED);
        labelTaskStateList.add(LabelTaskState.REVIEWED);

        LabelTask labelTask =  labelTaskRepository
                .findById(taskId)
                .orElseThrow(() -> new NotFoundException(LABEL_TASK_PLACEHOLDER, "id", "" + taskId));

        if (!labelTaskStateList.contains(labelTask.getLabelTaskState())) {
            // The task is not in allowed / supported state.
            throw new GenericException(WRONG_LABEL_TASK_STATE, null, null, labelTask.getId());
        }

        // Permission check: is the user admin or user of the project.
        Project project = labelTask.getProject();
        String owner = project.getOwner();
        DataGymSecurity.isAdminOrUser(owner, true);

        /*
         * Check if a LcEntry with the given export key exists within the documentation.
         */
        Optional<LcEntry> optionalLcEntry = project.getLabelConfiguration().getEntries().stream()
                .filter(entry -> URLDecoder.decode(entry.getEntryKey(), StandardCharsets.UTF_8).equals(lcEntryKey))
                .findFirst();

        if (optionalLcEntry.isEmpty()) {
            // no entry with the given key is defined.
            throw new NotFoundException(LC_ENTRY_PLACEHOLDER, "entry_key", lcEntryKey);
        }

        Media taskMedia = labelTask.getMedia();
        if (taskMedia.isDeleted()) {
            throw new NotFoundException(IMAGE_PLACEHOLDER, "id", taskMedia.getId());
        }

        List<LcEntryValue> values = lcEntryValueRepository
                .findAllByTaskIdAndEntryKeyAndEntryType(taskId, lcEntryKey, LcEntryType.IMAGE_SEGMENTATION);

        List<List<PointCollection>> pointCollections = values.stream()
                .filter(value -> value instanceof LcEntryImageSegmentationValue)
                .map(value -> ((LcEntryImageSegmentationValue) value).getPointsCollection())
                .collect(Collectors.toList());

        ImageSize image = (new ImageSizeReader(tika)).getSize(taskMedia);
        int height = image.getHeight();
        int width = image.getWidth();

        BufferedImage img = (new SegmentationBitmap(height, width)).addSegmentations(pointCollections).apply().asBufferedImage();

        // Stream the Image
        String mimeType = "image/bmp";
        String formatName = mimeType.split("/")[1].toLowerCase(Locale.ENGLISH);

        // Stream the Image
        response.setHeader("Content-Type", mimeType);
        ImageIO.write(img, formatName, response.getOutputStream());
    }
}
