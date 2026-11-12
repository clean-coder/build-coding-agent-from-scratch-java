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

class Lesson02AgentLoopTest {
  @Test
  void answersEveryPromptUntilTheInputEnds() throws Exception {
    try (FakeModel model = startFakeModel(says("Paris."), says("Berlin."))) {
      Session session = runAgent(model, "capital of France?\ncapital of Germany?\n");

      assertThat(session.output()).contains("Paris.").contains("Berlin.");
      assertThat(model.requests()).hasSize(2);
      assertThat(session.exitCode()).isZero();
    }
  }
}
