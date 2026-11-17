package agent;

import agent.lesson03.Main;
import agent.support.FakeModel;
import agent.support.RunAgent.Session;
import org.junit.jupiter.api.Test;

import static agent.support.FakeModel.startFakeModel;
import static agent.support.ModelScript.says;
import static agent.support.RunAgent.runAgent;
import static org.assertj.core.api.Assertions.assertThat;

class Lesson03ConversationTest {

    private static final Class<Main> mainClass = agent.lesson03.Main.class;

    @Test
    void sendsEveryEarlierMessageBackWithTheNextPrompt() throws Exception {
        try (FakeModel model = startFakeModel(
                says("Nice to meet you, Steven."),
                says("Your name is Steven."))) {

            Session session = runAgent(mainClass, model, "My name is Steven.\nWhat is my name?\n");
            var resent = model.requests().get(1).messages().stream().filter(m -> !m.role().equals("system")).toList();

            assertThat(session.output()).contains("Assistant: Your name is Steven.");
            assertThat(resent).hasSize(3);
            assertThat(resent.get(0).content()).isEqualTo("My name is Steven.");
            assertThat(resent.get(1).content()).isEqualTo("Nice to meet you, Steven.");
            assertThat(resent.get(2).content()).isEqualTo("What is my name?");
        }
    }
}
