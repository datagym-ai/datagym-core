package ai.datagym.application.media.validate;

import com.eforce21.lib.bin.file.model.BinFileUpdate;
import com.eforce21.lib.exception.Detail;
import com.eforce21.lib.exception.ValidationException;
import org.springframework.util.AntPathMatcher;

import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

public class ImageValidator {
    // Params: 0 = What/Fieldname
    public static final String EX_VAL_EMPTY = "ex_val_empty";

    // Params: 0 = What/Fieldname
    public static final String EX_VAL_BIN_ANALYSE = "ex_val_bin_analyse";

    // Params: 0 = Actual mime, 1 = Allowed mimes.
    public static final String EX_VAL_BIN_MIME = "ex_val_bin_mime";

    private static AntPathMatcher antPathMatcher = new AntPathMatcher();

    static {
        antPathMatcher.setCaseSensitive(false);
    }

    public static void validate(String filename, InputStream is) {
        ValidationException ve = new ValidationException();

        if (filename == null || filename.trim().isEmpty()) {
            ve.addDetail(new Detail("filename", EX_VAL_EMPTY, "Name"));
        }
        if (is == null) {
            ve.addDetail(new Detail("data", EX_VAL_EMPTY, "Data"));
        }

        if (ve.hasDetails()) {
            throw ve;
        }
    }

    public static void validateMimes(String actualMimeType, Collection<String> allowedMimeTypesAntPatterns) {
        ValidationException ve = new ValidationException();

        if (actualMimeType == null || actualMimeType.isEmpty()) {
            ve.addDetail(new Detail("data", EX_VAL_BIN_ANALYSE));
            throw ve;
        }

        if (allowedMimeTypesAntPatterns != null) {
            Optional<String> matchingPattern = allowedMimeTypesAntPatterns.stream().filter(allowedPattern -> {
                return antPathMatcher.match(allowedPattern, actualMimeType);
            }).findAny();

            if (matchingPattern.isEmpty()) {
                ve.addDetail(new Detail("data", EX_VAL_BIN_MIME, actualMimeType, allowedMimeTypesAntPatterns.toString()));
                throw ve;
            }
        }
    }

    public static void validate(BinFileUpdate update) {
        ValidationException ve = new ValidationException();

        if (update.getName() == null || update.getName().isEmpty()) {
            ve.addDetail(new Detail("name", EX_VAL_EMPTY));
        }

        if (ve.hasDetails()) {
            throw ve;
        }
    }
}
