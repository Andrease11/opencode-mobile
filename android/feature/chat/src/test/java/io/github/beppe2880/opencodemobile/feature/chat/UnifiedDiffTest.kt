package io.github.beppe2880.opencodemobile.feature.chat

import org.junit.Assert.assertEquals
import org.junit.Test

class UnifiedDiffTest {

    @Test
    fun `parses hunks with line numbers and skips file headers`() {
        val patch = listOf(
            "Index: a/Session.kt",
            "===================================================================",
            "--- a/Session.kt",
            "+++ a/Session.kt",
            "@@ -10,3 +10,4 @@ class Session",
            " val a = 1",
            "-val b = 2",
            "+val b = 3",
            "+val c = 4",
            " val d = 5",
            "\\ No newline at end of file",
            "",
        ).joinToString("\n")

        val lines = UnifiedDiff.parse(patch)

        assertEquals(
            listOf(
                DiffLine(DiffLineKind.Hunk, null, null, "@@ -10,3 +10,4 @@ class Session"),
                DiffLine(DiffLineKind.Context, 10, 10, "val a = 1"),
                DiffLine(DiffLineKind.Removed, 11, null, "val b = 2"),
                DiffLine(DiffLineKind.Added, null, 11, "val b = 3"),
                DiffLine(DiffLineKind.Added, null, 12, "val c = 4"),
                DiffLine(DiffLineKind.Context, 12, 13, "val d = 5"),
            ),
            lines,
        )
    }

    @Test
    fun `keeps numbering across multiple hunks`() {
        val lines = UnifiedDiff.parse("@@ -1 +1 @@\n-a\n+b\n@@ -40,2 +40,2 @@\n x\n-y\n+z")

        // hunk, -a, +b, hunk, " x", -y, +z
        assertEquals(listOf(null, 1, null, null, 40, 41, null), lines.map { it.oldLine })
        assertEquals(listOf(null, null, 1, null, 40, null, 41), lines.map { it.newLine })
    }
}
