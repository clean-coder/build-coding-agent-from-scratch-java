package agent.support;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

public class RunAgent {
  public record Session(String output, int exitCode) {}

  public static Session runAgent(Class mainClass, FakeModel model, String input) throws Exception {
    return runAgent(mainClass, model.url(), input);
  }

  // Spawned rather than called, so the test says nothing about how the agent is built.
  public static Session runAgent(Class mainClass, String baseUrl, String input) throws Exception {
    String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
    ProcessBuilder agent = new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), mainClass.getName());

    agent.directory(new File(System.getProperty("user.dir")));
    agent.redirectErrorStream(true);
    agent.environment().putAll(Map.of("OLLAMA_BASE_URL", baseUrl, "OLLAMA_MODEL", "fake/model"));

    Process running = agent.start();

    running.getOutputStream().write(input.getBytes(StandardCharsets.UTF_8));
    running.getOutputStream().close();

    String output = new String(running.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

    return new Session(output, running.waitFor());
  }
}
