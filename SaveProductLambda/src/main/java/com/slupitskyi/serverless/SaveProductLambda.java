package com.slupitskyi.serverless;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slupitskyi.serverless.dto.ProductDTO;


import java.util.UUID;

public class SaveProductLambda {

    private static final String TABLE_NAME = System.getenv("PRODUCTS_TABLE");
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());

    public void handler(SQSEvent event){
        event.getRecords().forEach(sqsMessage -> {
            try {
                ProductDTO productDTO = objectMapper.readValue(sqsMessage.getBody(),
                        ProductDTO.class);
                Table table = dynamoDB.getTable(TABLE_NAME);
                Item item = new Item().withPrimaryKey("id", UUID.randomUUID().toString())
                        .withString("name", productDTO.getName())
                        .withString("description", productDTO.getDescription())
                        .withInt("price", productDTO.getPrice());
                table.putItem(item);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
