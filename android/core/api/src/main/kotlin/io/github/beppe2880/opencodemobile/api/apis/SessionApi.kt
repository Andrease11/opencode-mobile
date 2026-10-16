package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.CommandExecutionErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.ConflictErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.ExperimentalSessionExport200Response
import io.github.beppe2880.opencodemobile.api.models.ExperimentalSessionImport200Response
import io.github.beppe2880.opencodemobile.api.models.ExperimentalSessionImportRequest
import io.github.beppe2880.opencodemobile.api.models.ExperimentalSessionInstructionsEntryList200Response
import io.github.beppe2880.opencodemobile.api.models.ExperimentalSessionInstructionsEntryPutRequest
import io.github.beppe2880.opencodemobile.api.models.ExperimentalSessionSkill404Response
import io.github.beppe2880.opencodemobile.api.models.ExperimentalSessionSkillRequest
import io.github.beppe2880.opencodemobile.api.models.ExperimentalSessionStats200Response
import io.github.beppe2880.opencodemobile.api.models.FormAlreadySettledErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.FormCreatePayload
import io.github.beppe2880.opencodemobile.api.models.FormReply
import io.github.beppe2880.opencodemobile.api.models.InstructionEntryValueTooLargeErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.ServiceUnavailableErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.SessionActive200Response
import io.github.beppe2880.opencodemobile.api.models.SessionBusyErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.SessionCommand404Response
import io.github.beppe2880.opencodemobile.api.models.SessionCommandRequest
import io.github.beppe2880.opencodemobile.api.models.SessionCompact200Response
import io.github.beppe2880.opencodemobile.api.models.SessionCompactRequest
import io.github.beppe2880.opencodemobile.api.models.SessionContext200Response
import io.github.beppe2880.opencodemobile.api.models.SessionCreate200Response
import io.github.beppe2880.opencodemobile.api.models.SessionCreateRequest
import io.github.beppe2880.opencodemobile.api.models.SessionDiff200Response
import io.github.beppe2880.opencodemobile.api.models.SessionEnvironmentRequest
import io.github.beppe2880.opencodemobile.api.models.SessionFork404Response
import io.github.beppe2880.opencodemobile.api.models.SessionForkRequest
import io.github.beppe2880.opencodemobile.api.models.SessionFormCreate200Response
import io.github.beppe2880.opencodemobile.api.models.SessionFormGet200Response
import io.github.beppe2880.opencodemobile.api.models.SessionFormGet404Response
import io.github.beppe2880.opencodemobile.api.models.SessionFormList200Response
import io.github.beppe2880.opencodemobile.api.models.SessionFormReply400Response
import io.github.beppe2880.opencodemobile.api.models.SessionGenerateRequest
import io.github.beppe2880.opencodemobile.api.models.SessionGenerateResponse
import io.github.beppe2880.opencodemobile.api.models.SessionInboxList200Response
import io.github.beppe2880.opencodemobile.api.models.SessionInboxUpdateRequest
import io.github.beppe2880.opencodemobile.api.models.SessionInterruptResponse
import io.github.beppe2880.opencodemobile.api.models.SessionList400Response
import io.github.beppe2880.opencodemobile.api.models.SessionLog200Response
import io.github.beppe2880.opencodemobile.api.models.SessionMessageGet200Response
import io.github.beppe2880.opencodemobile.api.models.SessionMessageGet404Response
import io.github.beppe2880.opencodemobile.api.models.SessionMessagesResponse
import io.github.beppe2880.opencodemobile.api.models.SessionMoveRequest
import io.github.beppe2880.opencodemobile.api.models.SessionNotFoundErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.SessionPrompt200Response
import io.github.beppe2880.opencodemobile.api.models.SessionPromptRequest
import io.github.beppe2880.opencodemobile.api.models.SessionRevertStage200Response
import io.github.beppe2880.opencodemobile.api.models.SessionRevertStage404Response
import io.github.beppe2880.opencodemobile.api.models.SessionRevertStageRequest
import io.github.beppe2880.opencodemobile.api.models.SessionShellRequest
import io.github.beppe2880.opencodemobile.api.models.SessionSwitchAgentRequest
import io.github.beppe2880.opencodemobile.api.models.SessionSwitchModelRequest
import io.github.beppe2880.opencodemobile.api.models.SessionSynthetic200Response
import io.github.beppe2880.opencodemobile.api.models.SessionSyntheticRequest
import io.github.beppe2880.opencodemobile.api.models.SessionUpdate404Response
import io.github.beppe2880.opencodemobile.api.models.SessionUpdateRequest
import io.github.beppe2880.opencodemobile.api.models.SessionViewRequest
import io.github.beppe2880.opencodemobile.api.models.SessionsResponse
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.UnknownErrorEncoded

