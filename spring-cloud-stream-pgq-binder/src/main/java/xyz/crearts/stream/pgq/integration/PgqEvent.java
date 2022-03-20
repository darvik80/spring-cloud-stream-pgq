package xyz.crearts.stream.pgq.integration;

import lombok.Data;

import java.sql.Date;
import java.util.Collections;
import java.util.Map;

@Data
public class PgqEvent {
    private long evId;
    private Date evTime;
    private long eveTxId;
    private Long evRetry;
    private String evType;
    private String evData;
    private Map<String, String> evHeaders = Collections.emptyMap();
}
