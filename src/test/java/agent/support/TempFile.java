package agent.support;

import java.nio.file.Files;
import java.nio.file.Path;

public class TempFile {
  // The agent reads real files, so a test that exercises reading has to hand it one.
  public static Path aFileContaining(String text) throws Exception {
    return Files.writeString(Files.createTempDirectory("coding-agent-").resolve("note.txt"), text);
  }
}