interface SessionApi {

    /**
    * enum for parameter sanitize
    */
    @Serializable
    enum class SanitizeExperimentalSessionExport(val value: kotlin.String) {
        @SerialName(value = "true") TRUE("true"),
        @SerialName(value = "false") FALSE("false")
    }

    /**
     * GET api/experimental/session/{sessionID}/export
     * Export session
     * Export a complete projected session transcript.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError
     *  - 500: UnknownError
     *
     * @param sessionID 
     * @param sanitize  (optional)
     * @return [ExperimentalSessionExport200Response]
     */
    @GET("api/experimental/session/{sessionID}/export")
    suspend fun experimentalSessionExport(@Path("sessionID") sessionID: kotlin.String, @Query("sanitize") sanitize: SanitizeExperimentalSessionExport? = null): Response<ExperimentalSessionExport200Response>

    /**
     * POST api/experimental/session/import
     * Import session
     * Import a projected session transcript at the requested location. If parentID is supplied, the parent session must already exist; import parents before children.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError
     *  - 409: ConflictError
     *
     * @param experimentalSessionImportRequest 
     * @return [ExperimentalSessionImport200Response]
     */
    @POST("api/experimental/session/import")
    suspend fun experimentalSessionImport(@Body experimentalSessionImportRequest: ExperimentalSessionImportRequest): Response<ExperimentalSessionImport200Response>

    /**
     * GET api/experimental/session/{sessionID}/instructions/entries
     * List instruction entries
     * List API-managed instruction entries attached to the session.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @return [ExperimentalSessionInstructionsEntryList200Response]
     */
    @GET("api/experimental/session/{sessionID}/instructions/entries")
    suspend fun experimentalSessionInstructionsEntryList(@Path("sessionID") sessionID: kotlin.String): Response<ExperimentalSessionInstructionsEntryList200Response>

    /**
     * PUT api/experimental/session/{sessionID}/instructions/entries/{key}
     * Put instruction entry
     * Attach or replace one durable instruction entry. Changes announce as updates at the next step boundary.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *  - 413: InstructionEntryValueTooLargeError
     *
     * @param sessionID 
     * @param key 
     * @param experimentalSessionInstructionsEntryPutRequest 
     * @return [Unit]
     */
    @PUT("api/experimental/session/{sessionID}/instructions/entries/{key}")
    suspend fun experimentalSessionInstructionsEntryPut(@Path("sessionID") sessionID: kotlin.String, @Path("key") key: kotlin.String, @Body experimentalSessionInstructionsEntryPutRequest: ExperimentalSessionInstructionsEntryPutRequest): Response<Unit>

    /**
     * DELETE api/experimental/session/{sessionID}/instructions/entries/{key}
     * Remove instruction entry
     * Remove one instruction entry; the removal is announced to the model at the next step boundary.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @param key 
     * @return [Unit]
     */
    @DELETE("api/experimental/session/{sessionID}/instructions/entries/{key}")
    suspend fun experimentalSessionInstructionsEntryRemove(@Path("sessionID") sessionID: kotlin.String, @Path("key") key: kotlin.String): Response<Unit>

