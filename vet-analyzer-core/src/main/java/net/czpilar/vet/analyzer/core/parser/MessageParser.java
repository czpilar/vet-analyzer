package net.czpilar.vet.analyzer.core.parser;

import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;

public interface MessageParser<T extends AnalyzerMessage> {

    T parse(String rawData);

    boolean canParse(String rawData);
}
