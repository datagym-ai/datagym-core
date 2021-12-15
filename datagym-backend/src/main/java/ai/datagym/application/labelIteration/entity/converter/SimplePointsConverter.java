package ai.datagym.application.labelIteration.entity.converter;

import ai.datagym.application.labelIteration.entity.geometry.SimplePointPojo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.mapping.MappingException;

import javax.persistence.AttributeConverter;

public class SimplePointsConverter implements AttributeConverter<SimplePointPojo, String> {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(SimplePointPojo attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new MappingException("Failed to map OBJ -> JSON. " + e.getMessage(), e);
        }
    }

    @Override
    public SimplePointPojo convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, SimplePointPojo.class);
        } catch (JsonProcessingException e) {
            throw new MappingException("Failed to map JSON -> OBJ. " + e.getMessage(), e);
        }
    }
}
