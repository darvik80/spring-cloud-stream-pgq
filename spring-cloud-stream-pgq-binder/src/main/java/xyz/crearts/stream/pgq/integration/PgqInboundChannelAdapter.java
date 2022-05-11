package xyz.crearts.stream.pgq.integration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ObjectUtils;
import xyz.crearts.stream.pgq.properties.PgqConsumerProperties;

/**
 * @author ivan.kishchenko
 */
@Slf4j
public class PgqInboundChannelAdapter extends MessageProducerSupport {
    private final PgqRepository repository;

    private final PgqConsumerProperties props;

    private ScheduledExecutorService executor;

    public PgqInboundChannelAdapter(PgqRepository repository, PgqConsumerProperties props) {
        this.repository = repository;
        this.props = props;
    }

    @Override
    protected void doStart() {
        executor =
            new ScheduledThreadPoolExecutor(
                1,
                runnable -> new Thread(runnable, "pqg-in-ch")
            );

        executor.schedule(this::doRegisterConsumer, 0, TimeUnit.MILLISECONDS);
    }

    private void doRegisterConsumer() {
        try {
            this.repository.registerConsumer();
            executor.scheduleWithFixedDelay(this::doProcessMessages, 0, props.getPullDelay(), TimeUnit.MILLISECONDS);
        } catch (Exception ignore) {
            executor.schedule(this::doRegisterConsumer, props.getPullDelay(), TimeUnit.MILLISECONDS);
        }
    }

    public void doProcessMessages() {
        try {
            while (true) {
                var id = repository.getNextId();
                if (id != null) {
                    var msgs = repository.getNextBatch(id);
                    for (var msg : msgs) {
                        var tag = msg.getEvHeaders().get(PgqHeader.TAG);
                        if (!ObjectUtils.isEmpty(props.getTags()) && !props.getTags().contains(tag)) {
                            continue;
                        }
                        this.sendMessage(
                            MessageBuilder.withPayload(msg.getEvData())
                                .setHeader(PgqHeader.TAG, tag)
                                .setHeader(PgqHeader.TOPIC, msg.getEvHeaders().get(PgqHeader.TOPIC))
                                .setHeader(PgqHeader.GROUP, msg.getEvHeaders().get(PgqHeader.GROUP))
                                .setHeader(PgqHeader.CONSUMER, msg.getEvHeaders().get(PgqHeader.CONSUMER))
                                .build()
                        );
                    }
                    repository.releaseBatch(id);
                    log.debug("Processed batch: {}, size: {}", id, msgs.size());
                } else {
                    log.debug("No any batches");
                    break;
                }
            }
        } catch (Exception ex) {
            log.warn("Can't process pgp messages", ex);
        }
    }

    @Override
    protected void doStop() {
        executor.shutdown();
    }
}
