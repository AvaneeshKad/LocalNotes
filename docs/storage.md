# LocalNotes Storage

This document describes the production storage layer that replaces the exploratory in-memory implementation identified in **Test_5** (`V1_LocalNotes` screenshots): notes were previously kept only in RAM and lost on process death.

## Problem

The original `NoteViewModel` held notes in a `MutableStateFlow<List<Note>>`. That was useful for prototyping UI flows, but it did not survive app restarts and could not be tested as a durable persistence contract.

## Solution

Notes are now stored in on-device SQLite through Room (`localnotes.db` in app-private storage).

| Layer | Responsibility |
| --- | --- |
| `NoteValidator` | Validates editor input before writes |
| `StrokeCodec` | Serializes/deserializes ink strokes to JSON |
| `NoteEntity` / `NoteDao` | Room table and queries |
| `RoomNotesRepository` | Production repository implementation |
| `NotesRepository` | Public contract consumed by `NoteViewModel` |

## Inputs

### `SaveNoteInput`

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `id` | `Long?` | No | Omit for create; provide for update |
| `title` | `String` | Yes | Blank titles become `"Untitled Note"` |
| `content` | `String` | Yes | Typed note body |
| `strokes` | `List<Stroke>` | No | Handwritten ink data |
| `timestamp` | `Long` | No | Defaults to current time |

### Validation rules

- Title length ≤ 200
- Content length ≤ 50,000
- Stroke count ≤ 10,000
- Each stroke must contain at least one point
- Stroke width must be between 0.5 and 100
- Stroke timestamps must be non-negative

## Outputs

### `SaveNoteResult`

- `Success(note)` — note persisted
- `ValidationError(errors)` — input rejected before write
- `StorageError(cause)` — database failure

### `DeleteNoteResult`

- `Success`
- `NotFound(id)`
- `StorageError(cause)`

### Reads

- `observeNotes(): Flow<List<Note>>` — ordered newest first
- `getNote(id: Long): Note?` — single-note lookup

## Tests

| Test | Scope |
| --- | --- |
| `NoteValidatorTest` | JVM unit tests for validation and title normalization |
| `StrokeCodecTest` | JVM unit tests for JSON round-trip |
| `RoomNotesRepositoryTest` | Android instrumented tests against in-memory Room |

Run JVM tests:

```bash
./gradlew :app:testDebugUnitTest
```

Run repository integration tests:

```bash
./gradlew :app:connectedDebugAndroidTest
```

## Wiring

- `LocalNotesApplication` creates the Room database and repository at startup.
- `NoteViewModelFactory` injects `NotesRepository` into `NoteViewModel`.
- UI screens remain unchanged except for using the shared default title constant.

## Migration note

This is schema version 1. Existing in-memory notes from exploratory sessions are not migrated automatically because they were never written to disk.
