package ai.datagym.application.errorHandling;

import com.eforce21.lib.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class FeatureNotForOpenCoreException extends ServiceException {


    public FeatureNotForOpenCoreException() {
        super(null, null);
    }

    public FeatureNotForOpenCoreException(String msg, Throwable cause, Object... params) {
        super(msg, cause, params);
    }

    public FeatureNotForOpenCoreException(String what) {
        this(what, null);
    }

    public FeatureNotForOpenCoreException(String what, Throwable cause) {
        super("The " + what + " service is not available.", cause, what);
    }

    @Override
    public int getHttpCode() {
        return HttpStatus.SERVICE_UNAVAILABLE.value();
    }

    @Override
    public String getKey() {
        return super.getKey() + "_feature_not_for_open_core";
    }
}
