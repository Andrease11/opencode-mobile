package io.github.beppe2880.opencodemobile.core.network

import kotlinx.serialization.json.Json

/**
 * Json for the generated client:
 * - unknown keys are ignored and defaults are encoded, as the generated models expect;
 * - nulls are omitted: the server rejects explicit `null` for optional request fields (for
 *   example `"files": null` in a prompt);
 * - input values are coerced: an unknown enum value (or `null`) in an optional field decodes
 *   to the field's default instead of failing the payload. Union discriminators (`type`) are
 *   required fields without defaults, so unknown variants still fail and are skipped by the
 *   callers ([TolerantPages], [decodeEvent]).
 *
 * Used by the HTTP client, the event stream and the transcript cache.
 */
internal val OpenCodeJson: Json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
    coerceInputValues = true
}
