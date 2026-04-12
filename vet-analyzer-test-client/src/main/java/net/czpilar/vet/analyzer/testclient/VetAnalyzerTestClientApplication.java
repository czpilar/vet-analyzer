package net.czpilar.vet.analyzer.testclient;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.jline.PromptProvider;

@SpringBootApplication
public class VetAnalyzerTestClientApplication {

    static void main(String[] args) {
        SpringApplication.run(VetAnalyzerTestClientApplication.class, args);
    }

    @Bean
    public PromptProvider promptProvider() {
        return () -> new AttributedString("vet:analyzer> ", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }
}
