package xyz.crearts.stream.pgq.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.acks.AcknowledgmentCallback;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Slf4j
public class PgqMessageSource extends AbstractMessageSource<Object> {
    private final PgqRepository repository;
    private List<PgqEvent> cache = Collections.emptyList();
    private Iterator<PgqEvent> iter = cache.iterator();
    private final Set<Long> confirmed = new HashSet<>();
    private long batchId = 0L;

    public PgqMessageSource(PgqRepository repository) {
        this.repository = repository;
        repository.registerConsumer();
    }

    @Override
    protected synchronized Object doReceive() {
        if (iter.hasNext()) {
            return doProcessMessage(batchId, iter.next());
        }

        var id = repository.getNextId();
        while (id != null && !id.equals(batchId)) {
            batchId = id;
            confirmed.clear();
            cache = repository.getNextBatch(id);
            iter = cache.iterator();
            if (iter.hasNext()) {
                return doProcessMessage(id, iter.next());
            }
            if (cache.size() == 0) {
                repository.releaseBatch(batchId);
                id = repository.getNextId();
            } else {
                break;
            }
        }

        return null;
    }

    @Override
    public String getComponentType() {
        return "pgq:message-source";
    }

    private Message<?> doProcessMessage(long batchId, PgqEvent event) {
        return MessageBuilder.withPayload(event.getEvData())
                .setHeader(PgqHeader.TAG, event.getEvHeaders().get(PgqHeader.TAG))
                .setHeader(PgqHeader.TOPIC, event.getEvHeaders().get(PgqHeader.TOPIC))
                .setHeader(PgqHeader.GROUP, event.getEvHeaders().get(PgqHeader.GROUP))
                .setHeader(PgqHeader.CONSUMER, event.getEvHeaders().get(PgqHeader.CONSUMER))
                .setHeader(
                        IntegrationMessageHeaderAccessor.ACKNOWLEDGMENT_CALLBACK,
                        new AcknowledgmentCallback() {
                            @Override
                            public synchronized void acknowledge(Status status) {
                                // TODO: if handle error reset batch  for read again
                                switch (status) {
                                    case REQUEUE:
                                        repository.retry(batchId, event.getEvId());
                                    case ACCEPT:
                                        confirmed.add(event.getEvId());
                                        if (confirmed.size() == cache.size()) {
                                            repository.releaseBatch(batchId);
                                            cache.clear();
                                            iter = cache.iterator();
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                )
                .build();
    }
}
