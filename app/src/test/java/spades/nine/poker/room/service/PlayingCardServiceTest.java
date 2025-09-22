package spades.nine.poker.room.service;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import spades.nine.poker.room.entity.PlayingCardEntity;
import spades.nine.poker.room.model.PlayingCard;
import spades.nine.poker.room.repository.PlayingCardRepository;
import spades.nine.poker.room.utils.PlayingCardEntities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlayingCardServiceTest {
    private PlayingCardService service;
    @Mock private PlayingCardRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PlayingCardService(repository);
    }

    @Test
    void getAllItems_whenRepositoryReturnsEmptyList_returnsEmptyList() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<PlayingCardEntity> result = service.getAllItems();

        assertNotNull(result);
        assertAll(
            () -> assertTrue(result.isEmpty()),
            () -> verify(repository).findAll()
        );
    }

    @Test
    void getAllItems_whenRepositoryReturnsCards_returnsList() {
        PlayingCardEntity entity1 = PlayingCardEntities.sampleInstance();
        entity1.setId(UUID.randomUUID());

        PlayingCardEntity entity2 = PlayingCardEntities.sampleInstance();
        entity2.setId(UUID.randomUUID());

        when(repository.findAll()).thenReturn(Arrays.asList(entity1, entity2));

        List<PlayingCardEntity> result = service.getAllItems();

        assertNotNull(result);
        assertAll(
            () -> assertEquals(2, result.size()),
            () ->  verify(repository).findAll()
        );
    }

    @Test
    void createItem_withValidCard_createsEntityAndSaves() {
        PlayingCard inputCard = PlayingCardEntities.sampleInstance();
        PlayingCardEntity savedEntity = PlayingCardEntities.sampleInstance();
        savedEntity.setId(UUID.randomUUID());

        when(repository.save(any(PlayingCardEntity.class))).thenReturn(savedEntity);

        assertAll(
            () -> assertEquals(savedEntity, service.createItem(inputCard)),
            () -> verify(repository).save(any(PlayingCardEntity.class))
        );
    }

    @Test
    void createItem_withNullInput_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.createItem(null);
        });
    }

    @Test
    void createItem_withDuplicateContent_generatesDistinctIds() {
        PlayingCard card1 = PlayingCardEntities.sampleInstance();
        PlayingCard card2 = PlayingCardEntities.sampleInstance();

        Set<UUID> existingIds = new HashSet<>();
        when(repository.existsById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            return existingIds.contains(id);
        });
        when(repository.save(any(PlayingCardEntity.class))).thenAnswer(invocation -> {
            PlayingCardEntity entity = invocation.getArgument(0);
            existingIds.add(entity.getId());
            return entity;
        });

        assertAll(
            () -> assertNotEquals(service.createItem(card1).getId(), service.createItem(card2).getId()),
            () -> verify(repository, times(2)).save(any(PlayingCardEntity.class))
        );
    }


    @Test
    void deleteItem_withExistingId_delegatesToRepository() {
        UUID existingId = UUID.randomUUID();
        when(repository.deleteById(existingId)).thenReturn(true);

        assertAll(
            () -> assertTrue(service.deleteItem(existingId)),
            () -> verify(repository).deleteById(existingId)
        );
    }

    @Test
    void deleteItem_withNonExistingId_returnsFalse() {
        UUID nonExistingId = UUID.randomUUID();
        when(repository.deleteById(nonExistingId)).thenReturn(false);

        assertAll(
            () -> assertFalse(service.deleteItem(nonExistingId)),
            () -> verify(repository).deleteById(nonExistingId)
        );
    }

    @Test
    void deleteItem_withNullId_delegatesToRepository() {
        when(repository.deleteById(null)).thenReturn(false);

        assertAll(
            () -> assertFalse(service.deleteItem(null)),
            () -> verify(repository).deleteById(null)
        );
    }
}