package net.czpilar.vet.analyzer.server.config;

import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener;
import net.czpilar.vet.analyzer.core.session.SessionFileListener;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SessionProperties.class)
public class TcpServerConfig {

    @Bean
    public AnalyzerMessageListener sessionFileListener(SessionProperties properties) {
        return new SessionFileListener(properties.getDirectory());
    }
}
