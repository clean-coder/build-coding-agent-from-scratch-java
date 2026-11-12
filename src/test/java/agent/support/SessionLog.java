package agent.support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionLog {
  private static final Pattern LOG_LINE = Pattern.compile("Session log: (\\S+)");

  public static String transcriptOf(RunAgent.Session session) throws Exception {
    Matcher said = LOG_LINE.matcher(session.output());

    if (!said.find()) throw new IllegalStateException("the agent never said where its log is:\n" + session.output());

    return Files.readString(Path.of(said.group(1)));
  }
}
