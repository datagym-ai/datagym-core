package ai.datagym.application.errorHandling;

import ai.datagym.application.utils.constants.CommonMessages;
import com.eforce21.lib.exception.to.DetailTO;
import com.eforce21.lib.exception.to.ErrorTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final int FIRST_MATCHING_GROUP = 1;
    private static final int SECOND_MATCHING_GROUP = 2;
    private static final Pattern pattern = Pattern.compile("^([a-z]+)*([A-Z][a-z]+)?$");

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Object> onConstraintValidationException(ConstraintViolationException e) {
        ErrorTO errorTO = new ErrorTO();
        errorTO.setKey("ex_validation");
        errorTO.setMsg(CommonMessages.VALIDATION_ERROR_MESSAGE);
        for (ConstraintViolation violation : e.getConstraintViolations()) {

            String annotationName = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
            String[] propertyPathElements = violation.getPropertyPath().toString().split("\\.");
            String propertyName = propertyPathElements[propertyPathElements.length - 1];

            Map<String, String> attributes = violation.getConstraintDescriptor().getAttributes();

            DetailTO detail = new DetailTO();

            String key = "ex_" + Objects.requireNonNull(annotationName).toLowerCase(Locale.ENGLISH);
            detail.setKey(key);

            String parsedFieldName = parseFieldName(propertyName);
            detail.setName(parsedFieldName);

            detail.getParams().add(parsedFieldName);

            Map<String, String> sortedAttributesMap = attributes.entrySet()
                    .stream()
                    .filter(k ->
                            !"groups".equals(k.getKey()) &&
                                    !"message".equals(k.getKey()) &&
                                    !"payload".equals(k.getKey()))
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors
                            .toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            for (Map.Entry<String, String> attribute : sortedAttributesMap.entrySet()) {
                String attributeKey = attribute.getKey();

                if (!"groups".equals(attributeKey) && !"message".equals(attributeKey) && !"payload".equals(attributeKey)) {
                    String attributeValue = String.valueOf(attribute.getValue());

                    detail.addParam(attributeValue);
                }
            }

            errorTO.addDetail(detail);
        }

        return new ResponseEntity<>(errorTO, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorTO errorTO = new ErrorTO();
        errorTO.setKey("ex_validation");
        errorTO.setMsg(CommonMessages.VALIDATION_ERROR_MESSAGE);

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            DetailTO detail = new DetailTO();

            String code = Objects.requireNonNull(error.getCode()).toLowerCase(Locale.ENGLISH);

            String key = "ex_" + code;
            detail.setKey(key);

            String parsedFieldName = parseFieldName(error.getField());
            detail.setName(parsedFieldName);

            detail.getParams().add(parsedFieldName);
            Arrays.stream(Objects.requireNonNull(error.getArguments()))
                    .skip(1)
                    .forEach((Object param) -> detail.addParam(param.toString()));

            errorTO.addDetail(detail);
        }

        return new ResponseEntity<>(errorTO, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    private static String parseFieldName(String fieldName) {
        Matcher matcher = pattern.matcher(fieldName);

        StringBuilder stringBuilder = new StringBuilder();

        while (matcher.find()) {
            String fullMatch = matcher.group();

            if (!fullMatch.isEmpty()) {
                String startWord = matcher.group(FIRST_MATCHING_GROUP);
                String nextWord = matcher.group(SECOND_MATCHING_GROUP);

                if (startWord != null) {
                    stringBuilder.append(startWord.toUpperCase(Locale.ENGLISH).charAt(0)).append(startWord.substring(1)).append(' ');
                }

                if (nextWord != null) {
                    stringBuilder.append(nextWord).append(' ');
                }
            }
        }

        return stringBuilder.toString().trim();
    }
}
