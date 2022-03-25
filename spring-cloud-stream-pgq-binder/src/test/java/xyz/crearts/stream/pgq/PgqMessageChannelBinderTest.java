package xyz.crearts.stream.pgq;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import xyz.crearts.stream.pgq.config.PgqExtendedBindingConfiguration;
import xyz.crearts.stream.pgq.config.PgqStreamConfig;
import xyz.crearts.stream.pgq.properties.PgqConsumerProperties;
import xyz.crearts.stream.pgq.properties.PgqProducerProperties;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(classes = PgqMessageChannelBinderTest.TestConfig.class,
        webEnvironment = NONE,
        properties = {
                "spring.cloud.stream.bindings.input1.destination=topic",
                "spring.cloud.stream.bindings.input1.content-type=application/json",
        })
class PgqMessageChannelBinderTest {
    @MockBean
    JdbcTemplate template;
    @Autowired
    PgqMessageChannelBinder binder;

    @BeforeEach
    void setup() {

    }

    @Test
    void createProducerMessageHandler() throws Exception {
        var handler = binder.createProducerMessageHandler(new ProducerDestination() {
            @Override
            public String getName() {
                return "topic";
            }

            @Override
            public String getNameForPartition(int partition) {
                return "topic";
            }
        }, new ExtendedProducerProperties<>(new PgqProducerProperties()), null);

        Assertions.assertNotNull(handler);
    }

    @Test
    void createConsumerEndpoint() throws Exception {
        var endpoint = binder.createConsumerEndpoint(
                () -> "topic", "test",
                new ExtendedConsumerProperties<>(new PgqConsumerProperties())
        );

        Assertions.assertNotNull(endpoint);
    }

    @Test
    void createPolledConsumerResources() {
        var resources = binder.createPolledConsumerResources(
                "test",
                "test",
                () -> "topic",
                new ExtendedConsumerProperties<>(new PgqConsumerProperties())
        );

        Assertions.assertNotNull(resources);
    }

    @Configuration
    @EnableAutoConfiguration(exclude =
            {
                    DataSourceAutoConfiguration.class,
                    DataSourceTransactionManagerAutoConfiguration.class,
                    HibernateJpaAutoConfiguration.class
            }
    )
    @ImportAutoConfiguration(
            {
                    PgqExtendedBindingConfiguration.class,
                    PgqStreamConfig.class
            }
    )
    public static class TestConfig {

    }
}