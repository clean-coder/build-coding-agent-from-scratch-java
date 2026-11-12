# Single turn

Ask for one prompt, send it to the model, print the reply, and exit.

This lesson is already built for you. Read `src/main/java/agent/Main.java` first — it is a
handful of lines, and it is the whole agent. Then read the two classes it leans on. Every later
lesson grows out of these three.

## Key concept

There is no magic underneath a coding agent. One turn is one HTTP request to Ollama: you send messages,
you get a message back. Everything else in this workshop is built on top of that single call.

## The shape

`Main.java` says what the agent does, and nothing about how. Terminal handling lives in
`Cli.java`, the model call in `Llm.java`. Keep it that way — when a later lesson makes
`Main` hard to read at a glance, the new detail belongs in one of the other two.

Each class is called by name — `Cli.ask()`, `Llm.complete()` — so every verb says where it
comes from without you scrolling back, and the agent reads like a list of its parts.

| File       | Owns                                                   |
| ---------- | ------------------------------------------------------ |
| `Main.java` | the agent, in the fewest lines that still say it        |
| `Cli.java`  | reading a prompt, printing a reply, ending on Ctrl-D    |
| `Llm.java`  | the Ollama address, the model, and the call to it       |

## Requirements

- Run it with `mvn -q compile exec:java`.
- Talk to Ollama at `OLLAMA_BASE_URL`, or `http://localhost:11434` if it is unset.
- If Ollama is not reachable, print a short error naming it and exit non-zero.
- Ask the user for one prompt, labelled `You:`.
- Use `OLLAMA_MODEL`, or `qwen3:8b` if it is unset.
- Call the model through LangChain4j's `OllamaChatModel`, with retries off: a failed call should
  fail once, loudly.
- Print the reply, labelled `Assistant:`.
- Exit after the reply.

## Example

Run `mvn -q compile exec:java`, then try:

```text
You: What is the capital of France?
Assistant: The capital of France is Paris.
```

One reply, then the program exits.

## Acceptance test

```sh
mvn test -Dtest='Lesson01*'
```

## Pressure test

Run it, ask one question, then try to ask a second one.

You cannot. The program has already exited, so every question needs a fresh process — and a
fresh process means a fresh `mvn` run for every single thing you want to say. That is why
the next lesson wraps this in a loop.
