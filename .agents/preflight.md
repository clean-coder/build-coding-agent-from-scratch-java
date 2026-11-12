---
description: Check the human is ready to build, before lesson 1 costs them time
---

Verify this machine can actually run the workshop. Do it before the first lesson, not during
it — every one of these failures otherwise surfaces mid-lesson as something that looks like a
bug in their code.

Run the checks yourself. Report a short pass/fail list at the end, nothing more. If everything
passes, say so in one line and move on.

## 1 · The shell

```sh
uname -s
```

- `Darwin` or `Linux` — carry on.
- Anything else (Windows) — carry on, and note that every command here is POSIX shell, so this
  has to be **Git Bash** or WSL, not PowerShell, which does not understand `./setup`, `&&` or
  `printf ... |`. Windows is the least rehearsed platform here, so run the checks and let them
  say what is wrong.

## 2 · The environment

```sh
./setup --check
```

On Windows, `sh setup --check`. `--check` only reports what is missing — it never downloads or
prompts. It checks, in order: Java 21 or newer; Maven; git; that Ollama is running at
`OLLAMA_BASE_URL` (default `http://localhost:11434`); that the model — `OLLAMA_MODEL`, default
`qwen3:8b` — is pulled; that the project has been built; and the check that matters — whether
the model will actually emit a `tool_call`. A model that chats fine can still be unable to, and
that failure does not surface until lesson 4, silently, in the middle of a lesson everyone else
has finished.

It stops at the first failure and prints the fix. Most fixes are one command:

- `ollama serve` (or open the Ollama app) if it is not running.
- `ollama pull qwen3:8b` for the default model.
- `./setup` — without `--check` — to build the project once so Maven's downloads are out of the
  way.

Run `./setup` in **their** terminal, not through a tool call. Then run `./setup --check` again
until it says "You're set up".

If it says the model answered with text instead of calling the tool, switch models. Any model
Ollama lists under the *tools* category works (ollama.com/search?c=tools):

```sh
export OLLAMA_MODEL=llama3.1:8b
```

Nothing else in the repo changes. Check that the model is already pulled with `ollama list`.

## 3 · The starting line

```sh
mvn test
```

Expect **lesson 1 passing and lessons 2 to 10 failing** — nine red test classes. That is correct
and it is the point: lesson 1 ships already built so there is a working agent to change, and
every other lesson is a failing assertion waiting to become an instruction. They go green one at
a time. If lesson 1 fails, something above is wrong — go back.

Lesson 1's test spawns the real agent in a child JVM with piped input and checks it exits `0`,
which makes it the only automated check of `Cli.java` in the repo.

## 4 · Their agent

Already proven: they are reading this because their coding agent found it. If it found this
file, it can find the lesson specs and the ledger.

## Record the pass

If every check passed, `touch .preflight-ok` (it is gitignored). That is how `coach me` knows
not to make them sit through this again when the session starts.

## Report

One line per check — the six `./setup --check` prints, the tests, their agent — `pass` or what to
fix. Then either "You're ready — say **coach me** to start lesson 2" or the single most
important thing to fix first. Do not list every problem at once; give them one thing to do.
