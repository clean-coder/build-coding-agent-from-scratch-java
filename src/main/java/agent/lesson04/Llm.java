package agent.lesson04;

import agent.lesson04.tools.Tools;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.*;
import java.net.ConnectException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Llm {
    private static final String BASE_URL = env("OLLAMA_BASE_URL", "http://localhost:11434");
    private static final String MODEL_NAME = env("OLLAMA_MODEL", "qwen3:8b");

    private static final ChatModel OLLAMA = OllamaChatModel.builder()
            .baseUrl(BASE_URL)
            .modelName(MODEL_NAME)
            .timeout(Duration.ofMinutes(5))
            .build();

    private static final List<ChatMessage> CONVERSATION = new ArrayList<>(List.of(SystemMessage.from(Prompt.CODING)));

    public static AiMessage complete(String userInput) {
        return send(List.of(UserMessage.from(userInput)));
    }

    static AiMessage complete(List<ToolExecutionResultMessage> toolResults) {
        return send(toolResults);
    }

    private static AiMessage send(List<? extends ChatMessage> messages) {
        try {
            CONVERSATION.addAll(messages);

            ChatResponse response;
            try {
                response = OLLAMA.chat(ChatRequest.builder()
                        .messages(CONVERSATION)
                        .toolSpecifications(Tools.SPECIFICATIONS)
                        .build());
            } catch (RuntimeException reason) {
                throw reason;
            }

            CONVERSATION.add(response.aiMessage());
            return response.aiMessage();
        } catch (RuntimeException failure) {
            if (!(rootCauseOf(failure) instanceof ConnectException)) throw failure;
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
