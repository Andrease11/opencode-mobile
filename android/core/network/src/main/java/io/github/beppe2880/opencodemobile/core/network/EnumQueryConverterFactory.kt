package io.github.beppe2880.opencodemobile.core.network

import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Writes generated enums in query and path parameters by their wire name (`desc`), since
 * Retrofit would otherwise use the Kotlin constant name (`DESC`).
 */
internal object EnumQueryConverterFactory : Converter.Factory() {
    override fun stringConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<*, String>? {
        if (type !is Class<*> || !type.isEnum) return null
        val serializer = OpenCodeJson.serializersModule.serializer(type)
        return Converter<Any, String> { value -> OpenCodeJson.encodeToJsonElement(serializer, value).jsonPrimitive.content }
    }
}
