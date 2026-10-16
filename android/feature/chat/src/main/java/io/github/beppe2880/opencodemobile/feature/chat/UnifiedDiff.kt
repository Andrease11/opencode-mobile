package io.github.beppe2880.opencodemobile.feature.chat

internal enum class DiffLineKind { Hunk, Context, Added, Removed }

/** One rendered diff line; [oldLine]/[newLine] are 1-based numbers on each side, null when absent. */
internal data class DiffLine(val kind: DiffLineKind, val oldLine: Int?, val newLine: Int?, val text: String)

/** Parses unified diff hunks; file headers (`Index:`, `---`, `+++`) before the first hunk are skipped. */
internal object UnifiedDiff {

    private val HUNK = Regex("""^@@ -(\d+)(?:,\d+)? \+(\d+)(?:,\d+)? @@(.*)$""")

    fun parse(patch: String): List<DiffLine> {
        val out = ArrayList<DiffLine>()
        var oldLine = 0
        var newLine = 0
        var inHunk = false
        for (raw in patch.lineSequence()) {
            val hunk = HUNK.find(raw)
            if (hunk != null) {
                oldLine = hunk.groupValues[1].toInt()
                newLine = hunk.groupValues[2].toInt()
                inHunk = true
                out += DiffLine(DiffLineKind.Hunk, null, null, raw)
                continue
            }
            if (!inHunk) continue
            when (raw.firstOrNull()) {
                '+' -> out += DiffLine(DiffLineKind.Added, null, newLine++, raw.drop(1))
                '-' -> out += DiffLine(DiffLineKind.Removed, oldLine++, null, raw.drop(1))
                ' ' -> out += DiffLine(DiffLineKind.Context, oldLine++, newLine++, raw.drop(1))
                // "\ No newline at end of file" and the trailing empty line carry no content.
                else -> Unit
            }
        }
        return out
    }
}
