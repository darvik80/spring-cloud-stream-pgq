package xyz.crearts.stream.pgq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import xyz.crearts.stream.pgq.integration.PgqInboundChannelAdapter;
import xyz.crearts.stream.pgq.integration.PgqMessageSource;
import xyz.crearts.stream.pgq.integration.PgqProducerMessageHandler;
import xyz.crearts.stream.pgq.properties.PgqConsumerProperties;
import xyz.crearts.stream.pgq.properties.PgqExtendedBindingProperties;
import xyz.crearts.stream.pgq.properties.PgqProducerProperties;

@Slf4j
public class PgqMessageChannelBinder extends AbstractMessageChannelBinder<ExtendedConsumerProperties<PgqConsumerProperties>, ExtendedProducerProperties<PgqProducerProperties>, PgqProvisioningProvider>
        implements ExtendedPropertiesBinder<MessageChannel, PgqConsumerProperties, PgqProducerProperties> {
    private final JdbcTemplate template;
    private final PgqExtendedBindingProperties properties;

    public PgqMessageChannelBinder(JdbcTemplate template, PgqProvisioningProvider provider, PgqExtendedBindingProperties properties) {
        super(null, provider);
        this.template = template;
        this.properties = properties;

        try {
            template.execute("create extension if not exists pgq");
            template.execute("create extension if not exists pgq_coop");
        } catch (Exception ex) {
            log.warn("\033[1;31mPlease follow to instruction in \033[38;5;15mREADME.md\033[1;31m for setup pgq & pgq_coop extensions");
            throw new RuntimeException("Please follow to instruction in README.md for setup pgq & pgq_coop extensions", ex);
        }
    }

    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination, ExtendedProducerProperties<PgqProducerProperties> producerProperties, MessageChannel errorChannel) throws Exception {
        return new PgqProducerMessageHandler(template, destination.getName());
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group, ExtendedConsumerProperties<PgqConsumerProperties> properties) throws Exception {
        return new PgqInboundChannelAdapter(template, destination.getName(), group);
    }

    @Override
    protected PolledConsumerResources createPolledConsumerResources(String name, String group, ConsumerDestination destination, ExtendedConsumerProperties<PgqConsumerProperties> consumerProperties) {
        return new PolledConsumerResources(
                new PgqMessageSource(template, destination.getName(), group),
                registerErrorInfrastructure(destination, group, consumerProperties, true)
        );
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return this.properties.getExtendedPropertiesEntryClass();
    }

    @Override
    public PgqConsumerProperties getExtendedConsumerProperties(String channelName) {
        return properties.getExtendedConsumerProperties(channelName);
    }

    @Override
    public PgqProducerProperties getExtendedProducerProperties(String channelName) {
        return properties.getExtendedProducerProperties(channelName);
    }

    @Override
    public String getDefaultsPrefix() {
        return properties.getDefaultsPrefix();
    }
}
