package xyz.crearts.stream.pgq.integration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ObjectUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PgqInboundChannelAdapter extends MessageProducerSupport implements Runnable {
    private final JdbcTemplate template;
    private final String topic;
    private final String groupId;

    private final ScheduledExecutorService executor;

    @Data
    static class Event {
        private long evId;
        private Date evTime;
        private long eveTxId;
        private Long evRetry;
        private String evType;
        private String evData;
        private Map<String, String> evHeaders = Collections.emptyMap();
    }

    static class EventRowMapper implements RowMapper<Event> {
        @Override
        public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
            Event res = new Event();

            res.setEvId(rs.getLong("ev_id"));
            res.setEvTime(rs.getDate("ev_time"));
            res.setEveTxId(rs.getLong("ev_txid"));
            res.setEvRetry(rs.getLong("ev_retry"));
            res.setEvType(rs.getString("ev_type"));
            res.setEvData(rs.getString("ev_data"));

            var evHeaders = new HashMap<String, String>();
            var extra = rs.getString("ev_extra1");
            if (extra != null) {
                evHeaders.put("TAGS", extra);
            }
            if (!evHeaders.isEmpty()) {
                res.setEvHeaders(evHeaders);
            }

            return res;
        }
    }

    public PgqInboundChannelAdapter(JdbcTemplate template, String topic, String groupId) {
        this.template = template;
        this.topic = topic;
        this.groupId = groupId;

        if (0 < template.queryForObject("SELECT * FROM pgq.register_consumer(?, ?)", Long.class, topic, groupId)) {
            log.info("repair subscribe for {}:{}", topic, groupId);
        } else {
            log.info("already subscribed for {}:{}", topic, groupId);
        }

        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(this, 0, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        try {
            while (true) {
                var rec = template.queryForRowSet("SELECT * FROM pgq.next_batch(?, ?)", topic, groupId);
                if (rec.next()) {
                    var id = rec.getLong(1);
                    if (id == 0) {
                        break;
                    }
                    log.info("got batch: {}", id);
                    var msgs = template.query("SELECT * FROM pgq.get_batch_events(?)", new EventRowMapper(), id);
                    for (var msg : msgs) {
                        this.sendMessage(
                                MessageBuilder.withPayload(msg.evData)
                                        .setHeader("TAG", msg.getEvHeaders().get("TAG"))
                                        .setHeader("TOPIC", topic)
                                        .build()
                        );

                    }
                    template.queryForObject("SELECT * FROM pgq.finish_batch(?)", Long.class, id);
                    log.info("processed batch: {}, size: {}", id, msgs.size());
                } else {
                    log.info("ignore batch");
                    break;
                }
            }
        } catch (Exception ex) {
            log.warn("can't process pgp messages", ex);
        }
    }
}
