package io.github.beppe2880.opencodemobile.core.network

import io.github.beppe2880.opencodemobile.api.infrastructure.Serializer
import kotlinx.serialization.json.Json

/**
 * The generated client's Json (which already ignores unknown keys), with two changes:
 * - nulls are omitted: the server rejects explicit `null` for optional request fields (for
 *   example `"files": null` in a prompt);
 * - input values are coerced: an unknown enum value (or `null`) in an optional field decodes
 *   to the field's default instead of failing the payload. Union discriminators (`type`) are
 *   required fields without defaults, so unknown variants still fail and are skipped by the
 *   callers ([TolerantPages], [decodeEvent]).
 *
 * Used by Retrofit, the event stream and the transcript cache.
 */
internal val OpenCodeJson: Json = Json(from = Serializer.kotlinxSerializationJson) {
    explicitNulls = false
    coerceInputValues = true
}
