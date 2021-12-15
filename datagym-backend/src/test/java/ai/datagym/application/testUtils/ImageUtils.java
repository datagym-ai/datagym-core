package ai.datagym.application.testUtils;

import ai.datagym.application.media.entity.*;
import ai.datagym.application.media.models.viewModels.LocalImageViewModel;
import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.media.models.viewModels.UrlImageUploadViewModel;
import ai.datagym.application.media.models.viewModels.UrlImageViewModel;
import com.eforce21.lib.bin.file.model.BinFileConsumerHttp;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImageUtils {
    public static final String IMAGE_ID = "TestId " + UUID.randomUUID();
    public static final String IMAGE_NAME = "TestName " + UUID.randomUUID();
    public static final String IMAGE_URL = "TestUrl " + UUID.randomUUID();
    public static final String TEST_URL_IMAGE_URL = "https://media.datagym.ai/prod/DataGym_integration_tests_images/normal_sized_jpg_image.jpg";
    public static final String UNSUPPORTED_FORMAT_URL = "https://fr.wikipedia.org/wiki/Fichier:JPEG_example_flower.jpg";
    public static final String UNSUPPORTED_FORMAT_URL_2 = "https://fr.wikipedia.org/wiki/Fichier:JPEG_example_flower.html";

    private static final Long TIME = System.currentTimeMillis();

    public static Media createTestImage(String id) {
        return new Media() {{
            setId(id);
            setTimestamp(TIME);
            setDeleted(false);
            setDeleteTime(null);
            setDatasets(new HashSet<>());
            setLabelTasks(new ArrayList<>());
        }};
    }

    public static LocalImage createTestLocalImage() {
        return new LocalImage() {{
            setId(IMAGE_ID);
            setTimestamp(TIME);
            setDeleted(false);
            setDeleteTime(null);
            setDatasets(new HashSet<>());
            setEntryValues(new HashSet<>());
            setLabelTasks(new ArrayList<>());
            setMediaSourceType(MediaSourceType.LOCAL);
            setMediaName(IMAGE_NAME);
            setBinFileEntity(null);
            setWidth(2);
            setHeight(2);
        }};
    }

    public static UrlImage createTestUrlImage() {
        return new UrlImage() {{
            setId(IMAGE_ID);
            setTimestamp(TIME);
            setDeleted(false);
            setDeleteTime(null);
            setDatasets(new HashSet<>());
            setEntryValues(new HashSet<>());
            setLabelTasks(new ArrayList<>());
            setMediaSourceType(MediaSourceType.LOCAL);
            setMediaName(IMAGE_NAME);
            setUrl(TEST_URL_IMAGE_URL);
        }};
    }

    public static Set<LocalImage> createTestSetLocalImages(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new LocalImage() {{
                    setId(IMAGE_ID + count);
                    setTimestamp(TIME);
                    setDeleted(false);
                    setDeleteTime(null);
                    setDatasets(new HashSet<>());
                    setEntryValues(new HashSet<>());
                    setLabelTasks(new ArrayList<>());
                    setMediaSourceType(MediaSourceType.LOCAL);
                    setMediaName(IMAGE_NAME + count);
                    setBinFileEntity(null);
                    setWidth(2);
                    setHeight(2);
                }})
                .collect(Collectors.toSet());
    }

    public static List<LocalImage> createTestListLocalImages(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new LocalImage() {{
                    setId(IMAGE_ID + count);
                    setTimestamp(TIME);
                    setDeleted(false);
                    setDeleteTime(null);
                    setDatasets(new HashSet<>());
                    setEntryValues(new HashSet<>());
                    setLabelTasks(new ArrayList<>());
                    setMediaSourceType(MediaSourceType.LOCAL);
                    setMediaName(IMAGE_NAME + count);
                    setBinFileEntity(null);
                    setWidth(2);
                    setHeight(2);
                }})
                .collect(Collectors.toList());
    }

    public static List<Media> createTestListImages(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new Media() {{
                    setId(String.valueOf(index + 1));
                    setTimestamp(TIME);
                    setDeleted(false);
                    setDeleteTime(null);
                    setMediaName(null);
                    setDatasets(new HashSet<>());
                }})
                .collect(Collectors.toList());
    }

    public static List<String> createTesSetImages(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new Media() {{
                    setId(String.valueOf(index + 1));
                    setTimestamp(TIME);
                    setDeleted(false);
                    setDeleteTime(null);
                    setMediaName(null);
                    setDatasets(new HashSet<>());
                }})
                .map(Media::getId)
                .collect(Collectors.toList());
    }

    public static List<UrlImageUploadViewModel> createTesImageUploadViewModels(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new UrlImageUploadViewModel() {{
                    setImageUrl(String.valueOf(index + 1));
                    setMediaUploadStatus(MediaUploadStatus.SUCCESS.name());
                }})
                .collect(Collectors.toList());
    }

    public static LocalImageViewModel createTestLocalImageViewModel() {
        return new LocalImageViewModel() {{
            setId(IMAGE_ID);
            setTimestamp(TIME);
            setMediaSourceType(MediaSourceType.LOCAL.name());
            setMediaName(IMAGE_NAME);
            setWidth(2);
            setHeight(2);
        }};
    }

    public static UrlImageViewModel createTestUrlImageViewModel() {
        return new UrlImageViewModel() {{
            setId(IMAGE_ID);
            setTimestamp(TIME);
            setMediaSourceType(MediaSourceType.LOCAL.name());
            setMediaName(IMAGE_NAME);
            setUrl(TEST_URL_IMAGE_URL);
        }};
    }

    public static Set<String> createTestImageUrlSet(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> ("ImageUrl" + index))
                .collect(Collectors.toSet());
    }

    public static Set<String> createTestImageIdSet(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> ("ImageId" + index))
                .collect(Collectors.toSet());
    }

    public static List<String> createTestImageIdList(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> ("ImageId" + index))
                .collect(Collectors.toList());
    }

    public static MediaViewModel createTestImageViewModel(String id) {
        return new MediaViewModel() {{
            setId(id);
            setTimestamp(TIME);
            setMediaName(IMAGE_NAME);
        }};
    }

    public static List<MediaViewModel> createTestListImageViewModel(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new MediaViewModel() {{
                    setId(IMAGE_ID);
                    setTimestamp(TIME);
                    setMediaSourceType(MediaSourceType.LOCAL.name());
                    setMediaName(IMAGE_NAME);
                }})
                .collect(Collectors.toList());
    }


    public static List<LocalImageViewModel> createTestListLocalImageViewModel(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new LocalImageViewModel() {{
                    setId(IMAGE_ID);
                    setTimestamp(TIME);
                    setMediaSourceType(MediaSourceType.LOCAL.name());
                    setMediaName(IMAGE_NAME);
                    setWidth(2);
                    setHeight(2);
                }})
                .collect(Collectors.toList());
    }

//    public static List<ImageViewModel> createTestListImageViewModel(int count) {
//        return IntStream.range(0, count)
//                .mapToObj(index -> new ImageViewModel() {{
//                    setId(String.valueOf(index + 1));
//                    setTimestamp(TIME);
//                    setBinFileEntityName("BinFileEntityName " + index);
//                }})
//                .collect(Collectors.toList());
//    }

    public static BinFileConsumerHttp createTestBinFileConsumerHttp() {
        return new BinFileConsumerHttp(null);
    }
}
