package agent.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

// The vocabulary a test uses to script what the model says back.
public class ModelScript {
  private static final ObjectMapper json = new ObjectMapper();

  public record ToolCall(String name, Map<String, Object> arguments) {}

  public sealed interface Reply permits Says, Calls, Fails {}

  public record Says(String text) implements Reply {}

  public record Calls(List<ToolCall> toolCalls) implements Reply {}

  public record Fails(int status, String error) implements Reply {}

  public static Reply says(String text) {
    return new Says(text);
  }

  public static Reply calls(ToolCall... toolCalls) {
    return new Calls(List.of(toolCalls));
  }

  public static ToolCall tool(String name, Map<String, Object> arguments) {
    return new ToolCall(name, arguments);
  }

  public static Reply fails(int status, String error) {
    return new Fails(status, error);
  }

  static int statusFor(Reply reply) {
    return reply instanceof Fails failure ? failure.status() : 200;
  }

  // Ollama's /api/chat envelope, which is what langchain4j reads back.
  static String completionFor(Reply reply) throws Exception {
    return json.writeValueAsString(
        switch (reply) {
          case Fails failure -> Map.of("error", failure.error());
          case Says spoken -> envelope(Map.of("role", "assistant", "content", spoken.text()));
          case Calls requested ->
              envelope(
                  Map.of(
                      "role", "assistant",
                      "content", "",
                      "tool_calls", requested.toolCalls().stream().map(ModelScript::wireToolCall).toList()));
        });
  }

  private static Map<String, Object> envelope(Map<String, Object> message) {
    return Map.of(
        "model", "fake/model",
        "created_at", "2026-01-01T00:00:00Z",
        "message", message,
        "done", true,
        "done_reason", "stop");
  }

  private static Map<String, Object> wireToolCall(ToolCall call) {
    return Map.of("function", Map.of("name", call.name(), "arguments", call.arguments()));
  }
}
