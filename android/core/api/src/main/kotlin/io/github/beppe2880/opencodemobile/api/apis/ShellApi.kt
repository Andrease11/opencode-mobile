package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.LocationNotFoundErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.ShellCreate200Response
import io.github.beppe2880.opencodemobile.api.models.ShellCreateRequest
import io.github.beppe2880.opencodemobile.api.models.ShellGet200Response
import io.github.beppe2880.opencodemobile.api.models.ShellGet404Response
import io.github.beppe2880.opencodemobile.api.models.ShellList200Response
import io.github.beppe2880.opencodemobile.api.models.ShellOutput200Response
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded

interface ShellApi {
    /**
     * POST api/shell
     * Run shell command
     * Spawn one non-interactive shell command for a location. Combined stdout/stderr is captured to a file pageable via output.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param shellCreateRequest 
     * @param locationDirectory  (optional)
     * @return [ShellCreate200Response]
     */
    @POST("api/shell")
    suspend fun shellCreate(@Body shellCreateRequest: ShellCreateRequest, @Query("location[directory]") locationDirectory: kotlin.String? = null): Response<ShellCreate200Response>

    /**
     * GET api/shell/{id}
     * Get shell command
     * Get one shell command, including its status and exit code once exited.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: ShellNotFoundError | LocationNotFoundError
     *
     * @param id 
     * @param locationDirectory  (optional)
     * @return [ShellGet200Response]
     */
    @GET("api/shell/{id}")
    suspend fun shellGet(@Path("id") id: kotlin.String, @Query("location[directory]") locationDirectory: kotlin.String? = null): Response<ShellGet200Response>

    /**
     * GET api/shell
     * List running shell commands
     * List currently running shell commands for a location. Exited commands are not included.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param locationDirectory  (optional)
     * @return [ShellList200Response]
     */
    @GET("api/shell")
    suspend fun shellList(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<ShellList200Response>

    /**
     * GET api/shell/{id}/output
     * Read shell output
     * Page through captured combined output by absolute byte cursor.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: ShellNotFoundError | LocationNotFoundError
     *
     * @param id 
     * @param locationDirectory  (optional)
     * @param cursor  (optional)
     * @param limit  (optional)
     * @return [ShellOutput200Response]
     */
    @GET("api/shell/{id}/output")
    suspend fun shellOutput(@Path("id") id: kotlin.String, @Query("location[directory]") locationDirectory: kotlin.String? = null, @Query("cursor") cursor: kotlin.String? = null, @Query("limit") limit: kotlin.String? = null): Response<ShellOutput200Response>

    /**
     * DELETE api/shell/{id}
     * Remove shell command
     * Terminate and remove one shell command and its retained output.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param id 
     * @param locationDirectory  (optional)
     * @return [Unit]
     */
    @DELETE("api/shell/{id}")
    suspend fun shellRemove(@Path("id") id: kotlin.String, @Query("location[directory]") locationDirectory: kotlin.String? = null): Response<Unit>

}
