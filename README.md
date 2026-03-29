
# Ad Insights Service

## Architecture
Kafka -> Flink -> Redis -> API Service -> Client

## API
GET /ad/{campaignId}/clicks

curl:
curl -H "X-Tenant-ID: t1" http://localhost:8080/ad/123/clicks

## Scaling
- Horizontal scaling (K8s HPA)
- Redis caching
- Kafka partitioning

## Trade-offs
- Real-time vs accuracy (eventual consistency)
- Cache staleness vs latency

## Observability
- Prometheus metrics via /actuator/prometheus
