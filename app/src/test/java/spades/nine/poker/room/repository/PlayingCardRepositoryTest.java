package spades.nine.poker.room.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import spades.nine.poker.room.entity.PlayingCardEntity;
import spades.nine.poker.room.utils.PlayingCardEntities;

import static org.junit.jupiter.api.Assertions.*;

class PlayingCardRepositoryTest {
    private PlayingCardRepository repository;

    @BeforeEach
    void setUp() {
        repository = new PlayingCardRepository();
        String env = System.getenv("ENV");
        if(env != null && !"dev".equals(env)) throw new RuntimeException("Not in dev environment");
        repository.findAll().forEach(item -> repository.deleteById(item.getId()));
        if(repository.count() > 0) throw new IllegalStateException(String.format("DynamoDB table '%s' is not empty", PlayingCardRepository.getTableName()));
    }

    @Test
    void save_withValidEntity_returnsEntity() {
        PlayingCardEntity entity = PlayingCardEntities.sampleInstance();
        entity.setId(UUID.randomUUID());

        assertEquals(entity, repository.save(entity));
    }

    @Test
    void save_withNullEntity_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            repository.save(null);
        });
    }

    @Test
    void save_withNullId_throwsException() {
        PlayingCardEntity entity = PlayingCardEntities.sampleInstance();
        entity.setId(null);

        assertThrows(IllegalArgumentException.class, () -> {
            repository.save(entity);
        });
    }

    @Test
    void save_updateExistingEntity_overwritesEntity() {
        UUID id = UUID.randomUUID();
        PlayingCardEntity entity1 = PlayingCardEntities.sampleInstance();
        entity1.setId(id);

        PlayingCardEntity entity2 = PlayingCardEntities.sampleInstance();
        entity2.setId(id);
        entity2.setContent(null);

        repository.save(entity1);

        assertAll(
            () -> assertEquals(entity2, repository.save(entity2)),
            () -> assertEquals(1, repository.count())
        );
    }

    @Test
    void findById_withExistingId_returnsEntity() {
        UUID id = UUID.randomUUID();
        PlayingCardEntity entity = PlayingCardEntities.sampleInstance();
        entity.setId(id);
        repository.save(entity);

        Optional<PlayingCardEntity> result = repository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(entity, result.get());
    }

    @Test
    void findById_withNonExistingId_returnsEmpty() {
        UUID nonExistingId = UUID.randomUUID();

        assertFalse(repository.findById(nonExistingId).isPresent());
    }

    @Test
    void findById_withNullId_returnsEmpty() {
        assertFalse(repository.findById(null).isPresent());
    }

    @Test
    void findAll_whenEmpty_returnsEmptyList() {
        List<PlayingCardEntity> result = repository.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_withMultipleEntities_returnsAllEntities() {
        PlayingCardEntity entity1 = PlayingCardEntities.sampleInstance();
        UUID id1 = UUID.randomUUID();
        entity1.setId(id1);

        PlayingCardEntity entity2 = PlayingCardEntities.sampleInstance();
        UUID id2 = UUID.randomUUID();
        entity2.setId(id2);

        repository.save(entity1);
        repository.save(entity2);
        List<PlayingCardEntity> result = repository.findAll();

        assertAll(
            () -> assertEquals(2, result.size()),
            () -> assertTrue(result.stream().anyMatch(e -> id1.equals(e.getId()))),
            () -> assertTrue(result.stream().anyMatch(e -> id2.equals(e.getId())))
        );
    }

    @Test
    void findAll_returnsDefensiveCopy() {
        PlayingCardEntity entity = PlayingCardEntities.sampleInstance();
        entity.setId(UUID.randomUUID());
        repository.save(entity);

        List<PlayingCardEntity> result1 = repository.findAll();
        List<PlayingCardEntity> result2 = repository.findAll();

        assertAll(
            () -> assertNotSame(result1, result2),
            () -> assertEquals(result1, result2)
        );
    }

    @Test
    void deleteById_withExistingId_returnsTrue() {
        UUID id = UUID.randomUUID();
        PlayingCardEntity entity = PlayingCardEntities.sampleInstance();
        entity.setId(id);
        repository.save(entity);

        assertAll(
            () -> assertTrue(repository.deleteById(id)),
            () -> assertFalse(repository.existsById(id)),
            () -> assertEquals(0, repository.count())
        );
    }

    @Test
    void deleteById_withNonExistingId_returnsFalse() {
        UUID nonExistingId = UUID.randomUUID();

        assertFalse(repository.deleteById(nonExistingId));
    }

    @Test
    void deleteById_withNullId_returnsFalse() {
        assertFalse(repository.deleteById(null));
    }

    @Test
    void existsById_withExistingId_returnsTrue() {
        UUID id = UUID.randomUUID();
        PlayingCardEntity entity = PlayingCardEntities.sampleInstance();
        entity.setId(id);
        repository.save(entity);

        assertTrue(repository.existsById(id));
    }

    @Test
    void existsById_withNonExistingId_returnsFalse() {
        UUID nonExistingId = UUID.randomUUID();

        assertFalse(repository.existsById(nonExistingId));
    }

    @Test
    void existsById_withNullId_returnsFalse() {
        assertFalse(repository.existsById(null));
    }

    @Test
    void count_whenEmpty_returnsZero() {
        assertEquals(0, repository.count());
    }

    @Test
    void count_withMultipleEntities_returnsCorrectCount() {
        PlayingCardEntity entity1 = PlayingCardEntities.sampleInstance();
        entity1.setId(UUID.randomUUID());

        PlayingCardEntity entity2 = PlayingCardEntities.sampleInstance();
        entity2.setId(UUID.randomUUID());

        PlayingCardEntity entity3 = PlayingCardEntities.sampleInstance();
        entity3.setId(UUID.randomUUID());

        repository.save(entity1);
        repository.save(entity2);
        repository.save(entity3);

        assertEquals(3, repository.count());
    }

    @Test
    void count_afterDeletion_updatesCorrectly() {
        UUID id = UUID.randomUUID();
        PlayingCardEntity entity = PlayingCardEntities.sampleInstance();
        entity.setId(id);
        repository.save(entity);

        assertEquals(1, repository.count());
        repository.deleteById(id);
        assertEquals(0, repository.count());
    }
}