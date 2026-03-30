# Ad Insights Platform

## Overview
Real-time + historical ad analytics platform using AWS-native services.
- ElastiCache for real-time reads
- DynamoDB for operational historical queries
- Snowflake for long-range analytics
- Resilience4j for circuit breaker, retry, and bulkhead

## Tech Stack
- Spring Boot
- ElastiCache
- DynamoDB
- Snowflake
- Kafka (MSK)
- Flink (stream processing)

## Features
- Smart query routing
- Circuit breaker + retry
- Async processing
- AWS-native architecture

## API
GET /v1/ad/{campaignId}/clicks?start=...&end=...

Header:
X-Tenant-Id: tenant-123

## Swagger
/swagger-ui.html

## Run
mvn clean install
java -jar target/ad-insights-service-1.0.0.jar

