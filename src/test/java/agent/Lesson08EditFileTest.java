package agent;

import static agent.support.FakeModel.startFakeModel;
import static agent.support.ModelScript.*;
import static agent.support.RunAgent.runAgent;
import static agent.support.TempFile.aFileContaining;
import static agent.support.ToolResults.toolResultsIn;
import static org.assertj.core.api.Assertions.assertThat;

import agent.support.FakeModel;
import agent.support.RunAgent.Session;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Lesson08EditFileTest {

  private static final Class mainClass = agent.lesson01.Main.class;

  @Test
  void appliesTheEditTheModelAsksFor() throws Exception {
    Path note = aFileContaining("the shed key is under the mat");

    try (FakeModel model = startFakeModel(
        calls(tool("edit_file", Map.of("path", note.toString(), "oldText", "under the mat", "newText", "in the drawer"))),
        says("The key is in the drawer now."))) {
      Session session = runAgent(mainClass, model, "the key moved to the drawer\n");
      var offered = model.requests().get(0).toolNames();

      assertThat(session.output()).contains("Assistant: The key is in the drawer now.");
      assertThat(offered).contains("edit_file");
      assertThat(Files.readString(note)).isEqualTo("the shed key is in the drawer");
    }
  }

  @Test
  void changesNothingWhenTheTextToReplaceIsNotUniqueAndSaysSo() throws Exception {
    Path note = aFileContaining("the key, and the spare key");

    try (FakeModel model = startFakeModel(
        calls(tool("edit_file", Map.of("path", note.toString(), "oldText", "key", "newText", "fob"))),
        calls(tool("edit_file", Map.of("path", note.toString(), "oldText", "spare key", "newText", "spare fob"))),
        says("The spare one, then."))) {
      Session session = runAgent(mainClass, model, "call the spare key a fob\n");

      assertThat(session.output()).contains("Assistant: The spare one, then.");
      assertThat(Files.readString(note)).isEqualTo("the key, and the spare fob");
    }
  }

  @Test
  void answersAToolItDoesNotHaveInsteadOfFallingOver() throws Exception {
    try (FakeModel model = startFakeModel(
        calls(tool("delete_file", Map.of("path", "anything.txt"))), says("No such tool, understood."))) {
      Session session = runAgent(mainClass, model, "delete anything.txt\n");
      var sentBack = toolResultsIn(model.requests().get(1));

      assertThat(session.output()).contains("Assistant: No such tool, understood.");
      assertThat(sentBack).hasSize(1);
    }
  }
}
