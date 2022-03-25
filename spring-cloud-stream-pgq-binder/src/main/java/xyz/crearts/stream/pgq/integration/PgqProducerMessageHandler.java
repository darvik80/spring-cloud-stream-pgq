package xyz.crearts.stream.pgq.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;

import java.nio.charset.Charset;

@Slf4j
public class PgqProducerMessageHandler extends AbstractMessageHandler {
    private final PgqRepository repository;

    public PgqProducerMessageHandler(JdbcTemplate template, String topic) {
        this.repository = new PgqRepositoryDefault(template, topic);
        this.repository.createQueue();
    }

    @Override
    protected void handleMessageInternal(Message<?> message) {
        String tag = (String)message.getHeaders().get(PgqHeader.TAG);
        String data = null;
        if (message.getPayload() instanceof String) {
            data = (String) message.getPayload();
        } else if (message.getPayload() instanceof byte[]) {
            data = new String((byte[]) message.getPayload(), Charset.defaultCharset());
        }

        if (data != null) {
            repository.publish(tag, data);
        }
    }
}
