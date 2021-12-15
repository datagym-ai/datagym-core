package ai.datagym.application.labelIteration.entity.converter;

import ai.datagym.application.labelIteration.entity.geometry.SimplePointPojo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.mapping.MappingException;

import javax.persistence.AttributeConverter;
import java.util.List;

public class SimplePointsListConverter implements AttributeConverter<List<SimplePointPojo>, String> {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public String convertToDatabaseColumn(List<SimplePointPojo> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new MappingException("Failed to map OBJ -> JSON. " + e.getMessage(), e);
        }
    }

    @Override
    public List<SimplePointPojo> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<SimplePointPojo>>() {
            });
        } catch (JsonProcessingException e) {
            throw new MappingException("Failed to map JSON -> OBJ. " + e.getMessage(), e);
        }
    }
}