    /**
     * POST api/experimental/session/{sessionID}/skill
     * Activate skill
     * Activate a skill for a session by appending a skill message and resuming execution.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | SkillNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @param experimentalSessionSkillRequest 
     * @return [Unit]
     */
    @POST("api/experimental/session/{sessionID}/skill")
    suspend fun experimentalSessionSkill(@Path("sessionID") sessionID: kotlin.String, @Body experimentalSessionSkillRequest: ExperimentalSessionSkillRequest): Response<Unit>


    /**
    * enum for parameter tools
    */
    @Serializable
    enum class ToolsExperimentalSessionStats(val value: kotlin.String) {
        @SerialName(value = "none") NONE("none"),
        @SerialName(value = "summary") SUMMARY("summary"),
        @SerialName(value = "detail") DETAIL("detail")
    }

    /**
     * GET api/experimental/session/stats
     * Get session statistics
     * Aggregate local session activity, usage, and tool reliability for a time range.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *
     * @param from  (optional)
     * @param to  (optional)
     * @param project  (optional)
     * @param timezone  (optional)
     * @param tools  (optional)
     * @return [ExperimentalSessionStats200Response]
     */
    @GET("api/experimental/session/stats")
    suspend fun experimentalSessionStats(@Query("from") from: kotlin.String? = null, @Query("to") to: kotlin.String? = null, @Query("project") project: kotlin.String? = null, @Query("timezone") timezone: kotlin.String? = null, @Query("tools") tools: ToolsExperimentalSessionStats? = null): Response<ExperimentalSessionStats200Response>

    /**
     * POST api/experimental/session/{sessionID}/wait
     * Wait for session
     * Wait for a session agent loop to become idle.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *  - 503: ServiceUnavailableError
     *
     * @param sessionID 
     * @return [Unit]
     */
    @POST("api/experimental/session/{sessionID}/wait")
    suspend fun experimentalSessionWait(@Path("sessionID") sessionID: kotlin.String): Response<Unit>

    /**
     * GET api/session/active
     * List active sessions
     * Retrieve foreground Session drains currently owned by this OpenCode process. Sessions absent from the result are inactive.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *
     * @return [SessionActive200Response]
     */
    @GET("api/session/active")
    suspend fun sessionActive(): Response<SessionActive200Response>

    /**
     * POST api/session/{sessionID}/background
     * Background blocking session tools
     * Move active foreground backgroundable tools for this session into background observation. Idle requests are a no-op.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @return [Unit]
     */
    @POST("api/session/{sessionID}/background")
    suspend fun sessionBackground(@Path("sessionID") sessionID: kotlin.String): Response<Unit>

    /**
     * POST api/session/{sessionID}/command
     * Run command
     * Execute a slash command callback immediately.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | CommandNotFoundError | LocationNotFoundError
     *  - 500: CommandExecutionError
     *
     * @param sessionID 
     * @param sessionCommandRequest 
     * @return [Unit]
     */
    @POST("api/session/{sessionID}/command")
    suspend fun sessionCommand(@Path("sessionID") sessionID: kotlin.String, @Body sessionCommandRequest: SessionCommandRequest): Response<Unit>

    /**
     * POST api/session/{sessionID}/compact
     * Compact session
     * Durably admit a session compaction request. Steers by default: it runs at the next step boundary instead of waiting behind queued prompts.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *  - 409: ConflictError
     *
     * @param sessionID 
     * @param sessionCompactRequest 
     * @return [SessionCompact200Response]
     */
    @POST("api/session/{sessionID}/compact")
    suspend fun sessionCompact(@Path("sessionID") sessionID: kotlin.String, @Body sessionCompactRequest: SessionCompactRequest): Response<SessionCompact200Response>

