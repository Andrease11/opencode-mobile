package io.github.beppe2880.opencodemobile.feature.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import io.github.beppe2880.opencodemobile.core.ui.markdown.rememberMarkdownCache
import io.github.beppe2880.opencodemobile.core.ui.theme.OcTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Real Compose layout: growing a single row must not move the paragraph being read. */
class TranscriptScrollTest {
    @get:Rule val compose = createComposeRule()

    private var state by mutableStateOf(chat(60))
    private lateinit var list: LazyListState
    private val events = mutableListOf<ChatEvent>()
    private var viewportHeight by mutableStateOf(450.dp)

    @Test
    fun growingResponseKeepsReadingPositionAndShowsNewContent() {
        showTranscript()
        readEarlier()
        assertReadingStaysStill { state = chat(70) }
        compose.onNodeWithTag("chat_new_messages_button").assertExists()
    }

    @Test
    fun atBottomFollowsGrowthWithinTheSameRow() {
        showTranscript()
        assertAtBottom()
        repeat(3) { update { state = chat(65 + it * 5) }; assertAtBottom() }
        compose.onNodeWithTag("chat_new_messages_button").assertDoesNotExist()
        compose.onNodeWithTag("chat_jump_latest_button").assertDoesNotExist()
    }

    @Test
    fun atBottomFollowsNewRowsAndViewportChanges() {
        showTranscript()
        update { state = state.copy(items = state.items + ChatItem.Text("next", "Another response", true)) }
        assertAtBottom()
        update { viewportHeight = 300.dp }
        assertAtBottom()
        update { viewportHeight = 450.dp }
        assertAtBottom()
    }

    @Test
    fun viewportChangesDoNotResumeFollowingWhileReading() {
        showTranscript()
        readEarlier()
        update { viewportHeight = 300.dp }
        compose.runOnIdle { assertTrue(list.canScrollForward) }
        assertReadingStaysStill { state = chat(70) }
        compose.onNodeWithTag("chat_new_messages_button").assertExists()
    }

    @Test
    fun shellOutputUpdatesBelowReadingPositionAreFlaggedWithoutMovingText() {
        state = chat(60).copy(items = chat(60).items + ChatItem.Shell("shell", "test", "Initial output", "running", null))
        showTranscript()
        readEarlier()
        assertReadingStaysStill {
            state = state.copy(items = chat(60).items + ChatItem.Shell("shell", "test", "More output\n".repeat(20), "running", null))
        }
        compose.onNodeWithTag("chat_new_messages_button").assertExists()
    }

    @Test
    fun jumpToLatestResumesFollowing() {
        showTranscript()
        readEarlier()
        update { state = chat(65) }
        compose.onNodeWithTag("chat_new_messages_button").performClick()
        compose.waitForIdle()
        assertAtBottom()
        update { state = chat(70) }
        assertAtBottom()
        compose.onNodeWithTag("chat_new_messages_button").assertDoesNotExist()
    }

    @Test
    fun manuallyReturningToBottomResumesFollowing() {
        showTranscript()
        readEarlier()
        update { state = chat(62) }
        repeat(4) { compose.onNodeWithTag("chat_transcript").performTouchInput { swipeUp() } }
        compose.waitForIdle()
        assertAtBottom()
        update { state = chat(70) }
        assertAtBottom()
        compose.onNodeWithTag("chat_new_messages_button").assertDoesNotExist()
    }

    @Test
    fun appendingAnotherRowDoesNotMoveReadingPosition() {
        showTranscript()
        readEarlier()
        assertReadingStaysStill { state = state.copy(items = state.items + ChatItem.Text("next", "Another response", true)) }
        compose.onNodeWithTag("chat_new_messages_button").assertExists()
    }

    @Test
    fun loadingOlderHistoryKeepsAnchorAndDoesNotCountAsNewContent() {
        showTranscript()
        readEarlier()
        assertReadingStaysStill { state = state.copy(loadingOlder = true) }
        assertReadingStaysStill {
            state = state.copy(loadingOlder = false, items = listOf(ChatItem.Text("older", "Older history", false)) + state.items)
        }
        compose.onNodeWithTag("chat_new_messages_button").assertDoesNotExist()
    }

    @Test
    fun completionDoesNotJumpToBottomWhileReading() {
        showTranscript()
        readEarlier()
        assertReadingStaysStill {
            state = state.copy(running = false, items = listOf((state.items.single() as ChatItem.Text).copy(streaming = false)))
        }
        compose.runOnIdle { assertTrue(list.canScrollForward) }
    }

