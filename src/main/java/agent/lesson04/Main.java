package agent.lesson04;

import agent.Cli;
import dev.langchain4j.data.message.AiMessage;

import static agent.lesson04.tools.Tools.*;

public class Main {
    public static void main(String[] args) {
        while (true) {
            String userInput = Cli.ask();
            AiMessage response = Llm.complete(userInput);

            while (response.hasToolExecutionRequests()) {
                response = Llm.complete(run(response.toolExecutionRequests()));
            }

            Cli.reply(response.text());
        }
    }
}
