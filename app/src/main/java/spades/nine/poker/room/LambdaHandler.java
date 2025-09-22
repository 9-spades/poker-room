package spades.nine.poker.room;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import spades.nine.poker.room.model.PlayingCard;

public class LambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static final String CARDS_PATH = "/cards";
    public static final String PATH_ID = "id";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOGGER = Logger.getLogger(LambdaHandler.class.getName());

    private final CardsApiImpl cardsApi;

    public LambdaHandler() {
        this(new CardsApiImpl());
    }

    public LambdaHandler(CardsApiImpl cardsApi) {
        this.cardsApi = cardsApi;
    }

    @SuppressWarnings({"java:S3776", "java:S1141"})
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String method = request.getHttpMethod();
        String path = request.getPath();
        if(LOGGER.isLoggable(Level.INFO)) LOGGER.info(String.format("Got %s call to %s", path, method));
        try {
            switch(method) {
                case HttpMethod.GET:
                    if(CARDS_PATH.equals(path)) return createResponse(Status.OK, cardsApi.cardsGet());
                    break;
                case HttpMethod.POST:
                    if(CARDS_PATH.equals(path)) {
                        if(request.getBody() == null) return createResponse(Status.BAD_REQUEST, VALIDATION_ERROR);
                        try {
                            return createResponse(Status.CREATED, cardsApi.cardsPost(OBJECT_MAPPER.readValue(request.getBody(), PlayingCard.class)));
                        } catch(JsonProcessingException ignore) {
                            return createResponse(Status.BAD_REQUEST, VALIDATION_ERROR);
                        }
                    }
                    break;
                case HttpMethod.DELETE:
                    if(path != null && path.startsWith(CARDS_PATH)) {
                        try {
                            cardsApi.cardsIdDelete(UUID.fromString(request.getPathParameters().get(PATH_ID)));
                            return createResponse(Status.NO_CONTENT);
                        } catch(IllegalArgumentException ignore) {
                            return createResponse(Status.BAD_REQUEST, VALIDATION_ERROR);
                        } catch(NoSuchElementException ignore) {
                            return createResponse(Status.NOT_FOUND, NOT_FOUND);
                        }
                    }
                    break;
                default: return createResponse(Status.METHOD_NOT_ALLOWED);
            }
        } catch(RuntimeException exception) {
            if(LOGGER.isLoggable(Level.SEVERE)) LOGGER.severe(exception.getMessage());
            return createResponse(Status.INTERNAL_SERVER_ERROR, INTERNAL_ERROR);
        }
        return createResponse(Status.NOT_FOUND);
    }

    private APIGatewayProxyResponseEvent createResponse(Status status) {
        return createResponse(status, null);
    }

    private APIGatewayProxyResponseEvent createResponse(Status status, Object body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(status.getStatusCode());
        if(body != null) {
            response.setHeaders(Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));
            try {
                response.setBody(OBJECT_MAPPER.writeValueAsString(body));
            } catch(JsonProcessingException ignore) {
                response.setBody(String.format("{%n    \"message\": \"%s\"%n}", body.toString()));
            }
        }
        return response;
    }
}
