package xyz.crearts.stream.pgq.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.support.MessageBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PgqInboundChannelAdapter extends MessageProducerSupport implements Runnable {
    private final PgqRepositoryDefault repository;

    private final ScheduledExecutorService executor;

    public PgqInboundChannelAdapter(JdbcTemplate template, String topic, String groupId) {
        repository = new PgqRepositoryDefault(template, topic, groupId);
        repository.registerConsumer();

        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(this, 0, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        try {
            while (true) {
                var id = repository.getNextId();
                if (id != null) {
                    var msgs = repository.getNextBatch(id);
                    for (var msg : msgs) {
                        this.sendMessage(
                                MessageBuilder.withPayload(msg.getEvData())
                                        .setHeader("TAG", msg.getEvHeaders().get("TAG"))
                                        .build()
                        );
                    }
                    repository.releaseBatch(id);
                    log.debug("processed batch: {}, size: {}", id, msgs.size());
                } else {
                    log.debug("no any batches");
                    break;
                }
            }
        } catch (Exception ex) {
            log.warn("can't process pgp messages", ex);
        }
    }
}
