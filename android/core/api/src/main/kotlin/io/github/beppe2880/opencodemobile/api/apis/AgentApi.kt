package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.AgentGet200Response
import io.github.beppe2880.opencodemobile.api.models.AgentGet404Response
import io.github.beppe2880.opencodemobile.api.models.AgentList200Response
import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.LocationNotFoundErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded

interface AgentApi {
    /**
     * GET api/agent/{agentID}
     * Get agent
     * Retrieve a single currently registered agent.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: AgentNotFoundError | LocationNotFoundError
     *
     * @param agentID 
     * @param locationDirectory  (optional)
     * @return [AgentGet200Response]
     */
    @GET("api/agent/{agentID}")
    suspend fun agentGet(@Path("agentID") agentID: kotlin.String, @Query("location[directory]") locationDirectory: kotlin.String? = null): Response<AgentGet200Response>

    /**
     * GET api/agent
     * List agents
     * Retrieve currently registered agents.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param locationDirectory  (optional)
     * @return [AgentList200Response]
     */
    @GET("api/agent")
    suspend fun agentList(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<AgentList200Response>

}
