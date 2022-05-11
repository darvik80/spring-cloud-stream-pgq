package xyz.crearts.stream.pgq.integration;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.acks.AcknowledgmentCallback;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ObjectUtils;
import xyz.crearts.stream.pgq.properties.PgqConsumerProperties;

@Slf4j
public class PgqMessageSource extends AbstractMessageSource<Object> {
    private final PgqRepository repository;
    private final PgqConsumerProperties props;
    private List<PgqEvent> cache = Collections.emptyList();
    private Iterator<PgqEvent> iter = cache.iterator();
    private final Set<Long> confirmed = new HashSet<>();
    private long batchId = 0L;

    private enum Status {
        REGISTER,
        READY,
    }

    Status status = Status.REGISTER;

    public PgqMessageSource(PgqRepository repository, PgqConsumerProperties props) {
        this.repository = repository;
        this.props = props;
    }

    private boolean doRegisterConsumer() {
        try {
            repository.registerConsumer();
            status = Status.READY;
        } catch (Exception ignore) {}

        return status == Status.READY;
    }

    protected synchronized Object doReadMessages() {
        if (iter.hasNext()) {
            return doProcessMessage(batchId, iter.next());
        }

        var id = repository.getNextId();
        while (id != null && !id.equals(batchId)) {
            batchId = id;
            confirmed.clear();
            cache = repository.getNextBatch(id).stream()
                .filter(
                    item -> {
                        if (ObjectUtils.isEmpty(props.getTags())) {
                            return true;
                        }

                        var tag = item.getEvHeaders().get(PgqHeader.TAG);
                        return props.getTags().contains(tag);
                    }
                ).collect(Collectors.toList());

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
    protected synchronized Object doReceive() {
        switch (status) {
            case REGISTER:
                if (!doRegisterConsumer()) {
                    return null;
                }
            case READY:
            default:
                return doReadMessages();
        }
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
