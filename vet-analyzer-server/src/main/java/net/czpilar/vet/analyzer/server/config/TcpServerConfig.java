package net.czpilar.vet.analyzer.server.config;

import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener;
import net.czpilar.vet.analyzer.core.session.SessionFileListener;
import net.czpilar.vet.analyzer.server.listener.SessionJsonListener;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableConfigurationProperties(SessionProperties.class)
public class TcpServerConfig {

    @Bean
    public AnalyzerMessageListener sessionFileListener(SessionProperties properties) {
        return new SessionFileListener(properties.getDirectory());
    }

    @Bean
    public AnalyzerMessageListener sessionJsonListener(SessionProperties properties, ObjectMapper mapper) {
        return new SessionJsonListener(properties.getDirectory(), mapper);
    }
}
