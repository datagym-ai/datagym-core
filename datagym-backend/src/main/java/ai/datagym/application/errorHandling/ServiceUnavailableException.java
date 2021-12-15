package ai.datagym.application.errorHandling;

import com.eforce21.lib.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends ServiceException {

    public ServiceUnavailableException(String msg, Throwable cause, Object... params) {
        super(msg, cause, params);
    }

    public ServiceUnavailableException(String what) {
        this(what, null);
    }

    public ServiceUnavailableException(String what, Throwable cause) {
        super("The " + what + " service is not available.", cause, what);
    }

    @Override
    public int getHttpCode() {
        return HttpStatus.SERVICE_UNAVAILABLE.value();
    }

    @Override
    public String getKey() {
        return super.getKey() + "_service_unavailable";
    }
}
