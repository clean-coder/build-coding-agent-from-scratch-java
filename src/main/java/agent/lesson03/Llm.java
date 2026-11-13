package agent.lesson03;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

public class Llm {
  private static final String baseUrl = env("OLLAMA_BASE_URL", "http://localhost:11434");
  private static final String modelName = env("OLLAMA_MODEL", "qwen3:8b");

  private static final List<ChatMessage> CONVERSATION = new ArrayList<>();

  private static final ChatModel model =
      OllamaChatModel.builder().baseUrl(baseUrl).modelName(modelName).maxRetries(0).build();

  public static AiMessage complete(String userInput) {
    return complete(UserMessage.from(userInput));
  }

  private static AiMessage complete(UserMessage userInput) {
    try {
      CONVERSATION.add(userInput);
      ChatResponse response = model.chat(CONVERSATION);
      CONVERSATION.add(response.aiMessage());
      return response.aiMessage();
    } catch (RuntimeException failure) {
      if (!(rootCauseOf(failure) instanceof ConnectException)) throw failure;

      System.err.println("Could not reach Ollama at " + baseUrl + ". Start it with: ollama serve");
      throw failure;
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
