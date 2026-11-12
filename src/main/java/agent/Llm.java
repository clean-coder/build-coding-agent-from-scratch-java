package agent;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.net.ConnectException;

public class Llm {
  private static final String baseUrl = env("OLLAMA_BASE_URL", "http://localhost:11434");
  private static final String modelName = env("OLLAMA_MODEL", "qwen3:8b");

  private static final ChatModel model =
      OllamaChatModel.builder().baseUrl(baseUrl).modelName(modelName).maxRetries(0).build();

  public static String complete(String userInput) {
    try {
      return model.chat(userInput);
    } catch (RuntimeException failure) {
      if (!(rootCauseOf(failure) instanceof ConnectException)) throw failure;

      System.err.println("Could not reach Ollama at " + baseUrl + ". Start it with: ollama serve");
      System.exit(1);

      return "";
    }
  }

  private static String env(String name, String fallback) {
    String value = System.getenv(name);

    return value == null || value.isBlank() ? fallback : value;
  }

  private static Throwable rootCauseOf(Throwable failure) {
    return failure.getCause() == null ? failure : rootCauseOf(failure.getCause());
  }
}
