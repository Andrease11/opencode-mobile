package io.github.beppe2880.opencodemobile.core.ui.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mikepenz.markdown.model.ReferenceLinkHandlerImpl
import com.mikepenz.markdown.model.State
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

/**
 * Parses markdown once per (id, content) pair. Held above the transcript list so
 * recycled items reuse the parsed tree; only the streaming part re-parses.
 */
class MarkdownCache {
    private val parser = MarkdownParser(GFMFlavourDescriptor())

    private data class Entry(val content: String, val state: State)

    private val cache = object : LinkedHashMap<String, Entry>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Entry>) = size > MAX_ENTRIES
    }

    fun stateFor(id: String, content: String): State {
        val cached = cache[id]
        if (cached != null && cached.content == content) return cached.state
        val state = try {
            State.Success(parser.buildMarkdownTreeFromString(content), content, linksLookedUp = false, ReferenceLinkHandlerImpl())
        } catch (error: Throwable) {
            State.Error(error, ReferenceLinkHandlerImpl())
        }
        cache[id] = Entry(content, state)
        return state
    }

    private companion object {
        const val MAX_ENTRIES = 300
    }
}

@Composable
fun rememberMarkdownCache(): MarkdownCache = remember { MarkdownCache() }
