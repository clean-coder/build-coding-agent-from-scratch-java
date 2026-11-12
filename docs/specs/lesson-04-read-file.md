# Read file

Give the agent a tool, so it can read a file instead of guessing what is in it.

## Key concept

A tool call is not the model running anything. You send a list of tools you are willing to
run; the model replies "call `read_file` with this path"; **you** run it and send the result
back as another message. That is two requests with your own code in between, and it is the
whole of tool calling.

## Requirements

- Keep everything lesson 03 does: the loop, the conversation, the labels.
- Send one tool with every request: `read_file`, taking a single string `path`. In LangChain4j
  that is a `ToolSpecification` handed to `ChatRequest`.
- When a reply asks for a tool call, read that path and send the contents back as a
  `ToolExecutionResultMessage` answering that call, then ask the model again.
- Keep the assistant's tool-call message in the conversation too — the model has to see what
  it asked for.
- Print the reply that comes back from the second request. A reply that is only a tool call has
  no text, so print it as an empty string rather than `null`.
- **Run only the first tool call.** A reply can carry several; handling more than one is
  lesson 06, and doing it here spends that lesson early.
- One file per tool: `tools/ReadFile.java` owns this tool — the schema the model is sent, and
  what happens when it is called. `tools/Tools.java` owns the plumbing every tool shares.
  Lessons 08 and 09 then add a file instead of growing one.
- Each tool parses its own arguments. The model sends them as a JSON string, so `run` takes
  that string and reads it into the shape it expects — one `record Arguments` per tool file.
- Nothing outside `Llm.java` imports `dev.langchain4j`. It speaks the library's types and hands
  the rest of the program plain records of your own — `Reply`, `ToolCall`, `ToolSchema`. Get this
  wrong and swapping the library later means editing every file instead of one.
- `Llm.java` owns the messages. `Main.java` gains the `if`, and nothing else.

## Example

Run `mvn -q compile exec:java` in this repo, then try:

```text
You: read src/main/java/agent/Cli.java and tell me what it does
Assistant: It prints a prompt, reads a line from the terminal, prints the assistant's reply,
and exits when the input ends.
```

Two requests went out. The first came back asking for `read_file`, and you never saw it.

Small local models are less predictable than hosted ones. Yours may answer without reading
anything, or reply with nothing at all. Nothing is broken: asking again, or naming the tool —
"use read_file on ..." — usually gets the call.

## Acceptance test

```sh
mvn test -Dtest='Lesson04*'
```

## Pressure test

Ask for two files at once:

```text
You: Use read_file on both pom.xml and .gitignore, and say what each one contains.
Assistant: The `.gitignore` file lists patterns for files and directories to exclude from Git...
```

Only `.gitignore` is described. The model asked for both files in one reply; you ran the first,
threw the second away, and the next request went out with two calls and one answer. Ollama does
not complain — it just works with what it has, so the answer quietly covers half the question.
Hosted providers are stricter and reject exactly this request. Same rule either way: **every tool
call needs its own answer.**

Nothing on screen says a call was dropped, or that there were two. The agent printed nothing
about the tool at all. Lesson 06 fixes the bug — but the next lesson comes first, because a bug
you cannot see is a bug you debug by guessing.
