package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.LocationNotFoundErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.SkillList200Response
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded

interface SkillApi {
    /**
     * GET api/skill
     * List skills
     * Retrieve currently registered skills.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param locationDirectory  (optional)
     * @return [SkillList200Response]
     */
    @GET("api/skill")
    suspend fun skillList(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<SkillList200Response>

}
