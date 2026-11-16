package agent.lesson04;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

public final class Log {
    static final Path FILE = Path.of("logs", "session-" + Instant.now().toString().replace(':', '-') + ".log");

    static {
        try {
            Files.createDirectories(FILE.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Log() {
    }

    public static void record(String line) {
        try {
            Files.writeString(FILE, "[" + Instant.now() + "] " + line + "\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Only headlines start at column zero, so an editor folds the evidence and grep indexes it.
    public static void detail(String headline, Object body) {
        record(headline + "\n  " + String.valueOf(body).replace("\n", "\n  "));
    }
}
