package io.github.beppe2880.opencodemobile.core.domain

data class PermissionRequest(
    val id: String,
    val sessionId: String,
    val action: String,
    val resources: List<String>,
    val message: String?,
    val canSave: Boolean,
)

enum class PermissionDecision(val wire: String) {
    Once("once"),
    Always("always"),
    Reject("reject"),
}
