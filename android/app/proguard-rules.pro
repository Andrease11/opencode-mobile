# Release shrinking rules. Libraries (Ktor, kotlinx.serialization, Room, Koin, Compose)
# ship their own consumer rules; only gaps are listed here.

# Tink (used by androidx.security-crypto for the saved server passwords) references
# compile-only Error Prone annotations.
-dontwarn com.google.errorprone.annotations.**
