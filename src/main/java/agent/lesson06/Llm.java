package agent.lesson06;

import agent.lesson06.tools.Tools;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.*;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Llm {
    private static final String BASE_URL = env("OLLAMA_BASE_URL", "http://localhost:11434");
    private static final String MODEL_NAME = env("OLLAMA_MODEL", "qwen3:8b");

    private static final ChatModel OLLAMA = OllamaChatModel.builder()
            .baseUrl(BASE_URL)
            .modelName(MODEL_NAME)
            .timeout(Duration.ofMinutes(5))
            .build();

    private static final List<ChatMessage> CONVERSATION = new ArrayList<>();
    private static final ObjectMapper JSON = new ObjectMapper();
    private static int requests = 0;

    static {
        Log.detail("loading tools", json(Tools.SPECIFICATIONS.stream().map(Llm::describe).toList()));
    }

    public static AiMessage complete(String userInput) {
        return send(List.of(UserMessage.from(userInput)));
    }

    static AiMessage complete(List<ToolExecutionResultMessage> toolResults) {
        return send(toolResults);
    }

    private static AiMessage send(List<? extends ChatMessage> messages) {
        try {
            CONVERSATION.addAll(messages);

            int number = ++requests;
            long askedAt = System.currentTimeMillis();

            Log.detail("--> request " + number + ", messages: " + CONVERSATION.size(), json(messages.stream().map(Llm::describe).toList()));

            ChatResponse response;
            try {
                response = OLLAMA.chat(ChatRequest.builder()
                        .messages(CONVERSATION)
                        .toolSpecifications(Tools.SPECIFICATIONS)
                        .build());
            } catch (RuntimeException reason) {
                Log.detail("!!! request " + number + " failed after " + (System.currentTimeMillis() - askedAt) + "ms", failureOf(reason));
                throw reason;
            }

            Log.detail("<-- reply " + number + " after " + (System.currentTimeMillis() - askedAt) + "ms", json(describe(response)));

            CONVERSATION.add(response.aiMessage());
            return response.aiMessage();
        } catch (RuntimeException failure) {
            if (!(rootCauseOf(failure) instanceof ConnectException)) throw failure;
            throw failure;
        }
    }

    private static Map<String, Object> describe(ToolSpecification tool) {
        return Map.of("name", tool.name(), "description", tool.description(),
                "parameters", tool.parameters().properties().keySet());
    }

    private static Map<String, Object> describe(ChatMessage message) {
        return switch (message) {
            case UserMessage user -> Map.of("role", "user", "content", user.singleText());
            case ToolExecutionResultMessage tool -> Map.of("role", "tool", "name", tool.toolName(), "content", tool.text());
            default -> Map.of("role", message.type().name().toLowerCase());
        };
    }

    private static Map<String, Object> describe(ChatResponse response) {
        AiMessage reply = response.aiMessage();

        return Map.of(
                "toolCalls", reply.toolExecutionRequests().stream()
                        .map(call -> Map.of("id", String.valueOf(call.id()), "name", call.name(), "arguments", call.arguments()))
                        .toList(),
                "text", String.valueOf(reply.text()),
                "finishReason", String.valueOf(response.finishReason()));
    }

    private static String json(Object value) {
        try {
            return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String failureOf(RuntimeException reason) {
        StringWriter stack = new StringWriter();
        reason.printStackTrace(new PrintWriter(stack));

        String status = rootCauseOf(reason) instanceof HttpException http ? "status " + http.statusCode() + "\n" : "";

        return status + stack;
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static Throwable rootCauseOf(Throwable failure) {
        return failure.getCause() == null ? failure : rootCauseOf(failure.getCause());
    }
}
