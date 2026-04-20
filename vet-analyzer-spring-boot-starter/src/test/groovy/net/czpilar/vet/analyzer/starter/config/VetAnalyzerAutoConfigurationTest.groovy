package net.czpilar.vet.analyzer.starter.config

import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener
import net.czpilar.vet.analyzer.core.model.AnalyzerMessage
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import spock.lang.Specification

class VetAnalyzerAutoConfigurationTest extends Specification {

    def runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VetAnalyzerAutoConfiguration))

    def "auto-configuration creates beans when enabled"() {
        expect:
        runner.withPropertyValues("vet.analyzer.server.port=19012", "vet.analyzer.server.auto-start=false")
                .withBean(AnalyzerMessageListener, { new TestListener() })
                .run { context ->
                    assert context.containsBean("vetAnalyzerMessageParserRegistry")
                    assert context.containsBean("vetAnalyzerServerLifecycle")
                    assert context.getBean(MessageParserRegistry) != null
                    assert context.getBean(VetAnalyzerServerLifecycle) != null
                }
    }

    def "auto-configuration respects enabled=false"() {
        expect:
        runner.withPropertyValues("vet.analyzer.server.enabled=false")
                .run { context ->
                    assert !context.containsBean("vetAnalyzerServerLifecycle")
                    assert !context.containsBean("vetAnalyzerMessageParserRegistry")
                }
    }

    def "properties are bound correctly for port=#port, idleTimeout=#idleTimeout"() {
        expect:
        runner.withPropertyValues(
                    "vet.analyzer.server.port=${port}",
                    "vet.analyzer.server.idle-timeout-seconds=${idleTimeout}",
                    "vet.analyzer.server.auto-start=false"
                )
                .withBean(AnalyzerMessageListener, { new TestListener() })
                .run { context ->
                    def props = context.getBean(VetAnalyzerProperties)
                    assert props.port == port
                    assert props.idleTimeoutSeconds == idleTimeout
                }

        where:
        port  | idleTimeout
        19012 | 60
        15050 | 120
        18888 | 0
    }

    static class TestListener implements AnalyzerMessageListener {
        @Override
        void onMessage(AnalyzerMessage message, String rawData, String remoteAddress) {}
    }
}
