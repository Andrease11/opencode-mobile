package io.github.beppe2880.opencodemobile.core.domain

/** Runtime connection state; connecting or disconnecting does not edit server configuration. */
data class McpServer(val name: String, val status: McpStatus)

enum class McpStatus { Connected, Pending, Disabled, Failed, NeedsAuth }
