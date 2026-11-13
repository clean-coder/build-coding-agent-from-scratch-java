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

class Lesson10CodingSystemPromptTest {

  private static final Class mainClass = agent.lesson01.Main.class;

  @Test
  void putsASystemPromptInFrontOfTheConversation() throws Exception {
    try (FakeModel model = startFakeModel(says("Read first, then edit."))) {
      Session session = runAgent(mainClass, model, "how do you work?\n");
      var sent = model.requests().get(0).messages();

      assertThat(session.output()).contains("Assistant: Read first, then edit.");
      assertThat(sent.get(0).role()).isEqualTo("system");
      assertThat(sent.get(0).content()).isNotEmpty();
      assertThat(sent.get(1).role()).isEqualTo("user");
      assertThat(sent.get(1).content()).isEqualTo("how do you work?");
    }
  }

  @Test
  void sendsTheOnePromptOnEveryRequestHoweverTheTurnGoes() throws Exception {
    try (FakeModel model = startFakeModel(
        calls(tool("bash", Map.of("command", "echo hello"))), says("It printed hello."), says("Yes, hello."))) {
      Session session = runAgent(mainClass, model, "run that echo\nwhat did it print?\n");
      var last = model.requests().get(2).messages();

      assertThat(session.output()).contains("Assistant: Yes, hello.");
      assertThat(model.requests()).allSatisfy(request -> assertThat(request.messages().get(0).role()).isEqualTo("system"));
      assertThat(last.stream().filter(message -> message.role().equals("system"))).hasSize(1);
    }
  }
}
