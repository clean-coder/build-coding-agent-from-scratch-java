package agent.lesson01;

import agent.Cli;
import agent.Llm;

public class Main {
  public static void main(String[] args) {
    String userInput = Cli.ask();

    Cli.reply(Llm.complete(userInput));
  }
}
