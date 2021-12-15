package ai.datagym.application.errorHandling;

import com.eforce21.lib.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class NoContentException extends ServiceException {
    public NoContentException(String msg, Throwable cause, Object... params) {
        super(msg, cause, params);
    }

    public NoContentException(String what, String criteria, String value) {
        this(what, criteria, value, null);
    }

    public NoContentException(String what, String criteria, String value, Throwable cause) {
        super("No more content for " + what + "with" + criteria + " " + value, cause, what, criteria, value);
    }

    @Override
    public int getHttpCode() {
        return HttpStatus.ACCEPTED.value();
    }

    @Override
    public String getKey() {
        return super.getKey() + "_no_content";
    }
}
