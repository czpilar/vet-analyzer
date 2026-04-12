package net.czpilar.vet.analyzer.core.model.fujifilm;

import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand;

public interface FujifilmMessage extends AnalyzerMessage {

    FujifilmCommand command();

    @Override
    default String messageDescription() {
        return command().name() + " - " + command().description();
    }
}
