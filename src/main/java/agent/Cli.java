package agent;

import java.util.Scanner;

public class Cli {
  private static final Scanner lines = new Scanner(System.in);

  public static String ask() {
    System.out.print("You: ");
    System.out.flush();

    return lines.hasNextLine() ? lines.nextLine() : goodbye();
  }

  public static void reply(String text) {
    System.out.println("Assistant: " + text);
  }

  private static String goodbye() {
    System.exit(0);

    return "";
  }
}
