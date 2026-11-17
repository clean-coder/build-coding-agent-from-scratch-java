package agent.lesson08.tools;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class ReadFile {
    static final ToolSpecification SPECIFICATION = ToolSpecification.builder()
            .name("read_file")
            .description("Read a file and return its contents.")
            .parameters(JsonObjectSchema.builder()
                    .addStringProperty("path", "Path of the file to read")
                    .required("path")
                    .build())
            .build();

    private ReadFile() {
    }

    static String run(JsonNode args) throws IOException {
        return Files.readString(Path.of(args.get("path").asText()));
    }
}
