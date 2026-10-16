package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.LocationNotFoundErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.Project
import io.github.beppe2880.opencodemobile.api.models.ProjectUpdate404Response
import io.github.beppe2880.opencodemobile.api.models.ProjectUpdateRequest
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded

interface ProjectApi {

    /**
    * enum for parameter archived
    */
    @Serializable
    enum class ArchivedProjectList(val value: kotlin.String) {
        @SerialName(value = "true") TRUE("true"),
        @SerialName(value = "false") FALSE("false")
    }

    /**
     * GET api/project
     * List projects
     * List known projects. Archived projects are excluded unless archived&#x3D;true.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param archived  (optional)
     * @return [kotlin.collections.List<Project>]
     */
    @GET("api/project")
    suspend fun projectList(@Query("archived") archived: ArchivedProjectList? = null): Response<kotlin.collections.List<Project>>

    /**
     * PATCH api/project/{projectID}
     * Update project
     * Update the project canonical directory, display metadata, and workspace commands.
     * Responses:
     *  - 200: Project
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: ProjectNotFoundError | LocationNotFoundError
     *
     * @param projectID 
     * @param projectUpdateRequest 
     * @return [Project]
     */
    @PATCH("api/project/{projectID}")
    suspend fun projectUpdate(@Path("projectID") projectID: kotlin.String, @Body projectUpdateRequest: ProjectUpdateRequest): Response<Project>

}
