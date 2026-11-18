package agent.lesson05;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;

import static agent.lesson05.tools.Tools.run;

public class Main {
    public static void main(String[] args) {
        while (true) {
            String userInput = Cli.ask();
            AiMessage response = Llm.complete(userInput);

            while (response.hasToolExecutionRequests()) {
                ToolExecutionRequest firstToolRequest = response.toolExecutionRequests().getFirst();
                response = Llm.complete(run(firstToolRequest));
            }

            Cli.reply(response.text());
        }
    }
}
