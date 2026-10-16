package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.ForbiddenErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.LocationNotFoundErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.PtyConnectToken200Response
import io.github.beppe2880.opencodemobile.api.models.PtyCreate200Response
import io.github.beppe2880.opencodemobile.api.models.PtyCreateRequest
import io.github.beppe2880.opencodemobile.api.models.PtyGet200Response
import io.github.beppe2880.opencodemobile.api.models.PtyGet404Response
import io.github.beppe2880.opencodemobile.api.models.PtyList200Response
import io.github.beppe2880.opencodemobile.api.models.PtyUpdateRequest
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded

interface PtyApi {
    /**
     * GET api/pty/{ptyID}/connect
     * Connect to PTY session
     * Establish a WebSocket connection streaming PTY output and accepting terminal input.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 403: ForbiddenError
     *  - 404: PtyNotFoundError | LocationNotFoundError
     *
     * @param ptyID 
     * @param locationDirectory  (optional)
     * @param cursor  (optional)
     * @param ticket  (optional)
     * @return [kotlin.Boolean]
     */
    @GET("api/pty/{ptyID}/connect")
    suspend fun ptyConnect(@Path("ptyID") ptyID: kotlin.String, @Query("location[directory]") locationDirectory: kotlin.String? = null, @Query("cursor") cursor: kotlin.String? = null, @Query("ticket") ticket: kotlin.String? = null): Response<kotlin.Boolean>

    /**
     * POST api/pty/{ptyID}/connect-token
     * Create PTY WebSocket token
     * Create a short-lived single-use ticket for opening a PTY WebSocket connection.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 403: ForbiddenError
     *  - 404: PtyNotFoundError | LocationNotFoundError
     *
     * @param ptyID 
     * @param xOpencodeTicket  (optional)
     * @param locationDirectory  (optional)
     * @return [PtyConnectToken200Response]
     */
    @POST("api/pty/{ptyID}/connect-token")
    suspend fun ptyConnectToken(@Path("ptyID") ptyID: kotlin.String, @Header("x-opencode-ticket") xOpencodeTicket: kotlin.String? = null, @Query("location[directory]") locationDirectory: kotlin.String? = null): Response<PtyConnectToken200Response>

    /**
     * POST api/pty
     * Create PTY session
     * Create a pseudo-terminal session for a location.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param ptyCreateRequest 
     * @param locationDirectory  (optional)
     * @return [PtyCreate200Response]
     */
    @POST("api/pty")
    suspend fun ptyCreate(@Body ptyCreateRequest: PtyCreateRequest, @Query("location[directory]") locationDirectory: kotlin.String? = null): Response<PtyCreate200Response>

    /**
     * GET api/pty/{ptyID}
     * Get PTY session
     * Get one PTY session, including its exit code once exited.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: PtyNotFoundError | LocationNotFoundError
     *
     * @param ptyID 
     * @param locationDirectory  (optional)
     * @return [PtyGet200Response]
     */
    @GET("api/pty/{ptyID}")
    suspend fun ptyGet(@Path("ptyID") ptyID: kotlin.String, @Query("location[directory]") locationDirectory: kotlin.String? = null): Response<PtyGet200Response>

    /**
     * GET api/pty
     * List PTY sessions
     * List PTY sessions for a location, including exited sessions retained until removal.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param locationDirectory  (optional)
     * @return [PtyList200Response]
     */
    @GET("api/pty")
    suspend fun ptyList(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<PtyList200Response>

    /**
     * DELETE api/pty/{ptyID}
     * Remove PTY session
     * Terminate and remove one PTY session.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: PtyNotFoundError | LocationNotFoundError
     *
     * @param ptyID 
     * @param locationDirectory  (optional)
     * @return [Unit]
     */
    @DELETE("api/pty/{ptyID}")
    suspend fun ptyRemove(@Path("ptyID") ptyID: kotlin.String, @Query("location[directory]") locationDirectory: kotlin.String? = null): Response<Unit>

    /**
     * PUT api/pty/{ptyID}
     * Update PTY session
     * Update the title or viewport size of one PTY session.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: PtyNotFoundError | LocationNotFoundError
     *
     * @param ptyID 
     * @param ptyUpdateRequest 
     * @param locationDirectory  (optional)
     * @return [PtyGet200Response]
     */
    @PUT("api/pty/{ptyID}")
    suspend fun ptyUpdate(@Path("ptyID") ptyID: kotlin.String, @Body ptyUpdateRequest: PtyUpdateRequest, @Query("location[directory]") locationDirectory: kotlin.String? = null): Response<PtyGet200Response>

}
