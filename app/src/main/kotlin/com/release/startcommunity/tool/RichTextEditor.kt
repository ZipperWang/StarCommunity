package com.release.startcommunity.tool

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.material3.RichText

class RichTextEditor: FunctionalWidget() {

    override val name = "RichTextEditor"
    override val info = "Editor of RichText in Post."

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ViewScreen(initTitle: String, initContent: String, navController: NavController, onBack: () -> Unit) {

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
                Button(
                    onClick = {
                        navController.navigate("PostCreateBack/$tarContent/$tarTitle")
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("插入富文本")
                }
                Spacer(modifier = Modifier.height(24.dp))
                YieldContent(tarContent)
            }
        }
    }

    @Composable
    fun YieldContent(targetString: String) {
        return RichText(modifier = Modifier.background(color = Color.White)) {
            Markdown(targetString)
        }
    }
}