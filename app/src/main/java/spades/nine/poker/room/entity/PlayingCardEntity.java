package spades.nine.poker.room.entity;

import java.util.Objects;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import spades.nine.poker.room.model.PlayingCard;

@DynamoDbBean
public class PlayingCardEntity extends PlayingCard {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    @DynamoDbPartitionKey
    public @NotNull UUID getId() {
        return super.getId();
    }

    @Override
    @DynamoDbConvertedBy(ObjectToJsonConverter.class)
    public Object getContent() {
        return super.getContent();
    }

    @Override
    public int hashCode() {
        try {
            return Objects.hash(
                getId(),
                getLabel(),
                getHeading(),
                getSublabel(),
                OBJECT_MAPPER.writeValueAsString(getContent())
            );
        } catch(JsonProcessingException ignore) {
            return -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o != null && getClass() == o.getClass() && hashCode() == o.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("class PlayingCard {\n");
        sb.append("    id: ").append(indentedString(getId())).append("\n");
        sb.append("    heading: ").append(indentedString(getHeading())).append("\n");
        sb.append("    label: ").append(indentedString(getLabel())).append("\n");
        sb.append("    sublabel: ").append(indentedString(getSublabel())).append("\n");
        try {
            sb.append("    content: ").append(indentedString(OBJECT_MAPPER.writeValueAsString(getContent())));
        } catch(JsonProcessingException exception) {
            sb.append("ERROR");
        } finally {
            sb.append("\n}");
        }
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String indentedString(Object o) {
        if (o == null) {
        return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
