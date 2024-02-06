package io.harmny.service.user.service

import io.harmny.service.user.model.User
import io.harmny.service.user.properties.HarmnyUserServiceProperties
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.jetbrains.api.runtime.SpaceAppInstance
import space.jetbrains.api.runtime.SpaceClient
import space.jetbrains.api.runtime.helpers.message
import space.jetbrains.api.runtime.ktorClientForSpace
import space.jetbrains.api.runtime.resources.chats
import space.jetbrains.api.runtime.types.ChannelIdentifier
import space.jetbrains.api.runtime.types.ChatMessage
import space.jetbrains.api.runtime.types.MessageStyle

@Service
class NotificationService(
    properties: HarmnyUserServiceProperties,
) {

    companion object {
        private val log = LoggerFactory.getLogger(NotificationService::class.java)
    }

    private val spaceConnectionDetails = properties.space
    private val spaceChannelId = spaceConnectionDetails.notificationsChannelId

    private val spaceClient: SpaceClient? = run {
        try {
            val spaceHttpClient = ktorClientForSpace()
            SpaceClient(
                ktorClient = spaceHttpClient,
                appInstance = SpaceAppInstance(
                    clientId = spaceConnectionDetails.clientId,
                    clientSecret = spaceConnectionDetails.clientSecret,
                    spaceServerUrl = spaceConnectionDetails.serverUrl,
                ),
            )
        } catch (e: Exception) {
            log.error("Failed to create notification service. Reason: ${e.message}")
            null
        }
    }

    fun onUserRegistered(user: User) = runBlocking {
        sendMessage(
            user,
            header = "✅ New user has been registered!",
        )
    }

    fun onUserActivated(user: User) = runBlocking {
        sendMessage(
            user,
            header = "✅ User has been activated!",
        )
    }

    private suspend fun sendMessage(user: User, header: String) {
        val content = messageContent(header, user.toMessageAdditionalLines())
        try {
            spaceClient?.chats?.messages?.sendMessage(
                channel = ChannelIdentifier.Id(spaceChannelId),
                content = content
            )
        } catch (e: Exception) {
            log.error("Failed to send message. Reason: ${e.message}")
        }
    }

    private fun messageContent(
        header: String,
        additionalLines: List<String>,
    ): ChatMessage {
        return message(style = MessageStyle.SUCCESS) {
            section {
                text(header)
            }
            additionalLines.takeIf { it.isNotEmpty() }?.let {
                section {
                    additionalLines.forEach { line ->
                        text(line)
                    }
                }
            }
        }
    }

    private fun User.toMessageAdditionalLines(): List<String> {
        return listOf(
            "Name: ${this.firstName} ${this.lastName ?: ""}",
            "Email: ${this.email}",
            "Auth provider: ${this.authProvider}",
            "Active: ${this.active}"
        )
    }
}
