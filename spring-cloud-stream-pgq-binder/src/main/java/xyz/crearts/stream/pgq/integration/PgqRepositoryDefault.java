package xyz.crearts.stream.pgq.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
public class PgqRepositoryDefault implements PgqRepository {
    protected final JdbcTemplate template;
    protected final String topic;
    protected final String groupId;

    public PgqRepositoryDefault(JdbcTemplate template, String topic, String groupId) {
        this.template = template;
        this.topic = topic;
        this.groupId = groupId;
    }

    public PgqRepositoryDefault(JdbcTemplate template, String topic) {
        this(template, topic, topic);
    }

    @Override
    public void createQueue() {
        var res = template.queryForObject("SELECT * FROM pgq.create_queue(?)", Long.class, topic);
        if (res != null && res > 0) {
            log.info("create queue {}", topic);
        } else {
            log.info("queue {} already created", topic);
        }
    }

    @Override
    public void registerConsumer() {
        var res = template.queryForObject("SELECT * FROM pgq.register_consumer(?, ?)", Long.class, topic, groupId);
        if (res != null && 0 < res) {
            log.info("repair subscribe for {}:{}", topic, groupId);
        } else {
            log.info("already subscribed for {}:{}", topic, groupId);
        }
    }

    @Override
    public boolean publish(String tag, String data) {
        var res = template.queryForObject("SELECT * FROM pgq.insert_event(?, ?, ?, ?, '', '', '')", Long.class, topic, tag, data, tag);

        return (res != null && res > 0);
    }

    @Override
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

    @Override
    public List<PgqEvent> getNextBatch(Long id) {
        try {
            var events = template.query("SELECT * FROM pgq.get_batch_events(?)", new PgqEventRowMapper(), id);
            events.forEach(item -> {
                item.getEvHeaders().put(PgqHeader.TOPIC, topic);
                item.getEvHeaders().put(PgqHeader.GROUP, groupId);
            });

            return events;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public boolean retry(long id, long evId) {
        var res = template.queryForObject("SELECT * FROM pgq.event_retry(?, ?, 5)", Long.class, id, evId);

        return (res != null && res > 0);
    }

    @Override
    public boolean releaseBatch(Long id) {
        var res = template.queryForObject("SELECT * FROM pgq.finish_batch(?)", Long.class, id);

        return (res != null && res > 0);
    }
}
