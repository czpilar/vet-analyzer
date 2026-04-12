package net.czpilar.vet.analyzer.starter.config;

import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener;
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(VetAnalyzerProperties.class)
@ConditionalOnProperty(prefix = "vet.analyzer.server", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VetAnalyzerAutoConfiguration {

    @Bean
    public MessageParserRegistry vetAnalyzerMessageParserRegistry() {
        return MessageParserRegistry.createDefault();
    }

    @Bean
    public VetAnalyzerServerLifecycle vetAnalyzerServerLifecycle(
            VetAnalyzerProperties properties,
            MessageParserRegistry parserRegistry,
            List<AnalyzerMessageListener> listeners) {
        return new VetAnalyzerServerLifecycle(properties, parserRegistry, listeners);
    }
}
