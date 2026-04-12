package net.czpilar.vet.analyzer.testclient.command;

final class CommandUtils {

    static final String NL = System.lineSeparator();

    private static final long DEFAULT_DELAY_MS = 300;

    private CommandUtils() {
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
