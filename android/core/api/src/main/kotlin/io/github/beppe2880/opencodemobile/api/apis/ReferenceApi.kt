package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.LocationNotFoundErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.ReferenceList200Response
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded

interface ReferenceApi {
    /**
     * GET api/reference
     * List references
     * List references available in the requested location.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param locationDirectory  (optional)
     * @return [ReferenceList200Response]
     */
    @GET("api/reference")
    suspend fun referenceList(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<ReferenceList200Response>

}
