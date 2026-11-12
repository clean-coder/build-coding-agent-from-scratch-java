# Conversation

Send the whole conversation on every request, so the agent remembers what was already said.

## Key concept

The model is stateless. Every request starts from nothing, so memory is not something the
model has — it is something you send. Keep the messages of the session in a list, append
each prompt and each reply to it, and post the whole list every turn.

## Requirements

- Keep everything lesson 02 does: the loop, the Ollama check, the model default, the labels.
- Record each prompt as a `UserMessage` and each reply as an `AiMessage` — LangChain4j's types
  for the `user` and `assistant` roles.
- Send every message so far, oldest first, on every request.
- Keep `Main.java` as it is. The conversation belongs in `Llm.java`, behind the same
  one-argument `complete` — the agent still asks, completes, and replies.

## Example

Run `mvn -q compile exec:java`, then try:

```text
You: My name is Steven.
Assistant: Nice to meet you, Steven.
You: What is my name?
Assistant: Your name is Steven.
```

The second request carries three messages: your first prompt, its reply, and your second
prompt.

## Acceptance test

```sh
mvn test -Dtest='Lesson03*'
```

## Pressure test

Run the agent in this repo, then try:

```text
You: What does src/main/java/agent/Main.java do?
Assistant: I do not have access to your files, but a file named Main.java usually...
```

It remembers the conversation and knows nothing else. Everything it says about your code is a
guess, because the only thing it can see is what you typed. It needs a way to reach out of the
conversation and fetch something — that is a tool, and the next lesson gives it one.
