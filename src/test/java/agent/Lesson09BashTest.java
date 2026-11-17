package agent;

import agent.lesson09.Main;
import agent.support.FakeModel;
import agent.support.RunAgent.Session;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static agent.support.FakeModel.startFakeModel;
import static agent.support.ModelScript.*;
import static agent.support.RunAgent.runAgent;
import static agent.support.ToolResults.toolResultsIn;
import static org.assertj.core.api.Assertions.assertThat;

class Lesson09BashTest {

    private static final Class<Main> mainClass = agent.lesson09.Main.class;

    @Test
    void runsTheCommandAndHandsTheOutputBackToTheModel() throws Exception {
        try (FakeModel model = startFakeModel(
                calls(tool("bash", Map.of("command", "echo the-shed-key-is-under-the-mat"))),
                says("It printed the-shed-key-is-under-the-mat."))) {

            Session session = runAgent(mainClass, model, "run that echo\n");
            var offered = model.requests().get(0).toolNames();
            var sentBack = toolResultsIn(model.requests().get(1));

            assertThat(session.output()).contains("Assistant: It printed the-shed-key-is-under-the-mat.");
            assertThat(offered).contains("bash");
            assertThat(sentBack.get(0).content()).contains("the-shed-key-is-under-the-mat");
        }
    }

    @Test
    void answersAFailingCommandWithWhatItPrintedAndTheExitCode() throws Exception {
        try (FakeModel model = startFakeModel(
                calls(tool("bash", Map.of("command", "echo no-such-target >&2; exit 3"))),
                says("That target is missing."))) {

            Session session = runAgent(mainClass, model, "run the build\n");
            var sentBack = toolResultsIn(model.requests().get(1));

            assertThat(session.output()).contains("Assistant: That target is missing.");
            assertThat(sentBack.get(0).content()).contains("no-such-target").contains("Exit code: 3");
        }
    }

    @Test
    void cutsOffAnOutputFarTooBigToSend() throws Exception {
        try (FakeModel model = startFakeModel(
                calls(tool("bash", Map.of("command", "head -c 40000 /dev/zero | tr '\\0' x"))),
                says("That was a wall of x."))) {

            Session session = runAgent(mainClass, model, "print the whole log\n");
            var sentBack = toolResultsIn(model.requests().get(1));

            assertThat(session.output()).contains("Assistant: That was a wall of x.");
            assertThat(sentBack.get(0).content().length()).isLessThan(40000);
            assertThat(sentBack.get(0).content()).contains("cut off");
        }
    }

    @Test
    void answersAToolThatThrowsInsteadOfDyingWithIt() throws Exception {
        try (FakeModel model = startFakeModel(
                calls(tool("read_file", Map.of("path", "no/such/file.txt"))),
                says("There is no file at that path."))) {

            Session session = runAgent(mainClass, model, "read no/such/file.txt\n");
            var sentBack = toolResultsIn(model.requests().get(1));

            assertThat(session.output()).contains("Assistant: There is no file at that path.");
            assertThat(sentBack.get(0).content()).contains("NoSuchFileException");
            assertThat(session.exitCode()).isZero();
        }
    }
}
