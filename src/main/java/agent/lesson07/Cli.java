package agent.lesson07;

import java.util.Scanner;

public class Cli {
    private static final Scanner lines = new Scanner(System.in);

    static {
        System.out.println("Session log: " + Log.FILE);
    }

    public static String ask() {
        System.out.print("You: ");
        System.out.flush();
        return lines.hasNextLine() ? heard(lines.nextLine()) : goodbye();
    }

    private static String heard(String userInput) {
        Log.record("You: " + userInput);
        return userInput;
    }

    public static void reply(String text) {
        say("Assistant: " + text);
    }

    private static String goodbye() {
        System.exit(0);
        return "";
    }

    public static void using(String name, String args) {
        say("→ " + name + " " + args);
    }

    private static void say(String line) {
        System.out.println(line);
        Log.record(line);
    }

}

