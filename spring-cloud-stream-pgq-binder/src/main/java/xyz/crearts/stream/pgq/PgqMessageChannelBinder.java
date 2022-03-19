package xyz.crearts.stream.pgq;

import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import xyz.crearts.stream.pgq.integration.PgqInboundChannelAdapter;
import xyz.crearts.stream.pgq.integration.PgqProducerMessageHandler;
import xyz.crearts.stream.pgq.properties.PgqConsumerProperties;
import xyz.crearts.stream.pgq.properties.PgqExtendedBindingProperties;
import xyz.crearts.stream.pgq.properties.PgqProducerProperties;

public class PgqMessageChannelBinder extends AbstractMessageChannelBinder<ExtendedConsumerProperties<PgqConsumerProperties>, ExtendedProducerProperties<PgqProducerProperties>, PgqProvisioningProvider>
        implements ExtendedPropertiesBinder<MessageChannel, PgqConsumerProperties, PgqProducerProperties> {
    private final JdbcTemplate template;
    private final PgqExtendedBindingProperties properties;

    public PgqMessageChannelBinder(JdbcTemplate template, PgqProvisioningProvider provider, PgqExtendedBindingProperties properties) {
        super(null, provider);
        this.template = template;
        this.properties = properties;
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
