package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.LocationNotFoundErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.ModelDefault200Response
import io.github.beppe2880.opencodemobile.api.models.ModelList200Response
import io.github.beppe2880.opencodemobile.api.models.ServiceUnavailableErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded

interface ModelApi {
    /**
     * GET api/model/default
     * Get default model
     * Retrieve the model used when a session has no explicit model selection.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *  - 503: ServiceUnavailableError
     *
     * @param locationDirectory  (optional)
     * @return [ModelDefault200Response]
     */
    @GET("api/model/default")
    suspend fun modelDefault(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<ModelDefault200Response>

    /**
     * GET api/model
     * List models
     * Retrieve the current snapshot of available models ordered by release date. The snapshot may precede initial plugin settlement.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *  - 503: ServiceUnavailableError
     *
     * @param locationDirectory  (optional)
     * @return [ModelList200Response]
     */
    @GET("api/model")
    suspend fun modelList(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<ModelList200Response>

}
