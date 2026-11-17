package agent;

import agent.lesson02.Main;
import agent.support.FakeModel;
import agent.support.RunAgent.Session;
import org.junit.jupiter.api.Test;

import static agent.support.FakeModel.startFakeModel;
import static agent.support.ModelScript.says;
import static agent.support.RunAgent.runAgent;
import static org.assertj.core.api.Assertions.assertThat;

class Lesson02AgentLoopTest {

    private static final Class<Main> mainClass = agent.lesson02.Main.class;

    @Test
    void answersEveryPromptUntilTheInputEnds() throws Exception {
        try (FakeModel model = startFakeModel(
                says("Paris."),
                says("Berlin."))) {

            Session session = runAgent(mainClass, model, "capital of France?\ncapital of Germany?\n");

            assertThat(session.output()).contains("Paris.").contains("Berlin.");
            assertThat(model.requests()).hasSize(2);
            assertThat(session.exitCode()).isZero();
        }
    }
}
