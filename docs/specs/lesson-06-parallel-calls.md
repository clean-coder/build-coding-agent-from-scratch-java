# Parallel calls

Run every tool call in a reply, instead of the first one and no others.

## Key concept

One assistant message can carry several tool calls. They do not depend on each other — that is
why the model sent them together — so run them all, at the same time, and answer each one with
its own `ToolExecutionResultMessage`. Every call needs an answer, or the next request is
malformed.

## Requirements

- Keep everything lesson 05 does.
- Run every tool call in the reply, not just the first.
- Send one tool message per call, in the order the calls came, each carrying the id of the call
  it answers.
- Run the calls concurrently. Nothing in a reply waits on anything else in the same reply.
- Print a line for each call, as in lesson 05.
- `tools/Tools.java` runs the list; the tool files themselves do not change. `Main.java` changes
  from one call to all of them, and nothing else.
- Send all the answers in **one** request. Asking the model again after each answer is a
  different bug.

## Example

Run `mvn -q compile exec:java` in this repo, then try:

```text
You: Use read_file on both pom.xml and .gitignore, and say what each one contains.
→ read_file {"path":"pom.xml"}
→ read_file {"path":".gitignore"}
Assistant: pom.xml is the Maven build; .gitignore keeps target/ and logs/ out of git.
```

Two arrows now, and lesson 04's silently dropped file is gone.

## Acceptance test

```sh
mvn test -Dtest='Lesson06*'
```

## Pressure test

Ask for something it has to look at twice:

```text
You: read pom.xml, then read whichever file holds its main class
→ read_file {"path":"pom.xml"}
Assistant:
```

The reply is blank. The model read `pom.xml`, saw `agent.Main`, and asked to read that — but
the agent had already spent its one round of tools, so it printed the empty text of a message
that was nothing but a tool call. It can look once. The next lesson lets it keep going until
it has an answer.
