package xyz.crearts.stream.pgq.integration;

import java.util.List;

public interface PgqRepository {
    void registerConsumer();
    void createQueue();

    boolean publish(String tag, String data);
    Long getNextId();
    List<PgqEvent> getNextBatch(Long id);

    boolean releaseBatch(Long id);
}
