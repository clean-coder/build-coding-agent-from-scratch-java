package agent;

public class Main {
  public static void main(String[] args) {
    String userInput = Cli.ask();

    Cli.reply(Llm.complete(userInput));
  }
}
