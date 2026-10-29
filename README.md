# The Haunted Stack Trace

A Spring Boot error handler possessed by the ghosts of deprecated Java APIs.

When the application throws an exception, it does not return a plain 500 page. The stack trace is sent to **Gemma 4**, an open-weight model running locally through **Ollama**, and a ghost (`java.util.Date`, `Vector`, `Hashtable`, `Applet` or `StringTokenizer`) answers with what broke, the exact line, why it happened and how to fix it. Then it whispers something unsettling.

Everything runs on your machine. No API key, no internet, and no stack trace ever leaves your laptop.

## Example

`GET /cursed/date` calls `LocalDate.parse("31/10/2026")`:

```json
{
  "ghost": "java.util.Date",
  "exception": "java.time.format.DateTimeParseException",
  "culprit": "com.maurodc90.hauntedstacktrace.controller.CursedController.halloweenDate(CursedController.java:63)",
  "cause": "The LocalDate.parse method expects the default ISO-8601 format (yyyy-MM-dd), but the provided string '31/10/2026' is in a different format.",
  "fix": "Use a DateTimeFormatter to specify the expected format, for example: LocalDate.parse(dateString, DateTimeFormatter.ofPattern(\"dd/MM/yyyy\")).",
  "whisper": "You reach for the current moment, but you are trapped in a format of the past. I haunt the dates you cannot reach."
}
```

## Why local and open

- **Private by design.** Stack traces contain class names, file paths and sometimes data. Sending them to a hosted AI service means company code leaving the building. Here it never leaves the machine.
- **Works offline.** No network, no rate limits, no API bill.
- **Swappable.** The model is one line in `application.yml`. Spring AI's `ChatClient` hides the provider, so the same code can point at a bigger local model or a hosted one.

## Tech stack

- Java 21, Spring Boot 4.1
- Spring AI 2.0 (`ChatClient`, `BeanOutputConverter`)
- Ollama with `gemma4:12b`
- Gradle

## How it works

1. A controller throws a `RuntimeException`.
2. `PossessedExceptionHandler` (`@RestControllerAdvice`) catches it and turns the stack trace into text.
3. `GhostService` sends the top 25 lines of the trace to Gemma with a system prompt. The prompt keeps the facts (`cause`, `fix`) plain and puts the role-play only in `whisper`.
4. `BeanOutputConverter` turns the `Haunting` record into JSON format instructions and parses the model's reply back into the record.
5. If Ollama is down, the handler still answers with a fallback instead of failing.

## Endpoints

| Method | Path | What it does |
|---|---|---|
| GET | `/cursed/null` | Throws `NullPointerException` |
| GET | `/cursed/index` | Throws `ArrayIndexOutOfBoundsException` |
| GET | `/cursed/number` | Throws `NumberFormatException` |
| GET | `/cursed/concurrent` | Throws `ConcurrentModificationException` |
| GET | `/cursed/date` | Throws `DateTimeParseException` |
| POST | `/seance` | Explains any stack trace sent as plain text |

## Run it

1. Install Ollama and pull the model:
   ```bash
   brew install ollama
   ollama pull gemma4:12b
   ```
2. Check that Ollama is running:
   ```bash
   curl http://localhost:11434
   ```
3. Start the app:
   ```bash
   ./gradlew bootRun
   ```
4. Wake a ghost:
   ```bash
   curl -s localhost:8080/cursed/null
   ```
   The first call is slow while the model loads into memory.

To explain a stack trace from another project:

```bash
curl -s -X POST localhost:8080/seance \
  -H "Content-Type: text/plain" \
  --data-binary @stacktrace.txt
```

## Design decisions

- **Facts and role-play are separate fields.** The joke never leaks into the fix.
- **Only the top of the trace is sent.** The throw site is at the top. This keeps prompts short and answers fast, and less code leaves the method.
- **`RuntimeException`, not `Exception`.** A request for a missing file such as `favicon.ico` raises `NoResourceFoundException`, which is not a runtime exception. Catching `Exception` would wake the model for every 404.
- **No enforced JSON Schema.** Ollama returned `501 structured output is unavailable` for this Gemma 4 build, so the schema is given as prompt instructions and parsed with `BeanOutputConverter`.
- **Temperature 0.4.** Low enough to keep the facts accurate, high enough for the ghost to have a voice.

## Limitations

- The model can be wrong. Treat the fix as a suggestion, like advice from a colleague.
- `Caused by:` sections at the bottom of long traces are cut off by the 25-line limit.
- Meant for local development. In production you would turn it on only in a dev profile.