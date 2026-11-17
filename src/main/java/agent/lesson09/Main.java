package agent.lesson09;

import dev.langchain4j.data.message.AiMessage;

import static agent.lesson09.tools.Tools.run;

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
