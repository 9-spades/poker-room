package spades.nine.poker.room.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class ObjectToJsonConverter implements AttributeConverter<Object> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public AttributeValue transformFrom(Object input) {
        try {
            return input != null ? AttributeValue.builder().s(OBJECT_MAPPER.writeValueAsString(input)).build() : AttributeValue.builder().nul(true).build();
        } catch (JsonProcessingException ignore) {
            return AttributeValue.builder().s(input.toString()).build();
        }
    }

    @Override
    public Object transformTo(AttributeValue input) {
        try {
            return input.nul() == null || !input.nul() ? OBJECT_MAPPER.readValue(input.s(), Object.class) : null;
        } catch (Exception ignore) {
            return input.s();
        }
    }

    @Override
    public EnhancedType<Object> type() {
        return EnhancedType.of(Object.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}