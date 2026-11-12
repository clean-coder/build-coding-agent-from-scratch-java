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

class Lesson04ReadFileTest {
  @Test
  void runsTheToolCallTheModelAsksForAndAnswersFromTheContents() throws Exception {
    Path note = aFileContaining("the shed key is under the mat");

    try (FakeModel model = startFakeModel(
        calls(tool("read_file", Map.of("path", note.toString()))), says("The shed key is under the mat."))) {
      Session session = runAgent(model, "where is the shed key?\n");
      var offered = model.requests().get(0).toolNames();
      var sentBack = toolResultsIn(model.requests().get(1));

      assertThat(session.output()).contains("Assistant: The shed key is under the mat.");
      assertThat(offered).first().isEqualTo("read_file");
      assertThat(sentBack.get(0).content()).isEqualTo("the shed key is under the mat");
    }
  }
}
