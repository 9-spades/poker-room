package spades.nine.poker.room;

import java.util.*;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import spades.nine.poker.room.entity.PlayingCardEntity;
import spades.nine.poker.room.model.PlayingCard;
import spades.nine.poker.room.utils.PlayingCardEntities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LambdaHandlerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private LambdaHandler handler;
    @Mock private Context context;
    @Mock private CardsApiImpl cardsApi;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new LambdaHandler(cardsApi);
    }

    @Test
    void handleRequest_getCards_returnsSuccessResponse() {
        List<PlayingCard> mockCards = Arrays.asList(
            PlayingCardEntities.sampleInstance(),
            PlayingCardEntities.sampleInstance()
        );
        when(cardsApi.cardsGet()).thenReturn(mockCards);

        assertAll(
            () -> assertValid(handler.handleRequest(createRequest(LambdaHandler.CARDS_PATH), context), Status.OK, mockCards),
            () -> verify(cardsApi).cardsGet()
        );
    }

    @Test
    void handleRequest_getCards_whenEmptyList_returnsEmptyArray() {
        when(cardsApi.cardsGet()).thenReturn(Collections.emptyList());

        assertAll(
            () -> assertValid(handler.handleRequest(createRequest(LambdaHandler.CARDS_PATH), context), Status.OK, Collections.emptyList()),
            () -> verify(cardsApi).cardsGet()
        );
    }

    @Test
    void handleRequest_postCard_returnsCreatedResponse() {
        PlayingCard inputCard = PlayingCardEntities.sampleInstance();
        PlayingCardEntity mockResult = PlayingCardEntities.sampleInstance();
        mockResult.setId(UUID.randomUUID());

        when(cardsApi.cardsPost(any(PlayingCard.class))).thenReturn(mockResult);

        assertAll(
            () -> assertValid(
                handler.handleRequest(createRequest(HttpMethod.POST, LambdaHandler.CARDS_PATH, OBJECT_MAPPER.writeValueAsString(inputCard)), context),
                Status.CREATED,
                mockResult
            ),
            () -> verify(cardsApi).cardsPost(any(PlayingCard.class))
        );
    }

    @Test
    void handleRequest_postCard_withInvalidJson_returnsBadRequest() {
        assertAll(
            () -> assertValid(
                handler.handleRequest(createRequest(HttpMethod.POST, LambdaHandler.CARDS_PATH, "{invalid-json}"), context),
                Status.BAD_REQUEST,
                LambdaHandler.VALIDATION_ERROR
            ),
            () -> verify(cardsApi, never()).cardsPost(any())
        );
    }

    @Test
    void handleRequest_postCard_withNullBody_returnsBadRequest() {
        assertAll(
            () -> assertValid(
                handler.handleRequest(createRequest(HttpMethod.POST, LambdaHandler.CARDS_PATH, null), context),
                Status.BAD_REQUEST,
                LambdaHandler.VALIDATION_ERROR
            ),
            () -> verify(cardsApi, never()).cardsPost(any())
        );
    }

    @Test
    void handleRequest_deleteCard_returnsNoContentResponse() {
        UUID cardId = UUID.randomUUID();
        APIGatewayProxyRequestEvent request = createRequest(HttpMethod.DELETE, String.format("%s/%s", LambdaHandler.CARDS_PATH, cardId), null);
        request.setPathParameters(Collections.singletonMap(LambdaHandler.PATH_ID, cardId.toString()));
        doNothing().when(cardsApi).cardsIdDelete(cardId);

        assertAll(
            () -> assertValid(handler.handleRequest(request, context), Status.NO_CONTENT, null),
            () -> verify(cardsApi).cardsIdDelete(cardId)
        );
    }

    @Test
    void handleRequest_deleteCard_withNonExistingId_returnsNotFound() {
        UUID nonExistingId = UUID.randomUUID();
        APIGatewayProxyRequestEvent request = createRequest(HttpMethod.DELETE, String.format("%s/%s", LambdaHandler.CARDS_PATH, nonExistingId), null);
        request.setPathParameters(Collections.singletonMap(LambdaHandler.PATH_ID, nonExistingId.toString()));
        doThrow(new NoSuchElementException()).when(cardsApi).cardsIdDelete(nonExistingId);

        assertAll(
            () -> assertValid(handler.handleRequest(request, context), Status.NOT_FOUND, LambdaHandler.NOT_FOUND),
            () -> verify(cardsApi).cardsIdDelete(nonExistingId)
        );
    }

    @Test
    void handleRequest_deleteCard_withInvalidUuid_returnsBadRequest() {
        String invalidUuid = "invalid-uuid";
        APIGatewayProxyRequestEvent request = createRequest(HttpMethod.DELETE, String.format("%s/%s", LambdaHandler.CARDS_PATH, invalidUuid), null);
        request.setPathParameters(Collections.singletonMap(LambdaHandler.PATH_ID, invalidUuid));

        assertAll(
            () -> assertValid(handler.handleRequest(request, context), Status.BAD_REQUEST, LambdaHandler.VALIDATION_ERROR),
            () -> verify(cardsApi, never()).cardsIdDelete(any())
        );
    }

    @Test
    void handleRequest_unsupportedMethod_returnsMethodNotAllowed() {
        assertEquals(Status.METHOD_NOT_ALLOWED.getStatusCode(), handler.handleRequest(createRequest(HttpMethod.PUT, LambdaHandler.CARDS_PATH, null), context).getStatusCode());
    }

    @Test
    void handleRequest_unsupportedPath_returnsNotFound() {
        assertEquals(Status.NOT_FOUND.getStatusCode(),  handler.handleRequest(createRequest("/unknown"), context).getStatusCode());
    }

    @Test
    void handleRequest_internalServerError_returnsInternalServerError() {
        when(cardsApi.cardsGet()).thenThrow(new RuntimeException());

        assertAll(
            () -> assertValid(handler.handleRequest(createRequest(LambdaHandler.CARDS_PATH), context), Status.INTERNAL_SERVER_ERROR, LambdaHandler.INTERNAL_ERROR),
            () -> verify(cardsApi).cardsGet()
        );
    }

    private APIGatewayProxyRequestEvent createRequest(String path) {
        return createRequest(HttpMethod.GET, path, null);
    }

    private APIGatewayProxyRequestEvent createRequest(String method, String path, String body) {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod(method);
        request.setPath(path);
        request.setBody(body);
        request.setHeaders(Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));
        return request;
    }

    private void assertValid(APIGatewayProxyResponseEvent response, Status status, Object body) {
        assertAll(
            () -> assertEquals(status.getStatusCode(), response.getStatusCode()),
            () -> {
                if(body != null) assertAll(
                    () -> assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE)),
                    () -> {
                        if(body instanceof String) assertTrue(response.getBody().contains(body.toString()));
                        else assertEquals(OBJECT_MAPPER.writeValueAsString(body), response.getBody());
                    }
                );
                else assertNull(response.getBody());
            }
        );
    }
}