    /**
     * GET api/session/{sessionID}/context
     * Get session context
     * Retrieve the active context messages for a session (all messages after the last compaction).
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError
     *  - 500: UnknownError
     *
     * @param sessionID 
     * @return [SessionContext200Response]
     */
    @GET("api/session/{sessionID}/context")
    suspend fun sessionContext(@Path("sessionID") sessionID: kotlin.String): Response<SessionContext200Response>

    /**
     * POST api/session
     * Create session
     * Create a session at the requested location. A parentID creates a linked child session at its parent&#39;s location.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError
     *
     * @param sessionCreateRequest 
     * @return [SessionCreate200Response]
     */
    @POST("api/session")
    suspend fun sessionCreate(@Body sessionCreateRequest: SessionCreateRequest): Response<SessionCreate200Response>

    /**
     * GET api/session/{sessionID}/diff
     * Diff session turns
     * Structured per-file diffs of the files a turn changed. A turn runs from the first prompt after the session was last idle until its next idle marker, so prompts steered in while it was busy belong to the same turn; &#x60;to&#x60; extends the range through a later turn. Compares the range&#39;s first recorded snapshot with its last; a step still running in the active session compares against the working copy. Ranges that span a location change are rejected. In sessions without any idle marker, a prompt&#39;s turn spans until the next user message.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: MessageNotFoundError | SessionNotFoundError | LocationNotFoundError
     *  - 500: UnknownError
     *
     * @param sessionID 
     * @param from  (optional)
     * @param to  (optional)
     * @param context  (optional)
     * @return [SessionDiff200Response]
     */
    @GET("api/session/{sessionID}/diff")
    suspend fun sessionDiff(@Path("sessionID") sessionID: kotlin.String, @Query("from") from: kotlin.String? = null, @Query("to") to: kotlin.String? = null, @Query("context") context: kotlin.String? = null): Response<SessionDiff200Response>

    /**
     * PUT api/session/{sessionID}/environment
     * Set session environment
     * Replace the process environment used by local shell commands for this session.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError
     *
     * @param sessionID 
     * @param sessionEnvironmentRequest 
     * @return [Unit]
     */
    @PUT("api/session/{sessionID}/environment")
    suspend fun sessionEnvironment(@Path("sessionID") sessionID: kotlin.String, @Body sessionEnvironmentRequest: SessionEnvironmentRequest): Response<Unit>

    /**
     * POST api/session/{sessionID}/fork
     * Fork session
     * Create a child session by copying projected history before a message. Omit before to copy the full history.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | MessageNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @param sessionForkRequest 
     * @return [ExperimentalSessionImport200Response]
     */
    @POST("api/session/{sessionID}/fork")
    suspend fun sessionFork(@Path("sessionID") sessionID: kotlin.String, @Body sessionForkRequest: SessionForkRequest): Response<ExperimentalSessionImport200Response>

    /**
     * DELETE api/session/{sessionID}/form/{formID}
     * Cancel form
     * Cancel a pending form, optionally telling the asker why it was not answered.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | FormNotFoundError | LocationNotFoundError
     *  - 409: FormAlreadySettledError
     *
     * @param sessionID 
     * @param formID 
     * @param message  (optional)
     * @return [Unit]
     */
    @DELETE("api/session/{sessionID}/form/{formID}")
    suspend fun sessionFormCancel(@Path("sessionID") sessionID: kotlin.String, @Path("formID") formID: kotlin.String, @Query("message") message: kotlin.String? = null): Response<Unit>

    /**
     * POST api/session/{sessionID}/form
     * Create session form
     * Create a form for a session.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *  - 409: ConflictError
     *
     * @param sessionID 
     * @param formCreatePayload 
     * @return [SessionFormCreate200Response]
     */
    @POST("api/session/{sessionID}/form")
    suspend fun sessionFormCreate(@Path("sessionID") sessionID: kotlin.String, @Body formCreatePayload: FormCreatePayload): Response<SessionFormCreate200Response>

