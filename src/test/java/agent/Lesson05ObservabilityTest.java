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

class Lesson05ObservabilityTest {

  private static final Class mainClass = agent.lesson05.Main.class;

  // The log may be pretty-printed or not, so shape is asserted without the whitespace.
  private static String dense(String text) {
    return text.replaceAll("\\s+", "");
  }

  @Test
  void showsEachToolCallAndWritesTheTranscriptToASessionLog() throws Exception {
    Path note = aFileContaining("the shed key is under the mat");

    try (FakeModel model = startFakeModel(
        calls(tool("read_file", Map.of("path", note.toString()))), says("The shed key is under the mat."))) {
      Session session = runAgent(mainClass, model, "where is the shed key?\n");
      String transcript = transcriptOf(session);
      String toolCall = "read_file {\"path\":" + quoted(note.toString()) + "}";

      assertThat(session.output()).contains(toolCall);
      assertThat(transcript).contains("You: where is the shed key?");
      assertThat(transcript).contains(toolCall);
      assertThat(transcript).contains("Assistant: The shed key is under the mat.");
    }
  }

  @Test
  void logsTheRequestItSentTheReplyItGotBackAndWhatTheToolAnswered() throws Exception {
    Path note = aFileContaining("the shed key is under the mat");

    try (FakeModel model = startFakeModel(
        calls(tool("read_file", Map.of("path", note.toString()))), says("The shed key is under the mat."))) {
      Session session = runAgent(mainClass, model, "where is the shed key?\n");
      String transcript = transcriptOf(session);
      int advertised = model.requests().get(0).toolNames().size();

      assertThat(dense(transcript)).contains("\"role\":\"user\"");
      assertThat(dense(transcript)).contains("\"name\":\"read_file\"");
      assertThat(dense(transcript)).contains("\"finishReason\":\"TOOL_EXECUTION\"");
      assertThat(transcript).contains("the shed key is under the mat");
      // Every tool advertised once at startup, not again on the second request.
      assertThat(dense(transcript).split("\"parameters\"", -1)).hasSize(advertised + 1);
    }
  }

  @Test
  void logsWhyTheModelCallFailedBeforeTheAgentDiesOfIt() throws Exception {
    try (FakeModel model = startFakeModel(fails(400, "that model is not serving requests"))) {
      Session session = runAgent(mainClass, model, "where is the shed key?\n");
      String transcript = transcriptOf(session);

      assertThat(transcript).contains("400").contains("that model is not serving requests");
      assertThat(session.exitCode()).isNotZero();
    }
  }

  private static String transcriptOf(Session session) throws Exception {
    return agent.support.SessionLog.transcriptOf(session);
  }

  // Stringified the way JSON would, so a Windows path's backslashes come out escaped.
  private static String quoted(String text) throws Exception {
    return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(text);
  }
}
