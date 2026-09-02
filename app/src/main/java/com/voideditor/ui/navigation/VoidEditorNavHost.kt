package com.voideditor.ui.navigation

import android.net.Uri
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.voideditor.agent.AgentCatalog
import com.voideditor.agent.AgentInstaller
import com.voideditor.ui.screens.EditorScreen
import com.voideditor.ui.screens.HomeScreen
import com.voideditor.ui.settings.SettingsScreen
import com.voideditor.ui.terminal.TerminalScreen

enum class VoidEditorRoute(val path: String) {
    Home("home"),
    Terminal("terminal"),
    Settings("settings"),
    AgentTerminal("agent"),
    Editor("editor")
}

fun editorRoute(path: String): String = VoidEditorRoute.Editor.path + "/" + Uri.encode(path)

fun projectTerminalRoute(path: String): String =
    VoidEditorRoute.Terminal.path + "/" + Uri.encode(path)

fun agentInstallRoute(agentId: String): String =
    VoidEditorRoute.AgentTerminal.path + "/" + Uri.encode(agentId)

@Composable
fun VoidEditorNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = VoidEditorRoute.Home.path,
        modifier = modifier,
        enterTransition = {
            slideInVertically(initialOffsetY = { height -> height / 12 }, animationSpec = tween(320)) +
                fadeIn(animationSpec = tween(320))
        },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(260)) },
        popExitTransition = {
            slideOutVertically(targetOffsetY = { height -> height / 10 }, animationSpec = tween(300)) +
                fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(VoidEditorRoute.Home.path) {
            HomeScreen(
                onNavigate = { route -> navController.navigate(route.path) },
                onProjectCreated = { path -> navController.navigate(editorRoute(path)) }
            )
        }
        composable(
            route = VoidEditorRoute.Editor.path + "/{path}",
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectPath = backStackEntry.arguments?.getString("path").orEmpty()
            EditorScreen(
                projectPath = projectPath,
                onOpenSettings = { navController.navigate(VoidEditorRoute.Settings.path) },
                onOpenTerminal = { navController.navigate(projectTerminalRoute(projectPath)) }
            )
        }
        composable(VoidEditorRoute.Terminal.path) {
            TerminalScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = VoidEditorRoute.Terminal.path + "/{path}",
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path").orEmpty()
            TerminalScreen(
                onBack = { navController.popBackStack() },
                projectDir = path.takeIf { it.isNotEmpty() }?.let { java.io.File(it) }
            )
        }
        composable(VoidEditorRoute.Settings.path) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onInstallAgent = { id -> navController.navigate(agentInstallRoute(id)) }
            )
        }
        composable(
            route = VoidEditorRoute.AgentTerminal.path + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id").orEmpty()
            val spec = AgentCatalog.byId(id)
            TerminalScreen(
                onBack = { navController.popBackStack() },
                initialCommand = spec?.let {
                    val path = AgentInstaller.guestScriptPath(it)
                    "echo '=== installing ${it.name} ==='; " +
                        "bash $path; " +
                        "echo \"=== exit code: \$? ===\""
                }
            )
        }
    }
}