    /**
     * GET api/session/{sessionID}/form/{formID}
     * Get session form
     * Retrieve a form and its current state for a session.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | FormNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @param formID 
     * @return [SessionFormGet200Response]
     */
    @GET("api/session/{sessionID}/form/{formID}")
    suspend fun sessionFormGet(@Path("sessionID") sessionID: kotlin.String, @Path("formID") formID: kotlin.String): Response<SessionFormGet200Response>

    /**
     * GET api/session/{sessionID}/form
     * List session forms
     * Retrieve pending forms for a session.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @return [SessionFormList200Response]
     */
    @GET("api/session/{sessionID}/form")
    suspend fun sessionFormList(@Path("sessionID") sessionID: kotlin.String): Response<SessionFormList200Response>

    /**
     * POST api/session/{sessionID}/form/{formID}/reply
     * Reply to form
     * Submit an answer to a pending form.
     * Responses:
     *  - 204: <No Content>
     *  - 400: FormInvalidAnswerError | InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | FormNotFoundError | LocationNotFoundError
     *  - 409: FormAlreadySettledError
     *
     * @param sessionID 
     * @param formID 
     * @param formReply 
     * @return [Unit]
     */
    @POST("api/session/{sessionID}/form/{formID}/reply")
    suspend fun sessionFormReply(@Path("sessionID") sessionID: kotlin.String, @Path("formID") formID: kotlin.String, @Body formReply: FormReply): Response<Unit>

    /**
     * POST api/session/{sessionID}/generate
     * Generate text from session context
     * Generate transient text from the current session context without mutating session history.
     * Responses:
     *  - 200: SessionGenerateResponse
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *  - 503: ServiceUnavailableError
     *
     * @param sessionID 
     * @param sessionGenerateRequest 
     * @return [SessionGenerateResponse]
     */
    @POST("api/session/{sessionID}/generate")
    suspend fun sessionGenerate(@Path("sessionID") sessionID: kotlin.String, @Body sessionGenerateRequest: SessionGenerateRequest): Response<SessionGenerateResponse>

    /**
     * GET api/session/{sessionID}
     * Get session
     * Retrieve a session by ID.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError
     *
     * @param sessionID 
     * @return [SessionCreate200Response]
     */
    @GET("api/session/{sessionID}")
    suspend fun sessionGet(@Path("sessionID") sessionID: kotlin.String): Response<SessionCreate200Response>

    /**
     * DELETE api/session/{sessionID}/inbox/{inboxID}
     * Cancel inbox input
     * Cancel an inbox item that has not yet been delivered. Unavailable items are a no-op.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError
     *
     * @param sessionID 
     * @param inboxID 
     * @return [Unit]
     */
    @DELETE("api/session/{sessionID}/inbox/{inboxID}")
    suspend fun sessionInboxCancel(@Path("sessionID") sessionID: kotlin.String, @Path("inboxID") inboxID: kotlin.String): Response<Unit>

    /**
     * GET api/session/{sessionID}/inbox
     * List session inbox
     * List durable enqueued session work not yet delivered, ordered by enqueue sequence. Includes user, synthetic, compaction, and move items.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError
     *
     * @param sessionID 
     * @return [SessionInboxList200Response]
     */
    @GET("api/session/{sessionID}/inbox")
    suspend fun sessionInboxList(@Path("sessionID") sessionID: kotlin.String): Response<SessionInboxList200Response>

    /**
     * PATCH api/session/{sessionID}/inbox/{inboxID}
     * Update inbox item
     * Change a pending inbox item&#39;s delivery mode. Steering wakes session execution.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError
     *  - 409: ConflictError
     *
     * @param sessionID 
     * @param inboxID 
     * @param sessionInboxUpdateRequest 
     * @return [Unit]
     */
    @PATCH("api/session/{sessionID}/inbox/{inboxID}")
    suspend fun sessionInboxUpdate(@Path("sessionID") sessionID: kotlin.String, @Path("inboxID") inboxID: kotlin.String, @Body sessionInboxUpdateRequest: SessionInboxUpdateRequest): Response<Unit>


