package com.note.dynamic.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.note.dynamic.ui.screens.HomeScreen
import com.note.dynamic.ui.screens.NoteEditScreen

object Routes {
    const val HOME = "home"
    const val GROUP = "group/{groupId}"
    fun group(id: Long) = "group/$id"
    const val NOTE_EDIT = "note/{noteId}"
    fun noteEdit(id: Long) = "note/$id"
    const val NOTE_NEW = "note/new?groupId={groupId}"
    fun noteNew(groupId: Long? = null) =
        if (groupId != null) "note/new?groupId=$groupId" else "note/new?groupId=-1"
}

@Composable
fun NavGraph() {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = Routes.HOME,
        enterTransition = { slideInHorizontally(tween(280)) { it / 5 } + fadeIn(tween(280)) },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { slideOutHorizontally(tween(280)) { it / 5 } + fadeOut(tween(220)) }
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                groupId = null,
                onOpenGroup = { id -> nav.navigate(Routes.group(id)) },
                onOpenNote = { id -> nav.navigate(Routes.noteEdit(id)) },
                onCreateNote = { gId -> nav.navigate(Routes.noteNew(gId)) }
            )
        }
        composable(
            Routes.GROUP,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { entry ->
            val gId = entry.arguments?.getLong("groupId") ?: -1L
            HomeScreen(
                groupId = gId,
                onOpenNote = { id -> nav.navigate(Routes.noteEdit(id)) },
                onCreateNote = { nav.navigate(Routes.noteNew(gId)) },
                onBack = { nav.popBackStack() }
            )
        }
        composable(
            Routes.NOTE_EDIT,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("noteId") ?: -1L
            NoteEditScreen(
                noteId = if (id <= 0L) 0L else id,
                onBack = { nav.popBackStack() }
            )
        }
        composable(
            Routes.NOTE_NEW,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { entry ->
            val gIdStr = entry.arguments?.getString("groupId") ?: "-1"
            val gId = gIdStr.toLongOrNull() ?: -1L
            NoteEditScreen(
                noteId = 0L,
                presetGroupId = if (gId <= 0L) null else gId,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
