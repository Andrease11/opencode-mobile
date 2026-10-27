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

    // Least recently used first: hits are moved to the end, the head is evicted.
    private val cache = LinkedHashMap<String, Entry>()

    fun stateFor(id: String, content: String): State {
        val cached = cache.remove(id)
        if (cached != null && cached.content == content) {
            cache[id] = cached
            return cached.state
        }
        val state = try {
            State.Success(parser.buildMarkdownTreeFromString(content), content, linksLookedUp = false, ReferenceLinkHandlerImpl())
        } catch (error: Throwable) {
            State.Error(error, ReferenceLinkHandlerImpl())
        }
        cache[id] = Entry(content, state)
        if (cache.size > MAX_ENTRIES) cache.remove(cache.keys.first())
        return state
    }

    private companion object {
        const val MAX_ENTRIES = 300
    }
}

@Composable
fun rememberMarkdownCache(): MarkdownCache = remember { MarkdownCache() }