    /**
    * enum for parameter resume
    */
    @Serializable
    enum class ResumeSessionInterrupt(val value: kotlin.String) {
        @SerialName(value = "true") TRUE("true"),
        @SerialName(value = "false") FALSE("false")
    }

    /**
     * POST api/session/{sessionID}/interrupt
     * Interrupt session execution
     * Interrupt active execution owned by this OpenCode process. Returns interrupted&#x3D;true when an active execution was interrupted and false for the idle no-op. When resume&#x3D;true, execution resumes pending steering input and next-in-line control items (manual compaction, moves) while queued prompts remain parked.
     * Responses:
     *  - 200: SessionInterruptResponse
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @param resume  (optional)
     * @return [SessionInterruptResponse]
     */
    @POST("api/session/{sessionID}/interrupt")
    suspend fun sessionInterrupt(@Path("sessionID") sessionID: kotlin.String, @Query("resume") resume: ResumeSessionInterrupt? = null): Response<SessionInterruptResponse>


    /**
    * enum for parameter order
    */
    @Serializable
    enum class OrderSessionList(val value: kotlin.String) {
        @SerialName(value = "asc") ASC("asc"),
        @SerialName(value = "desc") DESC("desc")
    }

    /**
     * GET api/session
     * List sessions
     * Retrieve sessions in the requested order. Items keep that order across pages; use cursor.next or cursor.previous to move through the ordered list.
     * Responses:
     *  - 200: SessionsResponse
     *  - 400: InvalidCursorError | InvalidRequestError
     *  - 401: UnauthorizedError
     *
     * @param limit  (optional)
     * @param order  (optional)
     * @param search  (optional)
     * @param parentID  (optional)
     * @param directory  (optional)
     * @param project  (optional)
     * @param subpath  (optional)
     * @param cursor  (optional)
     * @return [SessionsResponse]
     */
    @GET("api/session")
    suspend fun sessionList(@Query("limit") limit: kotlin.String? = null, @Query("order") order: OrderSessionList? = null, @Query("search") search: kotlin.String? = null, @Query("parentID") parentID: kotlin.String? = null, @Query("directory") directory: kotlin.String? = null, @Query("project") project: kotlin.String? = null, @Query("subpath") subpath: kotlin.String? = null, @Query("cursor") cursor: kotlin.String? = null): Response<SessionsResponse>


    /**
    * enum for parameter follow
    */
    @Serializable
    enum class FollowSessionLog(val value: kotlin.String) {
        @SerialName(value = "true") TRUE("true"),
        @SerialName(value = "false") FALSE("false")
    }

    /**
     * GET api/experimental/session/{sessionID}/log
     * Read the session log
     * Experimental durable session event log. Reads events after an exclusive aggregate sequence and continues with live events when follow&#x3D;true.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError
     *
     * @param sessionID 
     * @param after  (optional)
     * @param follow  (optional)
     * @return [SessionLog200Response]
     */
    @GET("api/experimental/session/{sessionID}/log")
    suspend fun sessionLog(@Path("sessionID") sessionID: kotlin.String, @Query("after") after: kotlin.String? = null, @Query("follow") follow: FollowSessionLog? = null): Response<SessionLog200Response>

    /**
     * GET api/session/{sessionID}/message/{messageID}
     * Get session message
     * Retrieve one projected message owned by the Session.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | MessageNotFoundError
     *
     * @param sessionID 
     * @param messageID 
     * @return [SessionMessageGet200Response]
     */
    @GET("api/session/{sessionID}/message/{messageID}")
    suspend fun sessionMessageGet(@Path("sessionID") sessionID: kotlin.String, @Path("messageID") messageID: kotlin.String): Response<SessionMessageGet200Response>


