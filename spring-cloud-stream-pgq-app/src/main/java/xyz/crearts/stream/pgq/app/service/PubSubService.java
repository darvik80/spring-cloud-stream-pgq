package xyz.crearts.stream.pgq.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PubSubService {
    private final StreamBridge bridge;


    @Scheduled(fixedDelay = 5000)
    public void publish() {
        bridge.send(
                "event_bus",
                MessageBuilder.withPayload(String.format("Hello: %d", System.currentTimeMillis()))
                        .setHeader("TAG", "report")
                        .build()
        );
    }

    @Bean
    @DependsOnDatabaseInitialization
    Consumer<Message<String>> input() {
        return msg -> {
            log.info("got msg: {}:{}", msg.getHeaders().get("TOPIC"), msg.getPayload());
        };
    }

}
