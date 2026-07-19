package com.sechat.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sechat.feature.contacts.ContactsScreen
import com.sechat.feature.identity.IdentityScreen
import com.sechat.feature.chat.ChatScreen

object SeChatRoutes {
    const val IDENTITY = "identity"
    const val CONTACTS = "contacts"
    const val CHAT = "chat/{contactId}"
    const val SETTINGS = "settings"

    fun chatRoute(contactId: String) = "chat/$contactId"
}

@Composable
fun SeChatNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SeChatRoutes.IDENTITY
    ) {
        composable(SeChatRoutes.IDENTITY) {
            IdentityScreen(
                onNavigateToContacts = { navController.navigate(SeChatRoutes.CONTACTS) }
            )
        }

        composable(SeChatRoutes.CONTACTS) {
            ContactsScreen(
                onContactSelected = { contactId ->
                    navController.navigate(SeChatRoutes.chatRoute(contactId))
                }
            )
        }

        composable(SeChatRoutes.CHAT) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: return@composable
            ChatScreen(
                contactId = contactId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
