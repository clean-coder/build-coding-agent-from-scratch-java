package agent;

import agent.lesson01.Main;
import agent.support.FakeModel;
import agent.support.RunAgent.Session;
import org.junit.jupiter.api.Test;

import static agent.support.FakeModel.startFakeModel;
import static agent.support.ModelScript.says;
import static agent.support.RunAgent.runAgent;
import static org.assertj.core.api.Assertions.assertThat;

class Lesson01SingleTurnTest {

    private static final Class<Main> mainClass = agent.lesson01.Main.class;

    @Test
    void answersOnePromptAndExits() throws Exception {
        try (FakeModel model = startFakeModel(
                says("The capital of France is Paris."))) {

            Session session = runAgent(mainClass, model, "What is the capital of France?\n");

            assertThat(session.output()).contains("Assistant: The capital of France is Paris.");
            assertThat(session.exitCode()).isZero();
        }
    }

    @Test
    void saysSoWhenOllamaIsNotRunning() throws Exception {
        Session session = runAgent(mainClass, "http://127.0.0.1:1", "hello\n");

        assertThat(session.output()).contains("Ollama");
        assertThat(session.exitCode()).isNotZero();
    }
}
