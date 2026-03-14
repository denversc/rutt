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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentDir by remember { mutableStateOf(File(System.getProperty("user.dir"))) }
        val files = remember(currentDir) { 
            currentDir.listFiles()?.toList()?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList() 
        }
        var selectedIndex by remember(currentDir) { mutableStateOf(0) }
        val listState = rememberLazyListState()
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        LaunchedEffect(selectedIndex) {
            if (files.isNotEmpty()) {
                listState.animateScrollToItem(selectedIndex)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            var pathInput by remember(currentDir) { mutableStateOf(currentDir.absolutePath) }
            androidx.compose.material3.TextField(
                value = pathInput,
                onValueChange = { pathInput = it },
                modifier = Modifier
                    .fillMaxWidth()
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
                textStyle = MaterialTheme.typography.labelSmall
            )
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
                    .focusable()
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.J -> {
                                    if (selectedIndex < files.size - 1) {
                                        selectedIndex++
                                    }
                                    true
                                }
                                Key.K -> {
                                    if (selectedIndex > 0) {
                                        selectedIndex--
                                    }
                                    true
                                }
                                Key.H -> {
                                    currentDir.parentFile?.let {
                                        currentDir = it
                                    }
                                    true
                                }
                                Key.L -> {
                                    if (files.isNotEmpty() && files[selectedIndex].isDirectory) {
                                        currentDir = files[selectedIndex]
                                    }
                                    true
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
                    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
