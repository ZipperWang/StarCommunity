package com.release.startcommunity
/*        不再用了        */
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import com.release.startcommunity.tool.RichTextEditor
//
//class RichTextEditActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val richTextEditor = RichTextEditor()
//        val initTitle = intent.getStringExtra("title") ?: "请输入标题"
//        val initContent = intent.getStringExtra("content") ?: "请输入正文"
//        setContent {
//            richTextEditor.ViewScreen(initTitle, initContent) { finish() }
//        }
//    }
//}