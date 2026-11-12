# Agent loop

Keep asking for prompts and answering them, instead of exiting after the first reply.

## Key concept

The feeling of talking to an agent comes from a loop. That is genuinely all it is — the same
single call from lesson 01, run over and over. Nothing about the model changed; only the
shape of the code around it did.

## Requirements

- Run it with `mvn -q compile exec:java`.
- Keep everything lesson 01 does: the Ollama check, the model default, the `You:` and
  `Assistant:` labels.
- After each reply, ask for another prompt.
- Send only the latest prompt to the model.
- Do not send earlier prompts or replies.
- Stop when the input ends, or when the user presses Ctrl-C.
- Neither way of stopping prints a stack trace.
- Change `Main.java` only. `Cli.java` and `Llm.java` already do their part.

## Example

Run `mvn -q compile exec:java`, then try:

```text
You: What is the capital of France?
Assistant: The capital of France is Paris.
You: What is the capital of Germany?
Assistant: The capital of Germany is Berlin.
```

Two questions, one process.

## Acceptance test

```sh
mvn test -Dtest='Lesson02*'
```

## Pressure test

Run the agent, then try:

```text
You: My name is Steven.
Assistant: Nice to meet you, Steven.
You: What is my name?
Assistant: I don't know your name.
```

The loop runs, but every turn starts from nothing. The model is not being forgetful — you
never told it. Each request carries one message and no history at all, so there is nothing
for it to remember. That is why the next lesson sends the conversation back every time.
