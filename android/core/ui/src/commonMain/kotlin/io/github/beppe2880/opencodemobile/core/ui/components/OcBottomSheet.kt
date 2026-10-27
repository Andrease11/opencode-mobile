package io.github.beppe2880.opencodemobile.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.beppe2880.opencodemobile.core.ui.theme.OcRadius
import io.github.beppe2880.opencodemobile.core.ui.theme.OcTheme

/** Modal sheet with token colors. [skipPartiallyExpanded] for forms and long pickers. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    skipPartiallyExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = OcTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded),
        modifier = (if (testTag != null) modifier.testTag(testTag) else modifier)
            .statusBarsPadding()
            .exposeTestTags(),
        shape = RoundedCornerShape(topStart = OcRadius.sheet, topEnd = OcRadius.sheet),
        containerColor = c.bgBase,
        contentColor = c.textBase,
        scrimColor = c.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = c.layer04) },
        content = content,
    )
}
