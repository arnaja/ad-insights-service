package com.adinsights;

import com.adinsights.dto.MetricResponse;
import com.adinsights.service.AdInsightsService;
import com.adinsights.service.CassandraHistoricalService;
import com.adinsights.service.RedisRealtimeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdInsightsServiceTest {

    @Mock
    private RedisRealtimeService redisService;

    @Mock
    private CassandraHistoricalService cassandraService;

    @InjectMocks
    private AdInsightsService service;

    @Test
    void testRealtimePath() {
        when(redisService.getClicks(any(), any())).thenReturn(100);

        MetricResponse response = service.getClicks(
                "t1", "c1", Instant.now(), Instant.now()
        );

        assertEquals(100, response.getCount());
        assertEquals("HYBRID", response.getSource());
    }
}