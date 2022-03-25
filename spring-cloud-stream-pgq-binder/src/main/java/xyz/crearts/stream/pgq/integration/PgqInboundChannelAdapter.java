package xyz.crearts.stream.pgq.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.Lifecycle;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.support.MessageBuilder;

import java.util.concurrent.*;

@Slf4j
public class PgqInboundChannelAdapter extends MessageProducerSupport implements Runnable {
    private final PgqRepository repository;

    private ScheduledExecutorService executor;

    public PgqInboundChannelAdapter(PgqRepository repository) {
        this.repository = repository;
        this.repository.registerConsumer();
    }

    @Override
    protected void doStart() {
        executor = Executors.newScheduledThreadPool(
                1,
                new ThreadFactory() {
                    int count = 0;

                    @Override
                    public Thread newThread(Runnable runnable) {
                        return new Thread(runnable, String.format("pqg-in-ch-%d", ++count));
                    }
                }
        );
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
                                        .setHeader(PgqHeader.TAG, msg.getEvHeaders().get(PgqHeader.TAG))
                                        .setHeader(PgqHeader.TOPIC, msg.getEvHeaders().get(PgqHeader.TOPIC))
                                        .setHeader(PgqHeader.GROUP, msg.getEvHeaders().get(PgqHeader.GROUP))
                                        .setHeader(PgqHeader.CONSUMER, msg.getEvHeaders().get(PgqHeader.CONSUMER))
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

    @Override
    protected void doStop() {
        executor.shutdown();
    }
}
