package com.adinsights.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DynamoDbMetricsRepository {

    private final DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.metrics-table}")
    private String tableName;

    public int getMetricCount(String tenantId,
                              String campaignId,
                              String metricType,
                              Instant start,
                              Instant end) {

        String pk = "TENANT#" + tenantId + "#CAMPAIGN#" + campaignId;

        String startDate = DateTimeFormatter.ISO_LOCAL_DATE
                .withZone(ZoneOffset.UTC)
                .format(start);

        String endDate = DateTimeFormatter.ISO_LOCAL_DATE
                .withZone(ZoneOffset.UTC)
                .format(end);

        String startSk = "METRIC#" + metricType + "#DATE#" + startDate + "#TS#" + start.toEpochMilli();
        String endSk = "METRIC#" + metricType + "#DATE#" + endDate + "#TS#" + end.toEpochMilli();

        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("pk = :pk AND sk BETWEEN :startSk AND :endSk")
                .expressionAttributeValues(Map.of(
                        ":pk", AttributeValue.fromS(pk),
                        ":startSk", AttributeValue.fromS(startSk),
                        ":endSk", AttributeValue.fromS(endSk)
                ))
                .build();

        QueryResponse response = dynamoDbClient.query(request);

        return response.items().stream()
                .mapToInt(item -> Integer.parseInt(item.get("count").n()))
                .sum();
    }
}