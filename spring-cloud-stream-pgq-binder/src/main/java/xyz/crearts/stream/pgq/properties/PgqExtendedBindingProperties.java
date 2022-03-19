package xyz.crearts.stream.pgq.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.AbstractExtendedBindingProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

@ConfigurationProperties("spring.cloud.stream.pgq")
public class PgqExtendedBindingProperties extends AbstractExtendedBindingProperties<PgqConsumerProperties, PgqProducerProperties, PgqSpecificPropertiesProvider> {

    private static final String DEFAULTS_PREFIX = "spring.cloud.stream.pgq.default";

    @Override
    public String getDefaultsPrefix() {
        return DEFAULTS_PREFIX;
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return PgqSpecificPropertiesProvider.class;
    }
}
