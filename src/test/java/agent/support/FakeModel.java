package agent.support;

import agent.support.ModelScript.Reply;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public class FakeModel implements AutoCloseable {
  private static final ObjectMapper json = new ObjectMapper();

  public record Message(String role, String content, String toolName) {}

  public record ModelRequest(String model, List<Message> messages, List<String> toolNames) {}

  private final List<ModelRequest> requests = new ArrayList<>();
  private final HttpServer server;

  // The agent under test is a black box: it is told a base URL and nothing else.
  public static FakeModel startFakeModel(Reply... script) throws Exception {
    return new FakeModel(List.of(script));
  }

  private FakeModel(List<Reply> script) throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/api/chat", exchange -> answer(exchange, script));
    server.start();
  }

  public String url() {
    return "http://127.0.0.1:" + server.getAddress().getPort();
  }

  public List<ModelRequest> requests() {
    return requests;
  }

  @Override
  public void close() {
    server.stop(0);
  }

  private synchronized void answer(HttpExchange exchange, List<Reply> script) {
    try (exchange) {
      requests.add(parse(json.readTree(exchange.getRequestBody())));

      if (requests.size() > script.size()) offScript(exchange);
      else send(exchange, ModelScript.statusFor(script.get(requests.size() - 1)), ModelScript.completionFor(script.get(requests.size() - 1)));
    } catch (Exception failure) {
      throw new RuntimeException(failure);
    }
  }

  // Running past the end of the script is a test bug, so it must be loud rather than blank.
  private void offScript(HttpExchange exchange) throws Exception {
    String message = "fake model was asked " + requests.size() + " times but the script has fewer replies";

    send(exchange, 500, json.writeValueAsString(java.util.Map.of("error", message)));
  }

  private static void send(HttpExchange exchange, int status, String body) throws Exception {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(status, bytes.length);
    exchange.getResponseBody().write(bytes);
  }

  private static ModelRequest parse(JsonNode body) {
    return new ModelRequest(
        body.path("model").asText(),
        StreamSupport.stream(body.path("messages").spliterator(), false)
            .map(m -> new Message(m.path("role").asText(), m.path("content").asText(), m.path("tool_name").asText(null)))
            .toList(),
        StreamSupport.stream(body.path("tools").spliterator(), false)
            .map(t -> t.path("function").path("name").asText())
            .toList());
  }
}
