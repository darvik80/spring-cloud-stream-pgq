package xyz.crearts.stream.pgq.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Slf4j
public class PgqRepositoryCoop extends PgqRepositoryDefault {
    private final String consumerId;

    public PgqRepositoryCoop(JdbcTemplate template, String topic, String groupId, String consumerId) {
        super(template, topic, groupId);

        this.consumerId = consumerId;
    }

    @Override
    public void registerConsumer() {
        var res = template.queryForObject("SELECT * FROM pgq_coop.register_subconsumer(?, ?, ?)", Long.class, topic, groupId, consumerId);
        if (res != null && 0 < res) {
            log.info("repair subscribe for {}:{}:{}", topic, groupId, consumerId);
        } else {
            log.info("already subscribed for {}:{}:{}", topic, groupId, consumerId);
        }
    }

    @Override
    public Long getNextId() {
        var rec = template.queryForRowSet("SELECT * FROM pgq_coop.next_batch(?, ?, ?, '2s')", topic, groupId, consumerId);
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
        var events = super.getNextBatch(id);
        events.forEach(item -> {
            item.getEvHeaders().put(PgqHeader.CONSUMER, consumerId);
        });

        return events;
    }

    @Override
    public boolean releaseBatch(Long id) {
        var res = template.queryForObject("SELECT * FROM pgq_coop.finish_batch(?)", Long.class, id);

        return (res != null && res > 0);
    }
}
