package app.rutt.rutt

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.io.File
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentDir by remember { mutableStateOf(File(System.getProperty("user.dir"))) }
        var files by remember(currentDir) {
            mutableStateOf(currentDir.listFiles()?.toList()?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList())
        }
        var selectedIndex by remember(currentDir) { mutableStateOf(0) }
        var editingIndex by remember(currentDir) { mutableStateOf<Int?>(null) }
        var editingText by remember { mutableStateOf(TextFieldValue("")) }
        val listState = rememberLazyListState()
        val focusRequester = remember { FocusRequester() }
        val editFocusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        LaunchedEffect(editingIndex) {
            if (editingIndex != null) {
                editFocusRequester.requestFocus()
            } else {
                focusRequester.requestFocus()
            }
        }

        LaunchedEffect(selectedIndex) {
            if (files.isNotEmpty()) {
                val layoutInfo = listState.layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo
                if (visibleItems.isNotEmpty()) {
                    val fullyVisibleItems = visibleItems.filter {
                        it.offset >= layoutInfo.viewportStartOffset && (it.offset + it.size) <= layoutInfo.viewportEndOffset
                    }
                    if (fullyVisibleItems.isEmpty()) {
                        listState.scrollToItem(selectedIndex)
                    } else {
                        val firstFullyVisible = fullyVisibleItems.first().index
                        val lastFullyVisible = fullyVisibleItems.last().index

                        if (selectedIndex < firstFullyVisible) {
                            listState.scrollToItem(selectedIndex)
                        } else if (selectedIndex > lastFullyVisible) {
                            val itemHeight = visibleItems.first().size
                            val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                            val itemsThatFit = if (itemHeight > 0) maxOf(1, viewportHeight / itemHeight) else 1
                            listState.scrollToItem(maxOf(0, selectedIndex - itemsThatFit + 1))
                        }
                    }
                } else {
                    listState.scrollToItem(selectedIndex)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            var pathInput by remember(currentDir) { mutableStateOf(currentDir.absolutePath) }
            androidx.compose.foundation.text.BasicTextField(
                value = pathInput,
                onValueChange = { pathInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                            val newDir = File(pathInput)
                            if (newDir.exists() && newDir.isDirectory) {
                                currentDir = newDir
                                focusRequester.requestFocus()
                            }
                            true
                        } else {
                            false
                        }
                    },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
            )
            androidx.compose.material3.HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outline
            )
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
                    .focusable()
                    .onPreviewKeyEvent { event ->
                        if (editingIndex != null) return@onPreviewKeyEvent false
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.J -> {
                                    if (event.isShiftPressed) {
                                        val step = maxOf(1, listState.layoutInfo.visibleItemsInfo.size - 1)
                                        selectedIndex = minOf(files.size - 1, selectedIndex + step)
                                    } else {
                                        if (selectedIndex < files.size - 1) {
                                            selectedIndex++
                                        }
                                    }
                                    true
                                }
                                Key.K -> {
                                    if (event.isShiftPressed) {
                                        val step = maxOf(1, listState.layoutInfo.visibleItemsInfo.size - 1)
                                        selectedIndex = maxOf(0, selectedIndex - step)
                                    } else {
                                        if (selectedIndex > 0) {
                                            selectedIndex--
                                        }
                                    }
                                    true
                                }
                                Key.H -> {
                                    if (event.isShiftPressed) {
                                        val layoutInfo = listState.layoutInfo
                                        val fullyVisibleItems = layoutInfo.visibleItemsInfo.filter {
                                            it.offset >= layoutInfo.viewportStartOffset && (it.offset + it.size) <= layoutInfo.viewportEndOffset
                                        }
                                        if (fullyVisibleItems.isNotEmpty()) {
                                            selectedIndex = fullyVisibleItems.first().index
                                        }
                                    } else {
                                        currentDir.parentFile?.let {
                                            currentDir = it
                                        }
                                    }
                                    true
                                }
                                Key.M -> {
                                    if (event.isShiftPressed) {
                                        val layoutInfo = listState.layoutInfo
                                        val fullyVisibleItems = layoutInfo.visibleItemsInfo.filter {
                                            it.offset >= layoutInfo.viewportStartOffset && (it.offset + it.size) <= layoutInfo.viewportEndOffset
                                        }
                                        if (fullyVisibleItems.isNotEmpty()) {
                                            selectedIndex = fullyVisibleItems[fullyVisibleItems.size / 2].index
                                        }
                                        true
                                    } else {
                                        false
                                    }
                                }
                                Key.G -> {
                                    if (files.isNotEmpty()) {
                                        if (event.isShiftPressed) {
                                            selectedIndex = files.size - 1
                                        } else {
                                            selectedIndex = 0
                                        }
                                        true
                                    } else {
                                        false
                                    }
                                }
                                Key.L -> {
                                    if (event.isShiftPressed) {
                                        val layoutInfo = listState.layoutInfo
                                        val fullyVisibleItems = layoutInfo.visibleItemsInfo.filter {
                                            it.offset >= layoutInfo.viewportStartOffset && (it.offset + it.size) <= layoutInfo.viewportEndOffset
                                        }
                                        if (fullyVisibleItems.isNotEmpty()) {
                                            selectedIndex = fullyVisibleItems.last().index
                                        }
                                    } else {
                                        if (files.isNotEmpty() && files[selectedIndex].isDirectory) {
                                            currentDir = files[selectedIndex]
                                        }
                                    }
                                    true
                                }
                                Key.I -> {
                                    if (files.isNotEmpty()) {
                                        editingIndex = selectedIndex
                                        val name = files[selectedIndex].name
                                        editingText = TextFieldValue(name, TextRange(0))
                                    }
                                    true
                                }
                                Key.A -> {
                                    if (files.isNotEmpty()) {
                                        editingIndex = selectedIndex
                                        val name = files[selectedIndex].name
                                        editingText = TextFieldValue(name, TextRange(name.length))
                                    }
                                    true
                                }
                                Key.D -> {
                                    if (event.isShiftPressed && files.isNotEmpty()) {
                                        editingIndex = selectedIndex
                                        editingText = TextFieldValue("", TextRange(0))
                                        true
                                    } else {
                                        false
                                    }
                                }
                                Key.R -> {
                                    if (event.isShiftPressed) {
                                        files = currentDir.listFiles()?.toList()?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
                                        true
                                    } else {
                                        false
                                    }
                                }
                                Key.F -> {
                                    if (event.isCtrlPressed) {
                                        val step = maxOf(1, listState.layoutInfo.visibleItemsInfo.size - 1)
                                        selectedIndex = minOf(files.size - 1, selectedIndex + step)
                                        true
                                    } else {
                                        false
                                    }
                                }
                                Key.B -> {
                                    if (event.isCtrlPressed) {
                                        val step = maxOf(1, listState.layoutInfo.visibleItemsInfo.size - 1)
                                        selectedIndex = maxOf(0, selectedIndex - step)
                                        true
                                    } else {
                                        false
                                    }
                                }
                                else -> false
                            }
                        } else {
                            false
                        }
                    }
            ) {
                itemsIndexed(files) { index, file ->
                    val isSelected = index == selectedIndex
                    val isEditing = index == editingIndex
                    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isEditing) {
                            Text(text = if (file.isDirectory) "📁 " else "📄 ", color = textColor)
                            androidx.compose.foundation.text.BasicTextField(
                                value = editingText,
                                onValueChange = { editingText = it },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(textColor),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(editFocusRequester)
                                    .onPreviewKeyEvent { e ->
                                        if (e.type == KeyEventType.KeyDown) {
                                            if (e.key == Key.Enter) {
                                                val newName = editingText.text
                                                if (newName.isNotBlank() && newName != file.name) {
                                                    val newFile = File(currentDir, newName)
                                                    if (!newFile.exists()) {
                                                        if (file.renameTo(newFile)) {
                                                            files = files.toMutableList().also {
                                                                it[index] = newFile
                                                            }
                                                        }
                                                    }
                                                }
                                                editingIndex = null
                                                true
                                            } else if (e.key == Key.Escape) {
                                                editingIndex = null
                                                true
                                            } else {
                                                false
                                            }
                                        } else {
                                            false
                                        }
                                    }
                            )
                        } else {
                            Text(
                                text = if (file.isDirectory) "📁 ${file.name}/" else "📄 ${file.name}",
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}
