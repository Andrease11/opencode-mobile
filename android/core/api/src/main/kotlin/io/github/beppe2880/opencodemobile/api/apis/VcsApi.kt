package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.ConflictErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.LocationNotFoundErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.ServiceUnavailableErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.VcsBase200Response
import io.github.beppe2880.opencodemobile.api.models.VcsBranchList200Response
import io.github.beppe2880.opencodemobile.api.models.VcsDiff200Response
import io.github.beppe2880.opencodemobile.api.models.VcsGet200Response
import io.github.beppe2880.opencodemobile.api.models.VcsInitNotSupportedErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.VcsMode
import io.github.beppe2880.opencodemobile.api.models.VcsStatus200Response

interface VcsApi {
    /**
     * GET api/vcs/base
     * VCS review base
     * Infer a local review base from named branch creation history, or the repository default only when currently on that branch. Returns null before the first commit or when the provider lacks base metadata; ambiguous Git history requires an explicit base on diff requests.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *  - 503: ServiceUnavailableError
     *
     * @param locationDirectory  (optional)
     * @return [VcsBase200Response]
     */
    @GET("api/vcs/base")
    suspend fun vcsBase(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<VcsBase200Response>

    /**
     * GET api/vcs/branch
     * VCS branches
     * List local and remote branches available at the requested location.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param locationDirectory  (optional)
     * @param search  (optional)
     * @param limit  (optional)
     * @return [VcsBranchList200Response]
     */
    @GET("api/vcs/branch")
    suspend fun vcsBranchList(@Query("location[directory]") locationDirectory: kotlin.String? = null, @Query("search") search: kotlin.String? = null, @Query("limit") limit: kotlin.String? = null): Response<VcsBranchList200Response>

    /**
     * GET api/vcs/diff
     * VCS diff
     * Diff HEAD to the working copy (working), the base merge-base to the working copy (branch), or the base merge-base to HEAD (committed). Omitting base preserves repository-default comparison; supplying it overrides the comparison without saving it.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *  - 503: ServiceUnavailableError
     *
     * @param mode 
     * @param locationDirectory  (optional)
     * @param base  (optional)
     * @param context  (optional)
     * @return [VcsDiff200Response]
     */
    @GET("api/vcs/diff")
    suspend fun vcsDiff(@Query("mode") mode: VcsMode, @Query("location[directory]") locationDirectory: kotlin.String? = null, @Query("base") base: kotlin.String? = null, @Query("context") context: kotlin.String? = null): Response<VcsDiff200Response>

    /**
     * GET api/vcs
     * VCS info
     * Get current and default branch information for the requested location.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param locationDirectory  (optional)
     * @return [VcsGet200Response]
     */
    @GET("api/vcs")
    suspend fun vcsGet(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<VcsGet200Response>

    /**
     * POST api/vcs/init
     * Initialize VCS repository
     * Initialize a repository using the selected VCS provider in a markerless project&#39;s directory and refresh its location services. Omitting provider defaults to git; built-in git and hg providers support initialization. An unknown provider returns 400; a registered provider without init returns 501.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *  - 409: ConflictError
     *  - 501: VcsInitNotSupportedError
     *  - 503: ServiceUnavailableError
     *
     * @param locationDirectory  (optional)
     * @param provider  (optional)
     * @return [Unit]
     */
    @POST("api/vcs/init")
    suspend fun vcsInit(@Query("location[directory]") locationDirectory: kotlin.String? = null, @Query("provider") provider: kotlin.String? = null): Response<Unit>

    /**
     * GET api/vcs/status
     * VCS status
     * List uncommitted working-copy changes relative to the requested location.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param locationDirectory  (optional)
     * @return [VcsStatus200Response]
     */
    @GET("api/vcs/status")
    suspend fun vcsStatus(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<VcsStatus200Response>

}
