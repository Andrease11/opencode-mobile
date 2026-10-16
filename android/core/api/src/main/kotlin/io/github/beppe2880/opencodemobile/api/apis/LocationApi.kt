package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.LocationNotFoundErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.LocationPublicInfo
import io.github.beppe2880.opencodemobile.api.models.ServiceUnavailableErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded

interface LocationApi {
    /**
     * GET api/location
     * Get location
     * Resolve the requested location or the server default location.
     * Responses:
     *  - 200: Location.PublicInfo
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param locationDirectory  (optional)
     * @return [LocationPublicInfo]
     */
    @GET("api/location")
    suspend fun locationGet(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<LocationPublicInfo>

    /**
     * POST api/location/reload
     * Reload configuration
     * Shut down and rebuild every loaded location. Pending permissions and forms are cancelled; running sessions continue with fresh services at the next step boundary. Emits location.shutdown for client recovery and responds once all replacement builds settle.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 503: ServiceUnavailableError
     *
     * @return [Unit]
     */
    @POST("api/location/reload")
    suspend fun locationReload(): Response<Unit>

}
