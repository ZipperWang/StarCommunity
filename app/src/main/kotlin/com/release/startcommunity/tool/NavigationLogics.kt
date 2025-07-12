package com.release.startcommunity.tool

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.release.startcommunity.api.CreatePostRequest
import com.release.startcommunity.view.PostCreateScreen
import com.release.startcommunity.viewmodel.UserViewModel


class NavigationLogics {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    fun NavLogic_CreatePost(bundleOnSubmit: (CreatePostRequest) -> Unit,
                            bundleOnBack: () -> Unit,
                            bundleUserViewModel: UserViewModel,
    ) {
        val navController = rememberNavController()
        val richTextEditor = RichTextEditor()
        NavHost(navController = navController, startDestination = "PostCreate") {
            composable("PostCreate") {
                PostCreateScreen(
                    navController = navController,
                    onBack = bundleOnBack,
                    onSubmit = bundleOnSubmit,
                    userViewModel = bundleUserViewModel,
                )
            }
            composable("PostCreateBack/{insertValue_Content}/{insertValue_Title}") {backStackEntry ->
                PostCreateScreen(
                    navController = navController,
                    onBack = bundleOnBack,
                    onSubmit = bundleOnSubmit,
                    userViewModel = bundleUserViewModel,
                    insertValueContent = backStackEntry.arguments?.getString("insertValue_Content") ?: "",
                    insertValueTitle = backStackEntry.arguments?.getString("insertValue_Title") ?: ""
                )
            }
            composable("RichTextEdit/{rich_text_edit_content}/{rich_text_edit_title}") {
                    backStackEntry ->
                val bundleContent = backStackEntry.arguments?.getString("rich_text_edit_content") ?: ""
                val bundleTitle = backStackEntry.arguments?.getString("rich_text_edit_title") ?: ""
                richTextEditor.ViewScreen(bundleTitle, bundleContent, navController) { navController.popBackStack() }
            }
        }
    }
}