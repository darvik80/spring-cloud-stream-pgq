package xyz.crearts.stream.pgq.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.cloud.stream.binder.PollableMessageSource;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PubSubService {
    private final StreamBridge bridge;
    private final PollableMessageSource source;

    @Scheduled(fixedDelay = 5000)
    public void publish() {
        bridge.send(
                "event_bus",
                MessageBuilder.withPayload(String.format("Hello: %d", System.currentTimeMillis()))
                        .setHeader("TAG", "report")
                        .build()
        );
    }

    @Scheduled(fixedDelay = 1000)
    public void poll() {
        while (source.poll(msg -> log.info("poll msg: {}:{}", msg.getHeaders().get("TOPIC"), msg.getPayload()))) { }
    }

    @Bean
    Consumer<Message<String>> eventBusTest() {
        return msg -> {
            log.info("read msg: {}:{}", msg.getHeaders().get("TOPIC"), msg.getPayload());
        };
    }
}
