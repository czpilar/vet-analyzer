package net.czpilar.vet.analyzer.core.parser.fujifilm.au20v;

import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmStartMessage;
import net.czpilar.vet.analyzer.core.parser.fujifilm.nx600.Nx600StartParser;

/**
 * AU20V S command is identical in format to NX600 S command.
 * This parser exists for type differentiation when explicitly creating AU20V context.
 * In practice, the shared Nx600StartParser handles S commands when analyzer type is ambiguous.
 */
public class Au20vStartParser extends Nx600StartParser {

    // Uses same parsing logic as NX600 S command.
    // The analyzer type would be determined by session context, not by this parser.
}
