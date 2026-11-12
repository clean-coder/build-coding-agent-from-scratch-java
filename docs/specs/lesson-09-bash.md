# Bash

Give the agent a shell, so it can run your build and read what came back.

## Key concept

A shell is the tool that closes the loop. Until now the agent could read your code and change
it, but nothing it did was ever checked — it changed a file and told you to go and run the
tests. With a shell it runs them itself, reads the output and keeps going.

Your terminal is invisible to the model. A command that fails prints in red and exits non-zero,
and none of that reaches the model unless you put it in the string you send back. So a failing
command is not an error in your program — it is an ordinary result, and the exit code is the
only way the model knows it happened.

## Requirements

- Keep everything lesson 08 does.
- Send a third tool with every request: `bash`, taking a single string `command`.
- Run it with `bash -c` (a `ProcessBuilder`) and answer the call with stdout and stderr
  interleaved, followed by the exit code as `Exit code: <n>`.
- A command that exits non-zero is a result, not a crash. Report it and carry on.
- Cut the output off after about 8000 characters and say that you did, using the words
  `cut off` — a stray `cat` of a big file is a context window, spent.
- Kill a command still running after 30 seconds and say so, or the first command that waits for
  input hangs the agent with no explanation on screen.
- If a tool throws — a missing path, a bad argument — answer the call with the reason instead of
  dying. That is one `catch` in `tools/Tools.java` and it covers every tool, not just this one.
- `tools/Bash.java` owns the new tool. `tools/Tools.java` gains the dispatch case and the catch.
  `Main.java`, `tools/ReadFile.java` and `tools/EditFile.java` do not change.

## Example

Run `mvn -q compile exec:java` in this repo, then ask for the thing lesson 08 could only
half-do:

```text
You: make Cli.ask() return the input trimmed, then run the tests
→ read_file {"path":"src/main/java/agent/Cli.java"}
→ edit_file {"path":"src/main/java/agent/Cli.java","oldText":"Log.write(\"You: \" + line);\n\n    return line;","newText":"Log.write(\"You: \" + line);\n\n    return line.trim();"}
→ bash {"command":"mvn test -Dtest='Lesson01*'"}
Assistant: Done. ask() now returns the trimmed line. The lesson 1 tests pass.
```

`git diff` and put it back. Read the calls in order: it read, it edited, and then it checked —
and the third one is what makes the first two worth anything.

The two numbers in `Bash.java` are guardrails, and you can watch one of them:

```text
You: run this exact command: sleep 45
→ bash {"command":"sleep 45"}
Assistant: The `sleep 45` command was killed because it exceeded the 30-second execution
timeout. If you need a longer sleep, you can try a shorter duration like `sleep 25`.
```

Nothing told the model about a timeout except the string your own tool sent back. The same is
true of a tool that throws:

```text
You: read the file no/such/file.txt
→ read_file {"path":"no/such/file.txt"}
Assistant: The file no/such/file.txt does not exist, so it cannot be read. The error
NoSuchFileException indicates the file is missing.
```

Until this lesson that `NoSuchFileException` killed the agent — an exception sitting in
`read_file` since lesson 04, waiting for the first mistyped path.

## Acceptance test

```sh
mvn test -Dtest='Lesson09*'
```

## Pressure test

Break one expectation in any test, make a note for it to edit, then ask for both at once:

```sh
echo 'title: old' > scratch-note.txt
```

```text
You: run the tests, then change the title in scratch-note.txt to new
→ bash {"command":"mvn test"}
→ read_file {"path":"scratch-note.txt"}
→ edit_file {"path":"scratch-note.txt","oldText":"title: old","newText":"title: new"}
Assistant: Done. Tests: one failed (the deliberately broken scratch test).
scratch-note.txt: title changed from old to new.
```

It ran the tests, saw one fail, reported it accurately, and carried on editing as though nothing
had happened. It now has every tool a coding agent has and none of the habits: nothing tells it
that a red test comes before a new edit, or that "done" is a claim you check before you make it.
Tools are what it can do. The last lesson is about what it should do.
