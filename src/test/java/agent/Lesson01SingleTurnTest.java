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

class Lesson01SingleTurnTest {
  @Test
  void answersOnePromptAndExits() throws Exception {
    try (FakeModel model = startFakeModel(says("The capital of France is Paris."))) {
      Session session = runAgent(model, "What is the capital of France?\n");

      assertThat(session.output()).contains("Assistant: The capital of France is Paris.");
      assertThat(session.exitCode()).isZero();
    }
  }

  @Test
  void saysSoWhenOllamaIsNotRunning() throws Exception {
    Session session = runAgent("http://127.0.0.1:1", "hello\n");

    assertThat(session.output()).contains("Ollama");
    assertThat(session.exitCode()).isNotZero();
  }
}
