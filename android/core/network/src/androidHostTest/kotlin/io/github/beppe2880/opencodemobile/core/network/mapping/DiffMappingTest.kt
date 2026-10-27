package io.github.beppe2880.opencodemobile.core.network.mapping

import io.github.beppe2880.opencodemobile.core.domain.FileDiff
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiffMappingTest {

    private fun json(text: String): JsonObject = Json.parseToJsonElement(text).jsonObject

    @Test
    fun `edit metadata files become file diffs`() {
        val metadata = json(
            """{"truncated":false,"files":[{"file":"src/A.kt","patch":"@@ -1 +1 @@\n-a\n+b\n","status":"modified","additions":1,"deletions":1}]}""",
        )

        val diffs = toolDiffs("edit", json("""{"path":"src/A.kt","oldString":"a","newString":"b"}"""), metadata)

        assertEquals(listOf(FileDiff("src/A.kt", "modified", 1, 1, "@@ -1 +1 @@\n-a\n+b\n")), diffs)
    }

    @Test
    fun `write without server diff shows the content as added lines`() {
        val diffs = toolDiffs("write", json("""{"path":"notes.md","content":"one\ntwo\n"}"""), json("""{"truncated":false}"""))

        assertEquals(listOf(FileDiff("notes.md", FileDiff.WRITTEN, 2, 0, "@@ -0,0 +1,2 @@\n+one\n+two")), diffs)
    }

    @Test
    fun `other tools and malformed metadata have no diffs`() {
        assertTrue(toolDiffs("read", json("""{"path":"a"}"""), null).isEmpty())
        assertTrue(json("""{"files":[{"file":"a"},"x"]}""").fileDiffs().isEmpty())
    }
}
