package net.czpilar.vet.analyzer.testclient.command;

import java.io.PrintWriter;

final class CommandUtils {

    static final String NL = System.lineSeparator();

    private static final long DEFAULT_DELAY_MS = 300;

    private CommandUtils() {
    }

    static void printAndDelay(PrintWriter writer, String message) {
        writer.println(message);
        writer.flush();
        delay();
    }

    static void delay() {
        delay(DEFAULT_DELAY_MS);
    }

    static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
