package ai.datagym.application.errorHandling;

import com.eforce21.lib.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class AlreadyContainsException extends ServiceException {
    public AlreadyContainsException(String msg, Throwable cause, Object... params) {
        super(msg, cause, params);
    }

    public AlreadyContainsException(String what, String value) {
        this(what, value, null);
    }

    public AlreadyContainsException(String what, String value, Throwable cause) {
        super("The current " + what + "already contains the " + value, cause, what, value);
    }

    @Override
    public int getHttpCode() {
        return HttpStatus.PRECONDITION_FAILED.value();
    }

    @Override
    public String getKey() {
        return super.getKey() + "_already_contains";
    }
}
