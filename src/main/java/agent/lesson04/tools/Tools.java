package agent.lesson04.tools;

import agent.lesson04.Cli;
import agent.lesson04.Log;
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

    public static List<ToolExecutionResultMessage> run(List<ToolExecutionRequest> toolCalls) {
        return toolCalls.parallelStream().map(Tools::runOne).toList();
    }

    private static ToolExecutionResultMessage runOne(ToolExecutionRequest call) {
        Cli.using(call.name(), call.arguments());

        long startedAt = System.currentTimeMillis();
        String output = outputOf(call);

        Log.detail("<-- " + call.name() + " answered after " + (System.currentTimeMillis() - startedAt) + "ms", output);

        return ToolExecutionResultMessage.from(call, output);
    }

    private static String outputOf(ToolExecutionRequest call) {
        Runner runner = RUNNERS.get(call.name());

        if (runner == null) return "There is no tool called " + call.name() + ".";

        try {
            return runner.run(JSON.readTree(call.arguments()));
        } catch (Exception reason) {
            return String.valueOf(reason);
        }
    }
}
