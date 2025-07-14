package com.release.startcommunity.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.material3.RichText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichTextEditorScreen(
    initTitle: String,
    initContent: String,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit,
) {

    var tarContent by remember { mutableStateOf(initContent) }
    val tarTitle by remember { mutableStateOf(initTitle) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("富文本编辑器") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = tarContent,
                onValueChange = { tarContent = it },
                label = { Text("编辑正文") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            val preSetItems = listOf("# ", "## ", "### ", "#### ", "##### ", "###### ",
                "**", "*", "~~~", "1. ", "2. ", "3. ", "-", "***", "```", "`")
            var preSetItem = preSetItems[0]
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp, max = 80.dp),
                horizontalArrangement = Arrangement.Start,
            ) {
                ExposedDropdownMenuComboBox(
                    items = preSetItems,
                    selectedItem = preSetItem,
                    onItemSelected = {preSetItem = it},
                    label = "Markdown快捷输入"
                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Button(
                        onClick = { tarContent += preSetItem },
                        modifier = Modifier.align(Alignment.End).padding(top = 12.dp)
                    ) {
                        Text("插入")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            YieldContent("# 预览效果：")
            Spacer(modifier = Modifier.height(12.dp))
            YieldContent(tarContent)
            Button(
                onClick = {
                    //navController.navigate("PostCreateBack/$tarContent/$tarTitle")
                    onSubmit(tarContent)
                          },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("插入富文本")
            }
        }
    }
}

@Composable
fun YieldContent(targetString: String) {
    return RichText(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
        Markdown(targetString)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenuComboBox(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    label: String = "Combo Box"
) {
    var expanded by remember { mutableStateOf(false) }
    // 手动管理 TextField 的文本，使其与选中的项同步
    var textFieldValue by remember(selectedItem) { mutableStateOf(selectedItem) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField( // 或者使用 TextField
            modifier = Modifier.menuAnchor(), // 将 TextField 设为菜单的锚点
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            readOnly = true, // 使其更像 ComboBox
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onItemSelected(selectionOption)
                        textFieldValue = selectionOption // 更新 TextField 的显示
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}