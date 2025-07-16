package com.release.startcommunity.view

import android.util.Log
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.text.substring

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichTextEditorScreen(
    initTitle: String,
    initContent: String,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit,
) {
    var tarContent by remember { mutableStateOf(TextFieldValue(initContent)) }
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp, max = 80.dp),
                horizontalArrangement = Arrangement.Start,
            ) {
                val preSetItemsMap = mapOf("一级标题(#)" to "# ", "二级标题(##)" to "## ",
                    "三级标题(###)" to "### ", "四级标题(####)" to "#### ",
                    "五级标题(#####)" to "##### ", "六级标题(######)" to "###### ",
                "粗体(**)" to "**", "斜体(*)" to "*", "删除线(~)" to "~", "有序列表1(1.)" to "1. ",
                    "有序列表2(2.)" to "2. ", "有序列表3(3.)" to "3. ", "无序列表(-)" to "- ",
                    "引用(>)" to "> ", "分割线(***)" to "***\n", "连接文本([])" to "[]",
                    "链接地址(())" to "()", "代码块(```)" to "```\n", "小代码块(`)" to "`",
                    "图片(![name][url])" to "![]()")
                var preSetItem by remember { mutableStateOf(preSetItemsMap.keys.toList()[0]) }
                val preSetItems = preSetItemsMap.keys.toList()
                ExposedDropdownMenuComboBox(
                    items = preSetItems,
                    selectedItem = preSetItem,
                    onItemSelected = { preSetItem = it },
                    label = "Markdown快捷输入"
                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Button(
                        onClick = {
                            if (tarContent.selection.collapsed) {
                                val currentText = tarContent.text
                                val selection = tarContent.selection
                                val insertValue = preSetItemsMap[preSetItem] ?: ""

                                // 在光标处插入文本
                                val newText = currentText.substring(0, selection.start) +
                                        insertValue +
                                        currentText.substring(selection.end)
                                // 更新光标位置到插入文本之后
                                val newSelectionStart = selection.start + insertValue.length
                                tarContent = TextFieldValue(
                                    text = newText,
                                    selection = TextRange(newSelectionStart)
                                )
                            }
                                  },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 12.dp)
                    ) {
                        Text("插入")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            YieldContent("# 预览效果：")
            Spacer(modifier = Modifier.height(12.dp))
            YieldContent(tarContent.text)
            Button(
                onClick = {
                    //navController.navigate("PostCreateBack/$tarContent/$tarTitle")
                    onSubmit(tarContent.text)
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
//    var textFieldValue = selectedItem
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField( // 或者使用 TextField
            modifier = Modifier.menuAnchor(), // 将 TextField 设为菜单的锚点
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            readOnly = true, // 使其更像 ComboBox
            label = { Text(label, modifier = Modifier.background(MaterialTheme.colorScheme.background)) },
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