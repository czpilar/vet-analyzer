package net.czpilar.vet.analyzer.server.config;

import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener;
import net.czpilar.vet.analyzer.server.session.SessionFileListener;
import net.czpilar.vet.analyzer.starter.config.VetAnalyzerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TcpServerConfig {

    @Bean
    public AnalyzerMessageListener sessionFileListener(VetAnalyzerProperties properties) {
        return new SessionFileListener(properties);
    }
}
