package xyz.crearts.stream.pgq.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import xyz.crearts.stream.pgq.PgqMessageChannelBinder;
import xyz.crearts.stream.pgq.PgqProvisioningProvider;
import xyz.crearts.stream.pgq.properties.PgqExtendedBindingProperties;

@Configuration
@EnableConfigurationProperties(PgqExtendedBindingProperties.class)
public class PgqStreamConfig {
    @Bean
    public PgqProvisioningProvider pgqProvisioningProvider() {
        return new PgqProvisioningProvider();
    }

    @Bean
    @DependsOnDatabaseInitialization
    public PgqMessageChannelBinder pgqMessageChannelBinder(JdbcTemplate template, PgqProvisioningProvider provider, PgqExtendedBindingProperties properties) {
        return new PgqMessageChannelBinder(template, provider, properties);
    }


}
