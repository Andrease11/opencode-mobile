---
name: android-data-boundaries
description: Use when designing or changing opencode-mobile repository interfaces in core:domain, their implementations in core:data, local stores, SharedPreferences schemas, OpenCodeError mapping, domain mapping, or transcript reconciliation with TranscriptReducer.
---

# Android Data Boundaries

Repositories and stores present small, main-safe contracts to ViewModels. They
hide transport, persistence, dispatcher, and generated-model details while
preserving cancellation and actionable failures.

## Repository Contracts

- Use `suspend` for one-shot work and cold `Flow` for streams.
- Return `core:domain` models rather than generated API models, Retrofit
  responses, cursors, Android controllers, or mutable storage objects.
- Choose one failure contract per cohesive API: typed result, nullable absence,
  or exception. Do not mix meanings for convenience.
- Distinguish not-found/empty from transport, authorization, validation, and
  unexpected failures.
- Keep public methods main-safe; isolate genuinely blocking file/socket work on
  the appropriate dispatcher.
- Repository interfaces live in `core:domain/Repositories.kt` because features
  depend only on that module; implementations live in `core:data`.
- One-shot calls return `Result<T>` whose failures are `OpenCodeError`.

Load `android-opencode-api` or `android-security` when those systems own the
transport-specific decision.

## Transcript Reconciliation

- The server is the source of truth for messages. Live events update the
  transcript optimistically through the pure `TranscriptReducer`.
- Transcripts are mirrored in a Room cache (`core/data/cache`), keyed by saved server and
  session, storing the server wire JSON (`MessageCodec`) so mapping changes apply to cached
  history. The chat shows `ChatRepository.cachedMessages` first, then `messages` syncs.
- Session and project lists are also cached per saved server in Room (`ListCache`, domain
  JSON via `SessionListCodec`): shown first on start and server switch, refreshed after each
  load, cleared with drafts and transcripts when the server is removed.
- Newest first: the cache holds the newest part of a transcript (at most 1000 entries) plus
  `olderCursor` (server cursor to the history before it; null when complete or trimmed).
  `messages` syncs the newest part; `olderMessages` loads one older page (200) and the chat
  calls it until null, prepending to what is shown. Past the cap, pages are shown but not
  stored, and the cursor to the rest is kept in memory by `RemoteChatRepository`.
- List endpoints decode item by item (`TolerantPages`): unknown message or part variants are
  skipped, never failing the whole page; paging compares the server's raw item count.
- Sync is incremental: the anchor is the newest entry such that it and everything older
  are settled (`MessageCodec.isSettled`: no running step, shell or compaction). Pages are
  fetched newest-first (first page 50) until the anchor; everything after it is replaced.
  Without a usable anchor (none cached, not found, undecodable cache) the cache restarts
  from the newest page of 50.
- The sessions list calls `prefetch` for its 5 most recent sessions: sessions not cached
  yet store their last 2 messages, so they open with content.
- The cache is disposable (destructive migrations), keeps the 50 most recently synced
  sessions per server and is cleared when a server is removed.
- Reload messages after `ServerEvent.Connected` (reconnect), step end, execution
  end and structural changes, then `TranscriptReducer.merge` keeps queued prompts
  and the live assistant message that the server has not persisted yet.
- System instructions are dropped in mapping. `idle` and `synthetic` messages are kept
  as `Message.Idle`/`Message.Synthetic`: they are not rendered but define turns.
- A turn is one server execution, as in the OpenCode TUI: from the first user or
  synthetic message after the previous idle up to the next idle. Prompts steered or
  queued while running belong to it. Footer duration, tok/s and cost are computed over
  the whole execution; sessions without idle markers fall back to one turn per prompt.
- Live `session.execution.*` events insert the same idle message the server stores
  (event id with `evt_` replaced by `msg_`), so live and reloaded turns agree.
- Revert follows the TUI: `revert.stage(messageId)` hides that message and everything
  after it (cut in `ChatViewModel.visibleMessages`, not in the cache) and returns its text
  to the composer; `revert.clear` restores; the next prompt first calls `revert.commit`.
  `session.revert.*` events map to `ServerEvent.RevertChanged`.

## Prompt Input

- Attachments are `PromptAttachment` URIs: uploads are `data:` URIs read by
  `AttachmentReader` (bounded to 10 MB, photos downscaled to 2048 px JPEG); `@` mentions
  are `file://<session directory>/<path>` with the mention text. The mention position is
  computed from the final prompt text in `PromptMapping`; deleted mentions are dropped.
- `/name args` runs `session.command` when `name` is a server command; `/undo` and
  `/redo` are handled locally; anything else is sent as a plain prompt.
- Forms (agent questions) come from `session.form.list` and `form.*` events. Hidden
  fields become `Form.presetAnswers`; replies include presets plus answers of visible
  fields (`Form.reply`), with `when` conditions evaluated in the domain.

## Local Stores

- Treat preference/file keys and serialized payloads as a persisted schema.
- Define defaults, missing/corrupt-value behavior, version/migration needs, and
  whether each value may be backed up.
- Store the minimum necessary data. Private drafts, auth material, transcripts,
  and temporary files require explicit retention and cleanup policy.
- Do not expose mutable SharedPreferences or serializer state to feature code.
- Writes that must be durable before the next operation must not rely on an
  asynchronous persistence call without acknowledging that contract.

## Files And Cache

- Name the owner of every cache/temp file and when it is deleted.
- Prefer bounded streaming over reading an untrusted content URI fully into
  memory.
- Validate scheme, MIME type, display name, declared/actual size, and required
  seekability before expensive work.
- Close cursors, streams, HTTP responses, and file descriptors on every path.
- Never log authorization headers, private file contents, prompts or raw
  server response bodies.

## Mapping And Errors

- Keep protocol-to-domain mapping near the data source/repository.
- Unknown generated enum members need a deliberate fallback or typed failure.
- User messages are localized at the UI boundary; repositories retain typed
  meaning and diagnostic causes without exposing secrets.
- Broad catches around suspend calls rethrow `CancellationException`.

## Testing

Test mapping, corrupt local data, missing values, migration/default behavior,
blocking dispatcher boundaries, cancellation, oversized/malformed files, cache
cleanup, and every typed error branch relevant to consumers.