    /**
    * enum for parameter order
    */
    @Serializable
    enum class OrderSessionMessageList(val value: kotlin.String) {
        @SerialName(value = "asc") ASC("asc"),
        @SerialName(value = "desc") DESC("desc")
    }


    /**
    * enum for parameter type
    */
    @Serializable
    enum class TypeSessionMessageList(val value: kotlin.String) {
        @SerialName(value = "agent-switched") AGENT_SWITCHED("agent-switched"),
        @SerialName(value = "model-switched") MODEL_SWITCHED("model-switched"),
        @SerialName(value = "location-switched") LOCATION_SWITCHED("location-switched"),
        @SerialName(value = "user") USER("user"),
        @SerialName(value = "synthetic") SYNTHETIC("synthetic"),
        @SerialName(value = "system") SYSTEM("system"),
        @SerialName(value = "skill") SKILL("skill"),
        @SerialName(value = "shell") SHELL("shell"),
        @SerialName(value = "assistant") ASSISTANT("assistant"),
        @SerialName(value = "compaction") COMPACTION("compaction")
    }

    /**
     * GET api/session/{sessionID}/message
     * Get session messages
     * Retrieve projected messages for a session, optionally filtered by type. Items keep the requested order across pages; use cursor.next or cursor.previous to move through the ordered timeline, passing the same type filter on each page.
     * Responses:
     *  - 200: SessionMessagesResponse
     *  - 400: InvalidCursorError | InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError
     *  - 500: UnknownError
     *
     * @param sessionID 
     * @param limit  (optional)
     * @param order  (optional)
     * @param cursor  (optional)
     * @param type  (optional)
     * @return [SessionMessagesResponse]
     */
    @GET("api/session/{sessionID}/message")
    suspend fun sessionMessageList(@Path("sessionID") sessionID: kotlin.String, @Query("limit") limit: kotlin.String? = null, @Query("order") order: OrderSessionMessageList? = null, @Query("cursor") cursor: kotlin.String? = null, @Query("type") type: TypeSessionMessageList? = null): Response<SessionMessagesResponse>

    /**
     * POST api/session/{sessionID}/move
     * Move session
     * Move a session to another project directory at the requested delivery boundary.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError
     *
     * @param sessionID 
     * @param sessionMoveRequest 
     * @return [Unit]
     */
    @POST("api/session/{sessionID}/move")
    suspend fun sessionMove(@Path("sessionID") sessionID: kotlin.String, @Body sessionMoveRequest: SessionMoveRequest): Response<Unit>

    /**
     * POST api/session/{sessionID}/prompt
     * Send message
     * Durably admit one session input and schedule agent-loop execution unless resume is false.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *  - 409: ConflictError
     *
     * @param sessionID 
     * @param sessionPromptRequest 
     * @return [SessionPrompt200Response]
     */
    @POST("api/session/{sessionID}/prompt")
    suspend fun sessionPrompt(@Path("sessionID") sessionID: kotlin.String, @Body sessionPromptRequest: SessionPromptRequest): Response<SessionPrompt200Response>

    /**
     * DELETE api/session/{sessionID}
     * Delete session
     * Delete a session and its child sessions.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError
     *
     * @param sessionID 
     * @return [Unit]
     */
    @DELETE("api/session/{sessionID}")
    suspend fun sessionRemove(@Path("sessionID") sessionID: kotlin.String): Response<Unit>

    /**
     * DELETE api/session/{sessionID}/revert
     * Clear staged revert
     * 
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *  - 409: SessionBusyError
     *  - 500: UnknownError
     *
     * @param sessionID 
     * @return [Unit]
     */
    @DELETE("api/session/{sessionID}/revert")
    suspend fun sessionRevertClear(@Path("sessionID") sessionID: kotlin.String): Response<Unit>

