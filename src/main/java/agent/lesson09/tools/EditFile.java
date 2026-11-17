package agent.lesson09.tools;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class EditFile {
    static final ToolSpecification SPECIFICATION = ToolSpecification.builder()
            .name("edit_file")
            .description("Make exact, unique text replacements in a UTF-8 file. "
                         + "Read the file first; after a mismatch, re-read it and submit fresh exact text.")
            .parameters(JsonObjectSchema.builder()
                    .addStringProperty("path", "Path of the file to edit")
                    .addStringProperty("oldText", "Exact text to replace, appearing once in the file")
                    .addStringProperty("newText", "Text to put in its place")
                    .required("path", "oldText", "newText")
                    .build())
            .build();

    private EditFile() {}

    static String run(JsonNode args) throws IOException {
        Path path = Path.of(args.get("path").asText());
        String oldText = args.get("oldText").asText();
        String newText = args.get("newText").asText();
        String before = Files.readString(path);

        int matches = before.split(java.util.regex.Pattern.quote(oldText), -1).length - 1;
        if (matches != 1) {
            return "Found " + matches + " of that text in " + path + "; it has to appear exactly once.";
        }

        Files.writeString(path, before.replace(oldText, newText));
        return "Edited " + path;
    }
}
