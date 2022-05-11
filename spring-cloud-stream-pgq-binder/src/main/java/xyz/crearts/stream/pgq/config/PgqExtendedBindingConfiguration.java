package xyz.crearts.stream.pgq.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.cloud.stream.config.BindingHandlerAdvise;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.crearts.stream.pgq.properties.PgqConsumerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ivan.kishchenko
 */
@Configuration
@EnableConfigurationProperties(PgqConsumerProperties.class)
public class PgqExtendedBindingConfiguration {
    @Bean
    public BindingHandlerAdvise.MappingsProvider pgqExtendedPropertiesDefaultMappingsProvider() {
        return () -> {
            Map<ConfigurationPropertyName, ConfigurationPropertyName> mappings = new HashMap<>();
            mappings.put(
                    ConfigurationPropertyName.of("spring.cloud.stream.pgq.bindings"),
                    ConfigurationPropertyName.of("spring.cloud.stream.pgq.default"));
            mappings.put(
                    ConfigurationPropertyName.of("spring.cloud.stream.pgq.streams"),
                    ConfigurationPropertyName.of("spring.cloud.stream.pgq.streams.default")
            );
            return mappings;
        };
    }
}
