package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.LocationNotFoundErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.ProviderGet200Response
import io.github.beppe2880.opencodemobile.api.models.ProviderGet404Response
import io.github.beppe2880.opencodemobile.api.models.ProviderList200Response
import io.github.beppe2880.opencodemobile.api.models.ServiceUnavailableErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded

interface ProviderApi {
    /**
     * GET api/provider/{providerID}
     * Get provider
     * Retrieve a single AI provider so clients can inspect its availability and endpoint settings.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: ProviderNotFoundError | LocationNotFoundError
     *  - 503: ServiceUnavailableError
     *
     * @param providerID 
     * @param locationDirectory  (optional)
     * @return [ProviderGet200Response]
     */
    @GET("api/provider/{providerID}")
    suspend fun providerGet(@Path("providerID") providerID: kotlin.String, @Query("location[directory]") locationDirectory: kotlin.String? = null): Response<ProviderGet200Response>

    /**
     * GET api/provider
     * List providers
     * Retrieve active AI providers so clients can show provider availability and configuration.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *  - 503: ServiceUnavailableError
     *
     * @param locationDirectory  (optional)
     * @return [ProviderList200Response]
     */
    @GET("api/provider")
    suspend fun providerList(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<ProviderList200Response>

}
