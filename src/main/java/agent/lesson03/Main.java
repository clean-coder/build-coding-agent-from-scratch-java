package agent.lesson03;

import agent.Cli;
import dev.langchain4j.data.message.AiMessage;

public class Main {
    public static void main(String[] args) {
        while (true) {
            String userInput = Cli.ask();
            AiMessage response = Llm.complete(userInput);
            Cli.reply(response.text());
        }
    }
}