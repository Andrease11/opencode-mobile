package io.github.beppe2880.opencodemobile.core.network

import io.github.beppe2880.opencodemobile.api.apis.ServerApi
import io.github.beppe2880.opencodemobile.core.domain.OpenCodeError
import io.github.beppe2880.opencodemobile.core.domain.ServerProfile
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** HTTP Basic credentials. [toString] never reveals the secret. */
class BasicCredentials(val username: String = ServerProfile.DEFAULT_USERNAME, val password: String) {
    internal val header: String get() = Credentials.basic(username, password, Charsets.UTF_8)
    override fun toString(): String = "BasicCredentials(username=$username)"
}

/**
 * Builds [OpenCodeClient]s that share one connection pool. With [debugLogging] only request
 * lines and headers are logged, with `Authorization` redacted; bodies (prompts, tokens) never are.
 */
class OpenCodeClientFactory(debugLogging: Boolean = false) {

    private val logging: Interceptor? = if (debugLogging) {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
            redactHeader("Authorization")
            redactHeader("Set-Cookie")
        }
    } else {
        null
    }

    private val baseHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(
            Interceptor { chain ->
                val request = chain.request()
                // Required by /auth/connect to answer with JSON instead of an HTML page.
                if (request.header("Accept") != null) return@Interceptor chain.proceed(request)
                chain.proceed(request.newBuilder().header("Accept", "application/json").build())
            },
        )
        .build()

    fun create(baseUrl: String, credentials: BasicCredentials?): OpenCodeClient {
        val http = baseHttp.newBuilder()
            .apply {
                if (credentials != null) addInterceptor(BasicAuthInterceptor(credentials))
                // After auth so the logger sees, and redacts, the header.
                logging?.let(::addInterceptor)
            }
            .build()
        return OpenCodeClient(baseUrl, http, retrofit(baseUrl, http))
    }

    /** Exchanges a single-use pairing [code] for a session token. Never logged: the URL holds the code. */
    suspend fun redeemPairing(baseUrl: String, code: String): String = try {
        val api = retrofit(baseUrl, baseHttp).create(ServerApi::class.java)
        awaitBody { api.serverConnect(code) }.token
    } catch (e: OpenCodeError) {
        throw pairingError(e)
    }

    private fun retrofit(baseUrl: String, http: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl.trimEnd('/') + "/")
        .client(http)
        .addConverterFactory(EnumQueryConverterFactory)
        .addConverterFactory(TolerantPagesConverterFactory(OpenCodeJson))
        .addConverterFactory(OpenCodeJson.asConverterFactory("application/json".toMediaType()))
        .build()
}

private class BasicAuthInterceptor(private val credentials: BasicCredentials) : Interceptor {
    override fun intercept(chain: Interceptor.Chain) =
        chain.proceed(chain.request().newBuilder().header("Authorization", credentials.header).build())
}