    @Test
    fun savedReadingPositionIsRestoredAfterSwitchingSessions() {
        showTranscript()
        readEarlier()
        val saved = events.filterIsInstance<ChatEvent.ScrollMoved>().last().anchor!!
        update { state = chat(5).copy(sessionId = "another-session") }
        assertAtBottom()
        update { state = chat(60).copy(savedScroll = saved) }
        compose.runOnIdle {
            assertEquals(saved.offset, list.firstVisibleItemScrollOffset)
            assertTrue(list.canScrollForward)
        }
        compose.onNodeWithTag("chat_new_messages_button").assertDoesNotExist()
    }

    @Test
    fun unreadIndicatorDoesNotLeakIntoAnotherSession() {
        showTranscript()
        readEarlier()
        update { state = chat(70) }
        compose.onNodeWithTag("chat_new_messages_button").assertExists()
        update { state = chat(5).copy(sessionId = "another-session") }
        assertAtBottom()
        compose.onNodeWithTag("chat_new_messages_button").assertDoesNotExist()
    }

    @Test
    fun switchingSessionsWithIdenticalRowsStillStartsAtTheBottom() {
        showTranscript()
        readEarlier()
        update { state = state.copy(sessionId = "identical-content") }
        assertAtBottom()
    }

    @Test
    fun missingSavedAnchorFallsBackToLatest() {
        state = chat(60).copy(savedScroll = ScrollMemory.Anchor("removed", 200))
        showTranscript()
        assertAtBottom()
    }

    @Test
    fun initialServerSyncIsNotReportedAsNewContent() {
        state = chat(60).copy(synced = false, savedScroll = ScrollMemory.Anchor("answer", 100))
        showTranscript()
        update { state = chat(70).copy(savedScroll = state.savedScroll) }
        compose.onNodeWithTag("chat_new_messages_button").assertDoesNotExist()
    }

    @Test
    fun olderHistoryIsRequestedOnlyNearTheTop() {
        state = chat(60).copy(items = (1..60).map { ChatItem.Text("row-$it", "Reading paragraph $it. A finished message.", false) })
        showTranscript()
        compose.runOnIdle { assertFalse(events.any { it == ChatEvent.LoadOlder }) }
        compose.onNodeWithTag("chat_transcript").performTouchInput { swipeDown() }
        update { state = state.copy(savedScroll = ScrollMemory.Anchor("row-1", 0), sessionId = "top-session") }
        compose.runOnIdle { assertTrue(events.any { it == ChatEvent.LoadOlder }) }
    }

    private fun readEarlier() {
        compose.onNodeWithTag("chat_transcript").performTouchInput { swipeDown() }
        compose.waitForIdle()
        compose.runOnIdle { assertTrue("Gesture must move away from the bottom", list.canScrollForward) }
    }

    private fun assertReadingStaysStill(change: () -> Unit) {
        val paragraph = compose.onAllNodes(hasText("Reading paragraph", substring = true), useUnmergedTree = true)
            .fetchSemanticsNodes().first { it.boundsInRoot.height > 20 && it.boundsInRoot.top > 40 }
        val text = paragraph.config[SemanticsProperties.Text].single().text
        val before = paragraph.boundsInRoot.top
        update(change)
        val after = compose.onNodeWithText(text, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.top
        assertEquals("The paragraph being read moved when content changed", before, after, 1f)
    }

    private fun update(change: () -> Unit) {
        compose.runOnIdle(change)
        compose.waitForIdle()
    }

    private fun assertAtBottom() = compose.runOnIdle { assertFalse("Transcript must follow the latest content", list.canScrollForward) }

    private fun showTranscript() {
        compose.setContent {
            OcTheme {
                list = key(state.sessionId) { rememberLazyListState() }
                Box(Modifier.height(viewportHeight)) {
                    Transcript(state, events::add, {}, list, rememberMarkdownCache())
                }
            }
        }
        compose.waitForIdle()
    }

    private companion object {
        fun chat(paragraphs: Int) = ChatState(
            sessionId = "scroll-regression",
            loading = false,
            synced = true,
            running = true,
            hasOlder = false,
            items = listOf(
                ChatItem.Text(
                    "answer",
                    (1..paragraphs).joinToString("\n\n") {
                        "Reading paragraph $it. Stable text for the scrolling regression."
                    },
                    true,
                ),
            ),
        )
    }
}
