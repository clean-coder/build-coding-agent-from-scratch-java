package agent.lesson02;

import agent.Cli;
import agent.Llm;

public class Main {
    public static void main(String[] args) {
        while (true) {
            String userInput = Cli.ask();
            Cli.reply(Llm.complete(userInput));
        }
    }
}
