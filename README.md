# Ad Insights Platform

## Overview
Real-time + historical ad analytics platform using AWS-native services.

## Tech Stack
- Spring Boot
- Redis (ElastiCache)
- Cassandra (Keyspaces)
- Kafka (MSK)
- Flink (stream processing)

## Features
- Smart query routing
- Circuit breaker + retry
- Async processing
- AWS-native architecture

## Run
mvn clean install
java -jar target/ad-insights-service-1.0.0.jar