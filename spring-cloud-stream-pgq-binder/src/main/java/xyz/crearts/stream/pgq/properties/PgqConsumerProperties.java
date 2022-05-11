package xyz.crearts.stream.pgq.properties;

import java.util.Set;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = PgqExtendedBindingProperties.DEFAULTS_PREFIX_CONSUMER)
public class PgqConsumerProperties{
    private String consumerId;
    private Set<String> tags;
    private long pullDelay = 1000;
}
