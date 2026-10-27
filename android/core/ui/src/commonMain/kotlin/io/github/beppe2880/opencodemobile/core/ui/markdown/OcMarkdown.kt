package io.github.beppe2880.opencodemobile.core.ui.markdown

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.compose.elements.MarkdownTable
import com.mikepenz.markdown.compose.elements.MarkdownTableHeader
import com.mikepenz.markdown.compose.elements.MarkdownTableRow
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxThemes
import io.github.beppe2880.opencodemobile.core.ui.theme.OcColors
import io.github.beppe2880.opencodemobile.core.ui.theme.OcMonoTypography
import io.github.beppe2880.opencodemobile.core.ui.theme.OcTheme

/**
 * Renders assistant markdown with highlighted code fences (with copy header).
 * Parsing goes through [cache] so only the streaming message re-parses as it grows.
 */
@Composable
fun OcMarkdown(
    id: String,
    content: String,
    cache: MarkdownCache,
    modifier: Modifier = Modifier,
) {
    val colors = OcTheme.colors
    val typography = MaterialTheme.typography
    val mono = OcTheme.mono
    val highlights = remember(colors.isDark) {
        Highlights.Builder().theme(SyntaxThemes.atom(darkMode = colors.isDark))
    }
    val components = remember(highlights) {
        markdownComponents(
            codeFence = {
                MarkdownHighlightedCodeFence(
                    content = it.content,
                    node = it.node,
                    style = it.typography.code,
                    highlightsBuilder = highlights,
                    showHeader = true,
                )
            },
            codeBlock = {
                MarkdownHighlightedCodeBlock(
                    content = it.content,
                    node = it.node,
                    style = it.typography.code,
                    highlightsBuilder = highlights,
                    showHeader = true,
                )
            },
            // Cells wrap instead of being cut to one line: on a phone a cut cell cannot be read
            // even by scrolling the table sideways.
            table = {
                MarkdownTable(
                    content = it.content,
                    node = it.node,
                    style = it.typography.table,
                    headerBlock = { content, header, tableWidth, style ->
                        MarkdownTableHeader(content, header, tableWidth, style, maxLines = Int.MAX_VALUE, overflow = TextOverflow.Clip)
                    },
                    rowBlock = { content, row, tableWidth, style ->
                        MarkdownTableRow(
                            content, row, tableWidth, style,
                            verticalAlignment = Alignment.Top,
                            maxLines = Int.MAX_VALUE,
                            overflow = TextOverflow.Clip,
                        )
                    },
                )
            },
        )
    }
    val mdColors = remember(colors) { markdownColors(colors) }
    val mdTypography = remember(typography, colors, mono) { markdownTypography(typography, mono, colors) }
    val state = cache.stateFor(id, content)
    Markdown(
        state = state,
        colors = mdColors,
        typography = mdTypography,
        components = components,
        modifier = modifier,
    )
}

private fun markdownColors(c: OcColors): MarkdownColors = DefaultMarkdownColors(
    text = c.textBase,
    codeBackground = if (c.isDark) c.layer01 else c.layer02,
    inlineCodeBackground = c.layer02,
    dividerColor = c.borderBase,
    tableBackground = if (c.isDark) c.layer01 else c.layer02,
)

private fun markdownTypography(t: Typography, mono: OcMonoTypography, c: OcColors): MarkdownTypography {
    val body: TextStyle = t.bodyMedium.copy(color = c.textBase)
    return DefaultMarkdownTypography(
        h1 = t.titleLarge,
        h2 = t.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
        h3 = t.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
        h4 = t.titleSmall,
        h5 = body,
        h6 = body,
        text = body,
        code = mono.codeSmall,
        inlineCode = body.merge(mono.code.copy(fontSize = body.fontSize * 0.92f)),
        quote = body.copy(color = c.textMuted),
        paragraph = body,
        ordered = body,
        bullet = body,
        list = body,
        textLink = TextLinkStyles(
            style = body.copy(color = c.textAccent, textDecoration = TextDecoration.Underline).toSpanStyle(),
        ),
        table = t.bodySmall,
    )
}
