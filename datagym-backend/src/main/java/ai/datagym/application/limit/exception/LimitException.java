package ai.datagym.application.limit.exception;

import com.eforce21.lib.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class LimitException extends ServiceException {
    private static final long serialVersionUID = 1L;

    public LimitException(String what, String current, String max, Throwable cause) {
        super( what + " limit exceed. (" + current + " / " + max   + " )", cause, what, current, max);
    }

    @Override
    public String getKey() {
        return super.getKey() + "_limit";
    }

    @Override
    public int getHttpCode() {
        return HttpStatus.PAYMENT_REQUIRED.value();
    }
}
