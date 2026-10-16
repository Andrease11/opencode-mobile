package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.PairingCode
import io.github.beppe2880.opencodemobile.api.models.PairingSession
import io.github.beppe2880.opencodemobile.api.models.ServerInfo
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded

interface ServerApi {
    /**
     * GET auth/connect/{code}
     * Redeem pairing code
     * Redeem a pairing code. Browsers receive a session cookie and a redirect to the web app; requests that accept JSON receive a session token to use as the password.
     * Responses:
     *  - 200: PairingSession
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *
     * @param code 
     * @return [PairingSession]
     */
    @GET("auth/connect/{code}")
    suspend fun serverConnect(@Path("code") code: kotlin.String): Response<PairingSession>

    /**
     * GET api/info
     * Get server info
     * Return the server identity, connection URLs, paths, and readiness status.
     * Responses:
     *  - 200: ServerInfo
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *
     * @return [ServerInfo]
     */
    @GET("api/info")
    suspend fun serverInfo(): Response<ServerInfo>

    /**
     * POST api/pair
     * Create pairing code
     * Create a short-lived, single-use code for a /auth/connect/:code pairing link.
     * Responses:
     *  - 200: PairingCode
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *
     * @return [PairingCode]
     */
    @POST("api/pair")
    suspend fun serverPair(): Response<PairingCode>

}
