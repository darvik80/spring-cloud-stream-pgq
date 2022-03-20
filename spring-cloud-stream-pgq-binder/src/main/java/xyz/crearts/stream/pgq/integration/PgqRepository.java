package xyz.crearts.stream.pgq.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Slf4j
public class PgqRepository {
    private final JdbcTemplate template;
    private final String topic;
    private final String groupId;

    public PgqRepository(JdbcTemplate template, String topic, String groupId) {
        this.template = template;
        this.topic = topic;
        this.groupId = groupId;
    }

    public void register() {
        if (0 < template.queryForObject("SELECT * FROM pgq.register_consumer(?, ?)", Long.class, topic, groupId)) {
            log.info("repair subscribe for {}:{}", topic, groupId);
        } else {
            log.info("already subscribed for {}:{}", topic, groupId);
        }
    }

    public Long getNextId() {
        var rec = template.queryForRowSet("SELECT * FROM pgq.next_batch(?, ?)", topic, groupId);
        if (rec.next()) {
            var id = rec.getLong(1);
            if (id == 0) {
                return null;
            }

            return id;
        }

        return null;
    }

    public List<PgqEvent> getNextBatch(Long id) {
        return template.query("SELECT * FROM pgq.get_batch_events(?)", new PgqEventRowMapper(), id);
    }

    public boolean releaseBatch(Long id) {
        return template.queryForObject("SELECT * FROM pgq.finish_batch(?)", Long.class, id) > 0;
    }
}
