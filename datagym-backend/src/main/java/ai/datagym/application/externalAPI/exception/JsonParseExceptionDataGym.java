package ai.datagym.application.externalAPI.exception;

import com.eforce21.lib.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class JsonParseExceptionDataGym extends ServiceException {
    private static final long serialVersionUID = 1L;

    public JsonParseExceptionDataGym(int lineNr, int columnNr, Throwable cause, String message) {
        super("JSON parse error - Location: line: " + lineNr + ", column: " + columnNr + ".", cause, lineNr, columnNr, message);
    }

    @Override
    public String getKey() {
        return super.getKey() + "_json_parse";
    }

    @Override
    public int getHttpCode() {
        return HttpStatus.BAD_REQUEST.value();
    }
}
