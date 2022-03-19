package xyz.crearts.stream.pgq.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;

import java.nio.charset.Charset;

@Slf4j
public class PgqProducerMessageHandler extends AbstractMessageHandler {
    private final JdbcTemplate template;
    private final String topic;

    public PgqProducerMessageHandler(JdbcTemplate template, String topic) {
        this.template = template;
        this.topic = topic;

        if (0 < template.queryForObject("SELECT * FROM pgq.create_queue(?)", Long.class, topic)) {
            log.info("create queue {}", topic);
        } else {
            log.info("queue {} already created", topic);
        }
    }

    @Override
    protected void handleMessageInternal(Message<?> message) {
        String tag = (String)message.getHeaders().get("TAG");
        String data = null;
        if (message.getPayload() instanceof String) {
            data = (String) message.getPayload();
        } else if (message.getPayload() instanceof byte[]) {
            data = new String((byte[]) message.getPayload(), Charset.defaultCharset());
        }

        if (data != null) {
            template.queryForRowSet("SELECT * FROM pgq.insert_event(?, ?, ?, ?, '', '', '')", topic, tag, data, tag);
        }
    }
}
