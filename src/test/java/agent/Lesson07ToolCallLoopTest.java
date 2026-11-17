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

class Lesson07ToolCallLoopTest {

  private static final Class mainClass = agent.lesson07.Main.class;

  @Test
  void keepsRunningToolCallsUntilAReplyComesBackWithoutAny() throws Exception {
    Path shed = aFileContaining("the shed key is under the mat");
    Path bike = aFileContaining("the bike lock is 1234");

    try (FakeModel model = startFakeModel(
        calls(tool("read_file", Map.of("path", shed.toString()))),
        calls(tool("read_file", Map.of("path", bike.toString()))),
        says("Key under the mat, lock 1234."))) {

      Session session = runAgent(mainClass, model, "read my notes one at a time\n");
      var sentBack = toolResultsIn(model.requests().get(2));

      assertThat(session.output()).contains("Assistant: Key under the mat, lock 1234.");
      assertThat(model.requests()).hasSize(3);
      assertThat(sentBack).hasSize(2);
      assertThat(sentBack.get(1).content()).isEqualTo("the bike lock is 1234");
    }
  }
}
