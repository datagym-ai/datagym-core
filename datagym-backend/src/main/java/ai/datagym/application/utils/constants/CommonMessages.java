package ai.datagym.application.utils.constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * All common exception messages thrown to the client collected in one place.
 */
public final class CommonMessages {
    private CommonMessages() {
    }

    // Params: 0 = What/Fieldname
    public static final String EX_VAL_EMPTY = "ex_val_empty";

    // Params: 0 = What/Fieldname
    public static final String EX_VAL_ZERO = "ex_val_zero";

    // Params: 0 = What/Fieldname, 1 = Allowed values
    public static final String EX_VAL_INVALID = "ex_val_invalid";

    public static final String VALIDATION_ERROR_MESSAGE = "Validation error.";
    public static final String AISEG_COMMUNICATION_ERROR = "aiseg_communication_error";
    public static final String AISEG_GPU_BUSY = "aiseg_gpu_busy";

    // LcEntryTypes
    public static final String SELECT = "SELECT";
    public static final String CHECKLIST = "CHECKLIST";
    public static final String FREETEXT = "FREETEXT";
    public static final String POINT = "POINT";
    public static final String LINE = "LINE";
    public static final String POLYGON = "POLYGON";
    public static final String RECTANGLE = "RECTANGLE";
    public static final String IMAGE_SEGMENTATION = "IMAGE_SEGMENTATION";

    // Images constants
    public static final Collection<String> ALLOWED_IMAGE_MIME_PATTERNS = new ArrayList<>();

    static {
        ALLOWED_IMAGE_MIME_PATTERNS.add("image/jpeg");
        ALLOWED_IMAGE_MIME_PATTERNS.add("image/jpg");
        ALLOWED_IMAGE_MIME_PATTERNS.add("image/png");
    }

    // Allowed Image extensions
    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>();

    static {
        ALLOWED_IMAGE_EXTENSIONS.add("jpeg");
        ALLOWED_IMAGE_EXTENSIONS.add("jpg");
        ALLOWED_IMAGE_EXTENSIONS.add("png");
    }

    // Dummy_Project constants
    public static final String DUMMY_PROJECT_PLACEHOLDER = "Dummy_Project";
    public static final String DUMMY_DATASET_ONE_PLACEHOLDER = "Dummy_Dataset_One";
    public static final String DUMMY_DATASET_TWO_PLACEHOLDER = "Dummy_Dataset_Two";

    // Security Scopes constants
    public static final String TOKEN_SCOPE_TYPE = "type_token";
    public static final String OAUTH_SCOPE_TYPE = "type_oauth";
    public static final String BASIC_SCOPE_TYPE = "type_basic";
    public static final String SUPER_ADMIN_SCOPE_TYPE = "account.admin";
}
