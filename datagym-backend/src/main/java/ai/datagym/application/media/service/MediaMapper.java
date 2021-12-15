package ai.datagym.application.media.service;

import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.factory.MediaViewModelFactory;
import ai.datagym.application.media.models.viewModels.MediaViewModel;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class MediaMapper {
    private MediaMapper() {
    }

    public static MediaViewModel mapToMediaViewModel(Media from) {
        return MediaViewModelFactory.createImageViewModel(from);
    }

    public static List<MediaViewModel> mapToMediaViewModel(List<Media> from) {
        return from.stream()
                .filter(media -> !media.isDeleted())
                .map(MediaMapper::mapToMediaViewModel)
                .collect(Collectors.toList());
    }

    public static Set<MediaViewModel> mapToMediaViewModel(Set<Media> from) {
        return from.stream()
                .filter(media -> !media.isDeleted())
                .map(MediaMapper::mapToMediaViewModel)
                .collect(Collectors.toSet());
    }
}
