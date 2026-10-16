package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.LocationNotFoundErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.PermissionRequestList200Response
import io.github.beppe2880.opencodemobile.api.models.PermissionSavedList200Response
import io.github.beppe2880.opencodemobile.api.models.SessionPermissionCreate200Response
import io.github.beppe2880.opencodemobile.api.models.SessionPermissionCreateRequest
import io.github.beppe2880.opencodemobile.api.models.SessionPermissionGet200Response
import io.github.beppe2880.opencodemobile.api.models.SessionPermissionGet404Response
import io.github.beppe2880.opencodemobile.api.models.SessionPermissionList200Response
import io.github.beppe2880.opencodemobile.api.models.SessionPermissionReplyRequest
import io.github.beppe2880.opencodemobile.api.models.SessionUpdate404Response
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded

interface PermissionApi {
    /**
     * GET api/permission/request
     * List pending permission requests
     * Retrieve pending permission requests for a location.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param locationDirectory  (optional)
     * @return [PermissionRequestList200Response]
     */
    @GET("api/permission/request")
    suspend fun permissionRequestList(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<PermissionRequestList200Response>

    /**
     * GET api/permission/saved
     * List saved permissions
     * Retrieve saved permissions, optionally filtered by project.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param projectID  (optional)
     * @return [PermissionSavedList200Response]
     */
    @GET("api/permission/saved")
    suspend fun permissionSavedList(@Query("projectID") projectID: kotlin.String? = null): Response<PermissionSavedList200Response>

    /**
     * DELETE api/permission/saved/{id}
     * Remove saved permission
     * Remove a saved permission by ID.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param id 
     * @return [Unit]
     */
    @DELETE("api/permission/saved/{id}")
    suspend fun permissionSavedRemove(@Path("id") id: kotlin.String): Response<Unit>

    /**
     * POST api/session/{sessionID}/permission
     * Create permission request
     * Evaluate and, when approval is required, create a permission request for a session.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @param sessionPermissionCreateRequest 
     * @return [SessionPermissionCreate200Response]
     */
    @POST("api/session/{sessionID}/permission")
    suspend fun sessionPermissionCreate(@Path("sessionID") sessionID: kotlin.String, @Body sessionPermissionCreateRequest: SessionPermissionCreateRequest): Response<SessionPermissionCreate200Response>

    /**
     * GET api/session/{sessionID}/permission/{requestID}
     * Get permission request
     * Retrieve a pending permission request owned by a session.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | PermissionNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @param requestID 
     * @return [SessionPermissionGet200Response]
     */
    @GET("api/session/{sessionID}/permission/{requestID}")
    suspend fun sessionPermissionGet(@Path("sessionID") sessionID: kotlin.String, @Path("requestID") requestID: kotlin.String): Response<SessionPermissionGet200Response>

    /**
     * GET api/session/{sessionID}/permission
     * List session permission requests
     * Retrieve pending permission requests owned by a session.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @return [SessionPermissionList200Response]
     */
    @GET("api/session/{sessionID}/permission")
    suspend fun sessionPermissionList(@Path("sessionID") sessionID: kotlin.String): Response<SessionPermissionList200Response>

    /**
     * POST api/session/{sessionID}/permission/{requestID}/reply
     * Reply to pending permission request
     * Respond to a pending permission request owned by a session.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | PermissionNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @param requestID 
     * @param sessionPermissionReplyRequest 
     * @return [Unit]
     */
    @POST("api/session/{sessionID}/permission/{requestID}/reply")
    suspend fun sessionPermissionReply(@Path("sessionID") sessionID: kotlin.String, @Path("requestID") requestID: kotlin.String, @Body sessionPermissionReplyRequest: SessionPermissionReplyRequest): Response<Unit>

}
