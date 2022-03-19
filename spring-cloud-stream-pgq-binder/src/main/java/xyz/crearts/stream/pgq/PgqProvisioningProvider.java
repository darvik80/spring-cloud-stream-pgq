package xyz.crearts.stream.pgq;

import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import xyz.crearts.stream.pgq.properties.PgqConsumerProperties;
import xyz.crearts.stream.pgq.properties.PgqProducerProperties;

public class PgqProvisioningProvider implements ProvisioningProvider<ExtendedConsumerProperties<PgqConsumerProperties>, ExtendedProducerProperties<PgqProducerProperties>> {
    @Override
    public ProducerDestination provisionProducerDestination(String name, ExtendedProducerProperties<PgqProducerProperties> properties) throws ProvisioningException {
        return new ProducerDestination() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getNameForPartition(int partition) {
                return name;
            }
        };
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group, ExtendedConsumerProperties<PgqConsumerProperties> properties) throws ProvisioningException {
        return () -> name;
    }
}
