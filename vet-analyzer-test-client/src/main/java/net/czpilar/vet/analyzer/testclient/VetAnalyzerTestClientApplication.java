package net.czpilar.vet.analyzer.testclient;

import net.czpilar.vet.analyzer.testclient.command.Au20vCommands;
import net.czpilar.vet.analyzer.testclient.command.Hl7Commands;
import net.czpilar.vet.analyzer.testclient.command.Nx600Commands;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.core.command.annotation.EnableCommand;

@SpringBootApplication
@EnableCommand({Hl7Commands.class, Nx600Commands.class, Au20vCommands.class})
public class VetAnalyzerTestClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(VetAnalyzerTestClientApplication.class, args);
    }
}
