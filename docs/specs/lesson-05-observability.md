# Observability

Show what the agent is doing, and keep a transcript of it, so you can debug the loop instead
of guessing at it.

## Key concept

Everything interesting in an agent happens between the prompt and the reply, and by default
none of it is on screen. Two cheap things fix that: print each tool call as it happens, and
append the whole transcript to a file you can `tail -f` in another pane. The screen stays a
summary you can read while it scrolls; the file holds the evidence — what you sent, what came
back, and what broke.

## Requirements

- Keep everything lesson 04 does.
- Start a session log at `logs/session-<timestamp>.log`, one per run. `logs/` is already
  gitignored.
- Say where the log is before the first prompt, as `Session log: <path>`.
- Print a line for every tool call, before running it, naming the tool and its arguments.
- Write the prompts, the replies and the tool-call lines to the log, in the order they happen.
- Log the tools you advertise once, at startup. They never change, and re-sending them to the
  log on every request buries everything that does.
- Log what each request adds to the conversation, and how many messages it now holds. Log the
  reply in full, including its `finishReason`.
- Log what each tool answered with, not only that it was called.
- Indent everything under a headline, so an editor folds it and the headlines are what is left.
- When the model call fails, log the failure — its status code, its body and its stack — then
  let the agent die the way it did before. You are recording the crash, not surviving it.
- `Log.java` owns the file, and anything may write to it. `Cli.java` owns what reaches the
  screen, and is the only place that does both. `Main.java` does not change.

## Example

Run `mvn -q compile exec:java` in this repo, then try:

```text
Session log: logs/session-2026-08-27T18-04-11-233Z.log
You: what does pom.xml say about langchain4j?
→ read_file {"path":"pom.xml"}
Assistant: It pulls in langchain4j-ollama 1.23.0.
```

The arrow is the tool call you could not see in lesson 04. Now `tail -f` the log in another
pane, and the same exchange reads:

```text
[2026-08-27T18:04:11.698Z] loading tools
  [ {
    "name" : "read_file",
    "description" : "Read a file from disk and return its contents.",
    "parameters" : [ "path" ]
  } ]
[2026-08-27T18:04:11.720Z] You: what does pom.xml say about langchain4j?
[2026-08-27T18:04:11.721Z] --> request 1, messages: 1
  {
    "role" : "user",
    "content" : "what does pom.xml say about langchain4j?"
  }
[2026-08-27T18:04:16.306Z] <-- reply 1 after 4585ms
  {
    "toolCalls" : [ { "id" : "call_mi7nl33f", "name" : "read_file", ... } ],
    "text" : "",
    "finishReason" : "TOOL_EXECUTION"
  }
[2026-08-27T18:04:16.307Z] → read_file {"path":"pom.xml"}
[2026-08-27T18:04:16.311Z] <-- read_file answered after 3ms
  <?xml version="1.0" encoding="UTF-8"?>
  ...
```

Every line there answers something the screen cannot: what the model was actually sent, why it
stopped, how long it took, what the tool handed back. Only the headlines start at column zero,
so `grep -n '^\[' logs/session-*.log` gives you an index of the session with line numbers.

## Acceptance test

```sh
mvn test -Dtest='Lesson05*'
```

## Pressure test

Ask for two files again, and this time look at the log:

```text
You: Use read_file on both pom.xml and .gitignore, and say what each one contains.
→ read_file {"path":"pom.xml"}
Assistant: The pom.xml is a Maven project file that ...
```

Same silent drop as lesson 04, and now there is evidence. One arrow on screen — and in the log,
`reply 1` carries a `toolCalls` list with two entries. The thing that went missing is named,
with its id, before anyone has to guess.

That is the whole of observability. The bug did not change; what changed is that you can now
read it off the screen and the log instead of inferring it from a half-right answer. The next
lesson runs all of them.
