package spades.nine.poker.room.repository;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoClient {
    private static DynamoDbEnhancedClient client;

    private DynamoClient() {}

    public static DynamoDbEnhancedClient getClient() {
        if(client == null)
            client = DynamoDbEnhancedClient.builder().dynamoDbClient(
                DynamoDbClient.builder()
                    .region(Region.US_WEST_1)
                    .credentialsProvider(ProfileCredentialsProvider.create())
                    .build()
                ).build();
        return client;
    }
}