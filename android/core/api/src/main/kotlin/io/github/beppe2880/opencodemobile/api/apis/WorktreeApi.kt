package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.ProjectNotFoundErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.WorktreeCreate400Response
import io.github.beppe2880.opencodemobile.api.models.WorktreeCreateInput
import io.github.beppe2880.opencodemobile.api.models.WorktreeDirectory
import io.github.beppe2880.opencodemobile.api.models.WorktreeInfo
import io.github.beppe2880.opencodemobile.api.models.WorktreeRefreshRequest
import io.github.beppe2880.opencodemobile.api.models.WorktreeRemoveInput

interface WorktreeApi {
    /**
     * POST api/worktree
     * Create worktree
     * Load the project&#39;s canonical configuration, create a local worktree using its selected strategy, then run the project&#39;s setup script.
     * Responses:
     *  - 200: Worktree.Info
     *  - 400: WorktreeError | InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: ProjectNotFoundError
     *
     * @param worktreeCreateInput 
     * @return [WorktreeInfo]
     */
    @POST("api/worktree")
    suspend fun worktreeCreate(@Body worktreeCreateInput: WorktreeCreateInput): Response<WorktreeInfo>

    /**
     * GET api/worktree
     * List worktrees
     * Return the project&#39;s saved worktree inventory without loading configuration or running discovery.
     * Responses:
     *  - 200: Worktree.List
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: ProjectNotFoundError
     *
     * @param projectID 
     * @return [kotlin.collections.List<WorktreeDirectory>]
     */
    @GET("api/worktree")
    suspend fun worktreeList(@Query("projectID") projectID: kotlin.String): Response<kotlin.collections.List<WorktreeDirectory>>

    /**
     * POST api/worktree/refresh
     * Refresh worktrees
     * Load the project&#39;s canonical configuration, discover worktrees across known checkout roots using all available strategies, and reconcile saved state.
     * Responses:
     *  - 204: <No Content>
     *  - 400: WorktreeError | InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: ProjectNotFoundError
     *
     * @param worktreeRefreshRequest 
     * @return [Unit]
     */
    @POST("api/worktree/refresh")
    suspend fun worktreeRefresh(@Body worktreeRefreshRequest: WorktreeRefreshRequest): Response<Unit>

    /**
     * DELETE api/worktree
     * Remove worktree
     * Load the project&#39;s canonical configuration and remove a saved worktree using its recorded strategy.
     * Responses:
     *  - 204: <No Content>
     *  - 400: WorktreeError | InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: ProjectNotFoundError
     *
     * @param worktreeRemoveInput 
     * @return [Unit]
     */
    @DELETE("api/worktree")
    suspend fun worktreeRemove(@Body worktreeRemoveInput: WorktreeRemoveInput): Response<Unit>

}
