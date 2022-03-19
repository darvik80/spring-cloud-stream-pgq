package xyz.crearts.stream.pgq.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

public class PgqSpecificPropertiesProvider implements BinderSpecificPropertiesProvider {

    private PgqConsumerProperties consumer = new PgqConsumerProperties();

    private PgqProducerProperties producer = new PgqProducerProperties();

    @Override
    public PgqConsumerProperties getConsumer() {
        return this.consumer;
    }

    public void setConsumer(PgqConsumerProperties consumer) {
        this.consumer = consumer;
    }

    @Override
    public PgqProducerProperties getProducer() {
        return this.producer;
    }

    public void setProducer(PgqProducerProperties producer) {
        this.producer = producer;
    }
}
