# Build a Coding Agent from Scratch — Java edition

A Java port of [build-coding-agent-from-scratch](../build-coding-agent-from-scratch), using
**Java 21+, Maven, [LangChain4j](https://docs.langchain4j.dev) and a local [Ollama](https://ollama.com) model**.
No API key, no account, nothing leaves your machine.

You use AI agents every day. Under the hood, every one of them is a while loop, a language
model, and some tools. Over ten short lessons you will build one — until it can read your code,
change it, run your tests, and drive a kata test-first.

LangChain4j supplies the model client and the message types. The loop, the tool calling and the
dispatch are yours: this repo deliberately does not use `AiServices` or `@Tool`, because those
hide exactly the machinery you are here to see.

## Setup

You need JDK 21 or newer, Maven 3.9+, git, and [Ollama](https://ollama.com).

```sh
ollama pull qwen3:8b     # the default model, ~5 GB, supports tool calling
./setup
```

`./setup` checks Java, Maven and git, that Ollama is running and has the model, builds the
project once so Maven's downloads are out of the way, and finishes by proving your model can
actually call a tool — the failure that otherwise waits until lesson 4 to surface. To check it
again later without changing anything:

```sh
./setup --check
```

Run the agent you are building at any point:

```sh
mvn -q compile exec:java
```

From lesson 2 on it keeps asking until you stop it. Ctrl-C or Ctrl-D both quit cleanly.

### Choosing a model

Any model Ollama lists under [*tools*](https://ollama.com/search?c=tools) works. Set two optional
environment variables and nothing else changes:

| Variable          | Default                  | Meaning                      |
| ----------------- | ------------------------ | ---------------------------- |
| `OLLAMA_MODEL`    | `qwen3:8b`               | the model to chat with       |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | where Ollama is listening    |

Small local models are less predictable than hosted ones. Expect one that sometimes answers
from memory instead of calling a tool, or calls two tools when you asked for one. That is not
your code; `./setup --check` already told you the model *can* call tools. Ask again, or name the
tool in your prompt. A larger model (`gpt-oss:20b`, for instance) behaves better if your machine
can take it, and `ollama run` loads a model once, so only the first request after a cold start is
slow.

## The lessons

Each lesson adds one capability, then runs into the wall that motivates the next one. That
wall is the point: you feel the limitation before you hear the fix.

| #   | Lesson                 | What you add                                   | What still hurts                                    |
| --- | ---------------------- | ---------------------------------------------- | --------------------------------------------------- |
| 1   | `single-turn`          | one model call, print the reply, exit          | you cannot ask a follow-up                          |
| 2   | `agent-loop`           | the loop — prompt, reply, repeat               | it forgets everything you just said                 |
| 3   | `conversation`         | keep the messages and resend them              | it cannot see your code                             |
| 4   | `read-file`            | a tool: schema, dispatch, tool result          | ask for two files and one is silently dropped       |
| 5   | `observability`        | a session log, and tool calls on screen        | now you can watch it drop the second one            |
| 6   | `parallel-calls`       | run every tool call in the message             | it reads, then stops — it cannot act on what it saw |
| 7   | `tool-call-loop`       | keep going until the model stops calling tools | it understands your code but cannot change it       |
| 8   | `edit-file`            | exact-match edits                              | it edits blind — it cannot run the tests            |
| 9   | `bash`                 | run commands, feed the output back             | powerful, but with no method                        |
| 10  | `coding-system-prompt` | the prompt that makes it work test-first       | nothing — point it at the kata                      |

**The finale:** the rules are in [`kata/bowling/README.md`](kata/bowling/README.md). Start your
agent and give it this:

```text
Read kata/bowling/README.md and build the bowling scorer test-first in kata/bowling/src.
Run `mvn -q -f kata/pom.xml test` to check your work.
```

Nothing in that says how to work. That comes from the system prompt you wrote in lesson 10, and
this is where you find out whether it took. `kata/` is its own Maven project, so a half-finished
scorer never turns your lesson tests red. You are not expected to finish — three rules green,
written test-first, is the exercise working.

The folder holds one file, so your agent has to make the rest. Watch what it does, and watch for
the three ways it goes wrong: writing the code before the test, making a failing test pass by
editing the test, and saying it is done without running anything. When it does one of those, the
system prompt is the thing to change — not the code.

## Get started

Open your coding agent in this directory and say **"preflight"** — it runs `./setup --check` for
you. Then say **"coach me"**. It reads the ledger in [`docs/specs`](docs/specs), finds the first
lesson that is not done, and walks you through it one small step at a time. If you would rather
it wrote the code, say **"jfdi"**.

Claude Code, Codex, Copilot CLI, Cursor and pi all work. Each reads [`AGENTS.md`](AGENTS.md) on
its own and follows it to [`.agents/`](.agents), so the session is the same whichever you brought.

Check your work against the lesson you are on:

```sh
mvn test -Dtest='Lesson04*'    # one lesson
mvn test                       # all of them
```

Lessons you have not built yet stay red — that is the ledger, not a problem. A fresh clone
starts with nine red test classes and they go green one lesson at a time. Each test spawns your
agent in a child JVM and points it at a fake Ollama server, so a green test means the real
program works end to end.

## Falling behind

Every lesson has a tag on the `solution` branch. Take the working version of the lesson you are
stuck on and keep going — the `-- src/main/` matters, it copies those files in and leaves the
rest of your work alone:

```sh
git checkout lesson-4-read-file -- src/main/
```

Look at what changed between two of them to see a lesson in one screen:

```sh
git diff lesson-3-conversation lesson-4-read-file
```

## Repo map

- `docs/specs/` — the ledger and one spec per lesson
- `src/main/java/agent/` — the agent you are growing. `Main.java` is the loop, `Cli.java` the
  terminal, `Llm.java` the model call. Lesson 1 is already here
- `src/test/java/agent/` — an acceptance test per lesson, and under `support/` the fake Ollama
  server they run against
- `kata/` — the finale: a README, and a separate Maven project your agent fills in
- `setup` — the environment check `./setup` runs

## Differences from the TypeScript version

- **Ollama instead of OpenRouter.** No key, no `.env`; `OLLAMA_MODEL` and `OLLAMA_BASE_URL`
  replace `OPENROUTER_MODEL` and the key. The "missing key" check in lesson 1 becomes "Ollama is
  not reachable".
- **No tool-call ids on the wire.** Ollama's API has none; LangChain4j generates ids for you, so
  the lesson 4 pressure test is a *silent* wrong answer rather than a provider error.
- **The slides** (`docs/index.html`) and the QR codes are not ported.
