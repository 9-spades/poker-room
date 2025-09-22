package spades.nine.poker.room;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import spades.nine.poker.room.entity.PlayingCardEntity;
import spades.nine.poker.room.model.PlayingCard;
import spades.nine.poker.room.service.PlayingCardService;
import spades.nine.poker.room.utils.PlayingCardEntities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CardsApiImplTest {
    private CardsApiImpl api;
    @Mock private PlayingCardService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        api = new CardsApiImpl(service);
    }

    @Test
    void getGreeting_returnsExpectedMessage() {
        assertEquals("Hello World!", api.getGreeting());
    }

    @Test
    void cardsGet_whenServiceReturnsEmptyList_returnsEmptyList() {
        when(service.getAllItems()).thenReturn(Collections.emptyList());

        List<PlayingCard> result = api.cardsGet();

        assertNotNull(result);
        assertAll(
            () -> assertTrue(result.isEmpty()),
            () -> verify(service).getAllItems()
        );
    }

    @Test
    void cardsGet_whenServiceReturnsCards_returnsListOfPlayingCards() {
        PlayingCardEntity entity1 = PlayingCardEntities.sampleInstance();
        PlayingCardEntity entity2 = PlayingCardEntities.sampleInstance();

        when(service.getAllItems()).thenReturn(Arrays.asList(entity1, entity2));

        List<PlayingCard> result = api.cardsGet();

        assertNotNull(result);
        assertAll(
            () -> assertEquals(2, result.size()),
            () -> verify(service).getAllItems()
        );
    }

    @Test
    void cardsPost_withValidPlayingCard_returnsCreatedCard() {
        PlayingCard inputCard = (PlayingCard) PlayingCardEntities.sampleInstance();
        PlayingCardEntity mockEntity = PlayingCardEntities.sampleInstance();
        mockEntity.setId(UUID.randomUUID());

        when(service.createItem(any(PlayingCard.class))).thenReturn(mockEntity);

        assertAll(
            () -> assertEquals(mockEntity, api.cardsPost(inputCard)),
            () -> verify(service).createItem(inputCard)
        );
    }

    @Test
    void cardsPost_withNullCard_delegatesToService() {
        when(service.createItem(null)).thenThrow(new IllegalArgumentException());

        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> {
                api.cardsPost(null);
            }),
            () -> verify(service).createItem(null)
        );
    }

    @Test
    void cardsIdDelete_withExistingId_deletesSuccessfully() {
        UUID existingId = UUID.randomUUID();
        when(service.deleteItem(existingId)).thenReturn(true);

        assertAll(
            () -> assertDoesNotThrow(() -> {
                api.cardsIdDelete(existingId);
            }),
            () -> verify(service).deleteItem(existingId)
        );
    }

    @Test
    void cardsIdDelete_withNonExistingId_throwsNoSuchElementException() {
        UUID nonExistingId = UUID.randomUUID();
        when(service.deleteItem(nonExistingId)).thenReturn(false);

        assertAll(
            () -> assertThrows(NoSuchElementException.class, () -> {
                 api.cardsIdDelete(nonExistingId);
                }),
            () -> verify(service).deleteItem(nonExistingId)
        );
    }

    @Test
    void cardsIdDelete_withNullId_delegatesToService() {
        when(service.deleteItem(null)).thenReturn(false);

        assertAll(
            () -> assertThrows(NoSuchElementException.class, () -> {
                api.cardsIdDelete(null);
            }),
            () -> verify(service).deleteItem(null)
        );
    }
}
