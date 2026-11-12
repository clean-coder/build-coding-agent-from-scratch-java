package agent.support;

import agent.support.FakeModel.Message;
import agent.support.FakeModel.ModelRequest;
import java.util.List;

public class ToolResults {
  public static List<Message> toolResultsIn(ModelRequest request) {
    return request.messages().stream().filter(message -> message.role().equals("tool")).toList();
  }
}
