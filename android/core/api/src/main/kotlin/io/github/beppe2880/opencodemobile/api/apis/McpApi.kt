package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.ExperimentalMcpAddRequest
import io.github.beppe2880.opencodemobile.api.models.ExperimentalMcpRemove404Response
import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.LocationNotFoundErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.McpList200Response
import io.github.beppe2880.opencodemobile.api.models.McpResourceCatalog200Response
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded

interface McpApi {
    /**
     * PUT api/experimental/mcp/{server}
     * Add MCP server
     * Add an MCP server at runtime or replace an existing one, connecting it immediately.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param server 
     * @param experimentalMcpAddRequest 
     * @param locationDirectory  (optional)
     * @return [Unit]
     */
    @PUT("api/experimental/mcp/{server}")
    suspend fun experimentalMcpAdd(@Path("server") server: kotlin.String, @Body experimentalMcpAddRequest: ExperimentalMcpAddRequest, @Query("location[directory]") locationDirectory: kotlin.String? = null): Response<Unit>

    /**
     * POST api/experimental/mcp/{server}/connect
     * Connect MCP server
     * Connect an MCP server at runtime, overriding a disabled configuration until restart.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: McpServerNotFoundError | LocationNotFoundError
     *
     * @param server 
     * @param locationDirectory  (optional)
     * @return [Unit]
     */
    @POST("api/experimental/mcp/{server}/connect")
    suspend fun experimentalMcpConnect(@Path("server") server: kotlin.String, @Query("location[directory]") locationDirectory: kotlin.String? = null): Response<Unit>

    /**
     * POST api/experimental/mcp/{server}/disconnect
     * Disconnect MCP server
     * Disconnect an MCP server at runtime, removing its tools until reconnected.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: McpServerNotFoundError | LocationNotFoundError
     *
     * @param server 
     * @param locationDirectory  (optional)
     * @return [Unit]
     */
    @POST("api/experimental/mcp/{server}/disconnect")
    suspend fun experimentalMcpDisconnect(@Path("server") server: kotlin.String, @Query("location[directory]") locationDirectory: kotlin.String? = null): Response<Unit>

    /**
     * DELETE api/experimental/mcp/{server}
     * Remove MCP server
     * Stop an MCP server and remove it from the runtime set until restart.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: McpServerNotFoundError | LocationNotFoundError
     *
     * @param server 
     * @param locationDirectory  (optional)
     * @return [Unit]
     */
    @DELETE("api/experimental/mcp/{server}")
    suspend fun experimentalMcpRemove(@Path("server") server: kotlin.String, @Query("location[directory]") locationDirectory: kotlin.String? = null): Response<Unit>

    /**
     * GET api/mcp
     * List MCP servers
     * Retrieve configured MCP servers and their connection status.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param locationDirectory  (optional)
     * @return [McpList200Response]
     */
    @GET("api/mcp")
    suspend fun mcpList(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<McpList200Response>

    /**
     * GET api/mcp/resource
     * List MCP resources
     * Retrieve resources and resource templates from connected MCP servers.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param locationDirectory  (optional)
     * @return [McpResourceCatalog200Response]
     */
    @GET("api/mcp/resource")
    suspend fun mcpResourceCatalog(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<McpResourceCatalog200Response>

}
