
package com.example.adinsights.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ClickEventConsumer {

    @KafkaListener(topics = "ad-clicks", groupId = "ad-group")
    public void consume(String message) {
        System.out.println("Received click event: " + message);
    }
}
