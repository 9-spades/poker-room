package spades.nine.poker.room.repository;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import spades.nine.poker.room.entity.PlayingCardEntity;

public class PlayingCardRepository {
    public static final String TABLE_NAME = "playing-cards";
    private static final Logger LOGGER = Logger.getLogger(PlayingCardRepository.class.getName());

    private final DynamoDbTable<PlayingCardEntity> table;

    public static String getTableName() {
        String env = System.getenv("ENV");
        return String.format("%s-%s", TABLE_NAME, env != null ? env : "dev");
    }

    public PlayingCardRepository() {
        this(DynamoClient.getClient());
    }

    public PlayingCardRepository(DynamoDbEnhancedClient client) {
        table = client.table(getTableName(), TableSchema.fromBean(PlayingCardEntity.class));
        if(LOGGER.isLoggable(Level.INFO))
            LOGGER.info(String.format("Successfully initialized DynamoDB table '%s'", getTableName()));
    }

    @SuppressWarnings("java:S2589")
    public PlayingCardEntity save(PlayingCardEntity entity) {
        if(entity == null || entity.getId() == null) throw new IllegalArgumentException();
        table.putItem(entity);
        if(LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Successfully saved entity with ID " + entity.getId());
        return entity;
    }

    public Optional<PlayingCardEntity> findById(UUID id) {
        return id != null ? Optional.ofNullable(table.getItem(Key.builder().partitionValue(id.toString()).build())) : Optional.empty();
}

    public List<PlayingCardEntity> findAll() {
        List<PlayingCardEntity> items = new ArrayList<>();
        table.scan(ScanEnhancedRequest.builder().build()).items().forEach(items::add);
        return items;
    }

    public boolean deleteById(UUID id) {
        if (id == null) return false;
        boolean deleted = table.deleteItem(Key.builder().partitionValue(id.toString()).build()) != null;
        if(LOGGER.isLoggable(Level.INFO) && deleted)
            LOGGER.info("Successfully deleted entity with ID " + id);
        return deleted;
    }
    
    public boolean existsById(UUID id) {
        return id != null && findById(id).isPresent();
    }

    public long count() {
        return table.scan(ScanEnhancedRequest.builder().build()).items().stream().count();
    }
}