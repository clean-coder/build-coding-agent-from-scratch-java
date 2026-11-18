package agent.lesson05.tools;

import agent.lesson05.Cli;
import agent.lesson05.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ToolExecutionResultMessage;

import java.util.List;
import java.util.Map;

public final class Tools {
    @FunctionalInterface
    private interface Runner {
        String run(JsonNode args) throws Exception;
    }

    private static final ObjectMapper JSON = new ObjectMapper();

    public static final List<ToolSpecification> SPECIFICATIONS =
            List.of(ReadFile.SPECIFICATION);

    private static final Map<String, Runner> RUNNERS =
            Map.of("read_file", ReadFile::run);

    private Tools() {
    }

    public static ToolExecutionResultMessage run(ToolExecutionRequest toolCall) {
        Cli.using(toolCall.name(), toolCall.arguments());

        long startedAt = System.currentTimeMillis();
        String output = outputOf(toolCall);

        Log.detail("<-- " + toolCall.name() + " answered after " + (System.currentTimeMillis() - startedAt) + "ms", output);

        return ToolExecutionResultMessage.from(toolCall, output);
    }

    private static String outputOf(ToolExecutionRequest toolCall) {
        Runner runner = RUNNERS.get(toolCall.name());

        if (runner == null) return "There is no tool called " + toolCall.name() + ".";

        try {
            return runner.run(JSON.readTree(toolCall.arguments()));
        } catch (Exception reason) {
            return String.valueOf(reason);
        }
    }
}
