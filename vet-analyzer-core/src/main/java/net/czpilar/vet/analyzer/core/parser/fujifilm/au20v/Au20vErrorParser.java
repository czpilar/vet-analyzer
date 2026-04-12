package net.czpilar.vet.analyzer.core.parser.fujifilm.au20v;

import net.czpilar.vet.analyzer.core.parser.fujifilm.nx600.Nx600ErrorParser;

/**
 * AU20V E command is identical in format to NX600 E command.
 * This parser exists for type differentiation when explicitly creating AU20V context.
 */
public class Au20vErrorParser extends Nx600ErrorParser {

    // Uses same parsing logic as NX600 E command.
}
