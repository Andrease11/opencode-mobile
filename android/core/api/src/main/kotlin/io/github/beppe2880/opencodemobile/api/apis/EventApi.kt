package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.OpenCodeEvent
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded

interface EventApi {
    /**
     * GET api/event
     * Subscribe to events
     * Subscribe to native events and plugin RPC events across all server locations. Volatile by contract: a slow consumer overflows and fails the stream, and events during disconnection are missed.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *
     * @return [OpenCodeEvent]
     */
    @GET("api/event")
    suspend fun eventSubscribe(): Response<OpenCodeEvent>

}
