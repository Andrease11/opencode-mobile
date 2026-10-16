package io.github.beppe2880.opencodemobile.api.apis

import io.github.beppe2880.opencodemobile.api.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import okhttp3.ResponseBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import io.github.beppe2880.opencodemobile.api.models.ExperimentalFsWrite200Response
import io.github.beppe2880.opencodemobile.api.models.FsFind200Response
import io.github.beppe2880.opencodemobile.api.models.FsList200Response
import io.github.beppe2880.opencodemobile.api.models.FsRead404Response
import io.github.beppe2880.opencodemobile.api.models.InvalidRequestErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.LocationNotFoundErrorEncoded
import io.github.beppe2880.opencodemobile.api.models.UnauthorizedErrorEncoded

import okhttp3.MultipartBody

interface FilesystemApi {
    /**
     * POST api/experimental/fs/write
     * Write file
     * Write the raw request body to an absolute path or a path relative to the requested location, creating parent directories, and return the resolved absolute path. Unlike read, the target is not confined to the location. Experimental: may change without compatibility guarantees.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param path 
     * @param body 
     * @param locationDirectory  (optional)
     * @return [ExperimentalFsWrite200Response]
     */
    @POST("api/experimental/fs/write")
    suspend fun experimentalFsWrite(@Query("path") path: kotlin.String, @Body body: java.io.File, @Query("location[directory]") locationDirectory: kotlin.String? = null): Response<ExperimentalFsWrite200Response>


    /**
    * enum for parameter type
    */
    @Serializable
    enum class TypeFsFind(val value: kotlin.String) {
        @SerialName(value = "file") FILE("file"),
        @SerialName(value = "directory") DIRECTORY("directory")
    }

    /**
     * GET api/fs/find
     * Find files
     * Find recursively ranked filesystem entries relative to the requested location.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: LocationNotFoundError
     *
     * @param query 
     * @param locationDirectory  (optional)
     * @param type  (optional)
     * @param limit  (optional)
     * @return [FsFind200Response]
     */
    @GET("api/fs/find")
    suspend fun fsFind(@Query("query") query: kotlin.String, @Query("location[directory]") locationDirectory: kotlin.String? = null, @Query("type") type: TypeFsFind? = null, @Query("limit") limit: kotlin.String? = null): Response<FsFind200Response>

    /**
     * GET api/fs/list
     * List directory
     * List direct children using an absolute path or a path relative to the requested location, including parents and siblings outside its directory. Entry paths remain relative to the requested location; listing does not switch locations.
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: FileNotFoundError | LocationNotFoundError
     *
     * @param locationDirectory  (optional)
     * @param path  (optional)
     * @return [FsList200Response]
     */
    @GET("api/fs/list")
    suspend fun fsList(@Query("location[directory]") locationDirectory: kotlin.String? = null, @Query("path") path: kotlin.String? = null): Response<FsList200Response>

    /**
     * GET api/fs/read/<*>
     * Read file
     * Stream one file relative to the requested location with ETag, Last-Modified, and single-range HTTP Range support (206 Partial Content, 416 Range Not Satisfiable, and 304 Not Modified; multi-range and malformed Range headers fall back to 200).
     * Responses:
     *  - 200: Success
     *  - 400: InvalidRequestError
     *  - 401: UnauthorizedError
     *  - 404: FileNotFoundError | LocationNotFoundError
     *
     * @param locationDirectory  (optional)
     * @return [ResponseBody]
     */
    @GET("api/fs/read/*")
    suspend fun fsRead(@Query("location[directory]") locationDirectory: kotlin.String? = null): Response<ResponseBody>

}
