# Release shrinking rules. Libraries (Retrofit, kotlinx.serialization, Room, Koin, Compose)
# ship their own consumer rules; only gaps are listed here.

# Retrofit reads each suspend method's response type from its generic signature. A response
# class the app never reads (e.g. the prompt result) would be removed, turning the type into
# Object ("Unable to create converter"): keep every generated model, still obfuscated.
-keep,allowobfuscation class io.github.beppe2880.opencodemobile.api.models.** { *; }

# Tink (used by androidx.security-crypto for the saved server passwords) references
# compile-only Error Prone annotations.
-dontwarn com.google.errorprone.annotations.**
