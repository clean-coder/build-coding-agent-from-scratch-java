package agent.lesson09.tools;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

final class Bash {
    private static final int MAX_CHARACTERS = 8_000;
    private static final int MAX_SECONDS = 30;

    static final ToolSpecification SPECIFICATION = ToolSpecification.builder()
            .name("bash")
            .description("Run a shell command and return its output.")
            .parameters(JsonObjectSchema.builder()
                    .addStringProperty("command", "The command to run")
                    .required("command")
                    .build())
            .build();

    private Bash() {}

    static String run(JsonNode args) throws IOException, InterruptedException {
        Process shell = new ProcessBuilder("bash", "-c", args.get("command").asText())
                .redirectErrorStream(true)
                .start();

        // Read on a separate thread so a command that never ends cannot block the timeout.
        StringBuilder output = new StringBuilder();
        Thread collector = new Thread(() -> collect(shell, output));

        collector.start();

        boolean finished = shell.waitFor(MAX_SECONDS, TimeUnit.SECONDS);
        if (!finished) shell.destroyForcibly().waitFor();

        collector.join();

        return report(output.toString(), finished ? shell.exitValue() : null);
    }

    private static void collect(Process shell, StringBuilder output) {
        try {
            output.append(new String(shell.getInputStream().readAllBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String report(String output, Integer code) {
        return (capped(output) + "\n" + statusOf(code)).trim();
    }

    private static String statusOf(Integer code) {
        return code == null
                ? "Killed: it was still running after " + MAX_SECONDS + " seconds"
                : "Exit code: " + code;
    }

    private static String capped(String output) {
        if (output.length() <= MAX_CHARACTERS) return output;

        return output.substring(0, MAX_CHARACTERS) + "\n[cut off after " + MAX_CHARACTERS + " characters]";
    }
}
