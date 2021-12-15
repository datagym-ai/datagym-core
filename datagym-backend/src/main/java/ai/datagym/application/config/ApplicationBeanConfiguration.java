package ai.datagym.application.config;

import ai.datagym.application.dummy.models.bindingModels.labelIteration.DummyValueUpdateBindingModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.apache.tika.Tika;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationBeanConfiguration {
    private static ModelMapper mapper;

    @Value("${management.application.name}")
    private String applicationName;

    static {
        mapper = new ModelMapper();
        DummyValueUpdateBindingModel.initMap(mapper);
    }

    @Bean
    public ModelMapper modelMapper(){
        return mapper;
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }


    @Bean
    public Tika tika(){
        return new Tika();
    }

    /**
     * Global configuration of Tasks, that are common for all Metrics
     **/
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> registryCustomizer(){
        return registry -> registry
                .config()
                .commonTags("app.name", applicationName)
                .meterFilter(MeterFilter.ignoreTags("eforce21"));
    }
}