    /**
     * POST api/session/{sessionID}/revert/commit
     * Commit staged revert
     * 
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *  - 409: SessionBusyError
     *
     * @param sessionID 
     * @return [Unit]
     */
    @POST("api/session/{sessionID}/revert/commit")
    suspend fun sessionRevertCommit(@Path("sessionID") sessionID: kotlin.String): Response<Unit>

    /**
     * POST api/session/{sessionID}/revert/stage
     * Stage session revert
     * Stage or move a reversible session boundary and optionally apply its file changes.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: MessageNotFoundError | SessionNotFoundError | LocationNotFoundError
     *  - 409: SessionBusyError
     *  - 500: UnknownError
     *
     * @param sessionID 
     * @param sessionRevertStageRequest 
     * @return [SessionRevertStage200Response]
     */
    @POST("api/session/{sessionID}/revert/stage")
    suspend fun sessionRevertStage(@Path("sessionID") sessionID: kotlin.String, @Body sessionRevertStageRequest: SessionRevertStageRequest): Response<SessionRevertStage200Response>

    /**
     * POST api/session/{sessionID}/shell
     * Run shell command
     * Execute one shell command in the session&#39;s working directory. Emits a shell.started event before execution and a shell.ended event with the merged output after.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @param sessionShellRequest 
     * @return [Unit]
     */
    @POST("api/session/{sessionID}/shell")
    suspend fun sessionShell(@Path("sessionID") sessionID: kotlin.String, @Body sessionShellRequest: SessionShellRequest): Response<Unit>

    /**
     * POST api/session/{sessionID}/agent
     * Switch session agent
     * Switch the agent used by subsequent provider turns.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @param sessionSwitchAgentRequest 
     * @return [Unit]
     */
    @POST("api/session/{sessionID}/agent")
    suspend fun sessionSwitchAgent(@Path("sessionID") sessionID: kotlin.String, @Body sessionSwitchAgentRequest: SessionSwitchAgentRequest): Response<Unit>

    /**
     * POST api/session/{sessionID}/model
     * Switch session model
     * Switch the model used by subsequent provider turns.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @param sessionSwitchModelRequest 
     * @return [Unit]
     */
    @POST("api/session/{sessionID}/model")
    suspend fun sessionSwitchModel(@Path("sessionID") sessionID: kotlin.String, @Body sessionSwitchModelRequest: SessionSwitchModelRequest): Response<Unit>

    /**
     * POST api/session/{sessionID}/synthetic
     * Add synthetic message
     * Durably admit synthetic session input and schedule execution unless resume is false.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *  - 409: ConflictError
     *
     * @param sessionID 
     * @param sessionSyntheticRequest 
     * @return [SessionSynthetic200Response]
     */
    @POST("api/session/{sessionID}/synthetic")
    suspend fun sessionSynthetic(@Path("sessionID") sessionID: kotlin.String, @Body sessionSyntheticRequest: SessionSyntheticRequest): Response<SessionSynthetic200Response>

    /**
     * PATCH api/session/{sessionID}
     * Update session
     * Update mutable session properties.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError | LocationNotFoundError
     *
     * @param sessionID 
     * @param sessionUpdateRequest 
     * @return [Unit]
     */
    @PATCH("api/session/{sessionID}")
    suspend fun sessionUpdate(@Path("sessionID") sessionID: kotlin.String, @Body sessionUpdateRequest: SessionUpdateRequest): Response<Unit>

    /**
     * POST api/session/{sessionID}/view
     * View session
     * Mark the idle transition observed by the viewer as viewed.
     * Responses:
     *  - 204: <No Content>
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: SessionNotFoundError
     *
     * @param sessionID 
     * @param sessionViewRequest 
     * @return [Unit]
     */
    @POST("api/session/{sessionID}/view")
    suspend fun sessionView(@Path("sessionID") sessionID: kotlin.String, @Body sessionViewRequest: SessionViewRequest): Response<Unit>

}
