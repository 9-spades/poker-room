package spades.nine.poker.room.entity;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import spades.nine.poker.room.utils.PlayingCardEntities;

import static org.junit.jupiter.api.Assertions.*;

class PlayingCardEntityTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void defaultConstructor_createEmptyEntity() {
        PlayingCardEntity entity = new PlayingCardEntity();

        assertAll(
            () -> assertNull(entity.getId()),
            () -> assertNull(entity.getHeading()),
            () -> assertNull(entity.getLabel()),
            () -> assertNull(entity.getSublabel()),
            () -> assertNull(entity.getContent())
        );
    }

    @Test
    void settersAndGetters_workCorrectly() {
        PlayingCardEntity entity = new PlayingCardEntity();
        PlayingCardEntity expected = PlayingCardEntities.sampleInstance();
        UUID id = UUID.randomUUID();
        expected.setId(id);

        entity.setId(expected.getId());
        entity.setHeading(expected.getHeading());
        entity.setLabel(expected.getLabel());
        entity.setSublabel(expected.getSublabel());
        entity.setContent(expected.getContent());

        assertEquals(expected, entity);
    }

    @Test
    void equals_withSameContent_returnsTrue() {
        UUID id = UUID.randomUUID();

        PlayingCardEntity entity1 = PlayingCardEntities.sampleInstance();
        entity1.setId(id);

        PlayingCardEntity entity2 = PlayingCardEntities.sampleInstance();
        entity2.setId(id);

        assertEquals(entity1, entity2);
    }

    @Test
    void equals_withDifferentContent_returnsFalse() {
        UUID id = UUID.randomUUID();

        PlayingCardEntity entity1 = PlayingCardEntities.sampleInstance();
        entity1.setId(id);

        PlayingCardEntity entity2 = PlayingCardEntities.sampleInstance();
        entity2.setId(id);
        entity2.setContent(null);

        assertNotEquals(entity1, entity2);
    }

    @Test
    void toString_includesAllFields() {
        PlayingCardEntity entity = PlayingCardEntities.sampleInstance();
        UUID id = UUID.randomUUID();
        entity.setId(id);

        String toString = entity.toString();
System.out.println(toString);
        assertAll(
            () -> assertTrue(toString.contains(id.toString())),
            () -> assertTrue(toString.contains(PlayingCardEntities.sampleInstance().getHeading())),
            () -> assertTrue(toString.contains(PlayingCardEntities.sampleInstance().getLabel())),
            () -> assertTrue(toString.contains(PlayingCardEntities.sampleInstance().getSublabel())),
            () -> assertTrue(toString.contains(OBJECT_MAPPER.writeValueAsString(PlayingCardEntities.sampleInstance().getContent())))
        );
    }
}