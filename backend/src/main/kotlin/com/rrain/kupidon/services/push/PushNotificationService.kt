package com.rrain.kupidon.services.push

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.*
import java.io.FileInputStream
import java.util.UUID

/**
 * Сервис для отправки push-уведомлений через Firebase Cloud Messaging
 */
class PushNotificationService {

    companion object {
        private var firebaseApp: FirebaseApp? = null

        /**
         * Инициализировать Firebase Admin SDK с service account файлом
         */
        fun initialize(serviceAccountPath: String = "tet-a-tet-e98c8-firebase-adminsdk.json") {
            if (firebaseApp == null) {
                try {
                    val serviceAccount = FileInputStream(serviceAccountPath)
                    val credentials = GoogleCredentials.fromStream(serviceAccount)

                    val options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build()

                    firebaseApp = FirebaseApp.initializeApp(options)

                    println("Firebase Admin SDK инициализирован успешно с service account: $serviceAccountPath")
                } catch (e: Exception) {
                    println("Ошибка инициализации Firebase Admin SDK: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Отправить уведомление одному пользователю
     */
    suspend fun sendToUser(
        fcmToken: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
        imageUrl: String? = null
    ): Boolean {
        return try {
            val messaging = FirebaseMessaging.getInstance(getFirebaseApp())

            val message = Message.builder()
                .setToken(fcmToken)
                .setNotification(
                    com.google.firebase.messaging.Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .apply {
                            imageUrl?.let { setImage(it) }
                        }
                        .build()
                )
                .putAllData(data)
                .setAndroidConfig(
                    AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(
                            AndroidNotification.builder()
                                .setSound("default")
                                .setColor("#6366F1")
                                .setChannelId("tet_a_tet_notifications")
                                .build()
                        )
                        .build()
                )
                .setWebpushConfig(
                    WebpushConfig.builder()
                        .setNotification(
                            WebpushNotification.builder()
                                .setIcon("/icons/icon-192x192.png")
                                .setBadge("/icons/badge-72x72.png")
                                .setRequireInteraction(false)
                                .build()
                        )
                        .build()
                )
                .build()

            val response = messaging.send(message)
            println("Уведомление отправлено успешно: $response")

            true
        } catch (e: Exception) {
            println("Ошибка отправки уведомления: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Отправить уведомление нескольким пользователям
     */
    suspend fun sendToMultipleUsers(
        fcmTokens: List<String>,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
        imageUrl: String? = null
    ): Int {
        if (fcmTokens.isEmpty()) return 0

        return try {
            val messaging = FirebaseMessaging.getInstance(getFirebaseApp())

            val message = MulticastMessage.builder()
                .addAllTokens(fcmTokens)
                .setNotification(
                    com.google.firebase.messaging.Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .apply {
                            imageUrl?.let { setImage(it) }
                        }
                        .build()
                )
                .putAllData(data)
                .setAndroidConfig(
                    AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(
                            AndroidNotification.builder()
                                .setSound("default")
                                .setColor("#6366F1")
                                .setChannelId("tet_a_tet_notifications")
                                .build()
                        )
                        .build()
                )
                .build()

            val response = messaging.sendEachForMulticast(message)
            println("Отправлено ${response.successCount} из ${fcmTokens.size} уведомлений")

            response.successCount
        } catch (e: Exception) {
            println("Ошибка отправки множественных уведомлений: ${e.message}")
            e.printStackTrace()
            0
        }
    }

    /**
     * Отправить уведомление по топику
     */
    suspend fun sendToTopic(
        topic: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
        imageUrl: String? = null
    ): Boolean {
        return try {
            val messaging = FirebaseMessaging.getInstance(getFirebaseApp())

            val message = Message.builder()
                .setTopic(topic)
                .setNotification(
                    com.google.firebase.messaging.Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .apply {
                            imageUrl?.let { setImage(it) }
                        }
                        .build()
                )
                .putAllData(data)
                .build()

            val response = messaging.send(message)
            println("Уведомление в топик отправлено: $response")

            true
        } catch (e: Exception) {
            println("Ошибка отправки уведомления в топик: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Подписать пользователя на топик
     */
    suspend fun subscribeToTopic(fcmToken: String, topic: String): Boolean {
        return try {
            val messaging = FirebaseMessaging.getInstance(getFirebaseApp())
            messaging.subscribeToTopic(listOf(fcmToken), topic)
            println("Пользователь подписан на топик: $topic")
            true
        } catch (e: Exception) {
            println("Ошибка подписки на топик: ${e.message}")
            false
        }
    }

    /**
     * Отписать пользователя от топика
     */
    suspend fun unsubscribeFromTopic(fcmToken: String, topic: String): Boolean {
        return try {
            val messaging = FirebaseMessaging.getInstance(getFirebaseApp())
            messaging.unsubscribeFromTopic(listOf(fcmToken), topic)
            println("Пользователь отписан от топика: $topic")
            true
        } catch (e: Exception) {
            println("Ошибка отписки от топика: ${e.message}")
            false
        }
    }

    /**
     * Получить FirebaseApp экземпляр
     */
    private fun getFirebaseApp(): FirebaseApp {
        return firebaseApp ?: throw IllegalStateException("Firebase App не инициализирован. Вызовите PushNotificationService.initialize() сначала.")
    }
}

/**
 * Типы уведомлений
 */
enum class NotificationType(val value: String) {
    MESSAGE("message"),
    LIKE("like"),
    MATCH("match"),
    EVENT("event"),
    DATE_IDEA("date_idea"),
    SYSTEM("system")
}

/**
 * Хелпер для создания data payload
 */
object NotificationDataBuilder {
    fun forMessage(chatId: UUID, senderId: UUID, senderName: String): Map<String, String> {
        return mapOf(
            "type" to NotificationType.MESSAGE.value,
            "chatId" to chatId.toString(),
            "senderId" to senderId.toString(),
            "senderName" to senderName
        )
    }

    fun forLike(userId: UUID, userName: String): Map<String, String> {
        return mapOf(
            "type" to NotificationType.LIKE.value,
            "userId" to userId.toString(),
            "userName" to userName
        )
    }

    fun forMatch(userId: UUID, userName: String): Map<String, String> {
        return mapOf(
            "type" to NotificationType.MATCH.value,
            "userId" to userId.toString(),
            "userName" to userName
        )
    }

    fun forEvent(eventId: UUID, eventTitle: String): Map<String, String> {
        return mapOf(
            "type" to NotificationType.EVENT.value,
            "eventId" to eventId.toString(),
            "eventTitle" to eventTitle
        )
    }

    fun forDateIdea(ideaId: UUID, ideaTitle: String): Map<String, String> {
        return mapOf(
            "type" to NotificationType.DATE_IDEA.value,
            "ideaId" to ideaId.toString(),
            "ideaTitle" to ideaTitle
        )
    }
}
