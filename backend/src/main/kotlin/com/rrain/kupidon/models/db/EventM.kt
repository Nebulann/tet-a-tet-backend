package com.rrain.kupidon.models.db

import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Событие/мероприятие
 */
data class EventM(
  var id: UUID,
  
  // ID создателя события
  var creatorId: UUID,
  
  // Название события
  var title: String,
  
  // Описание
  var description: String,
  
  // Категория
  var category: EventCategory,
  
  // Дата и время начала
  var startDate: Instant,
  
  // Дата и время окончания
  var endDate: Instant? = null,
  
  // Место проведения
  var location: String? = null,
  
  // Координаты (широта, долгота)
  var coordinates: EventCoordinates? = null,
  
  // URL изображения
  var imageUrl: String? = null,
  
  // Максимальное количество участников
  var maxParticipants: Int? = null,
  
  // Список участников (UUID)
  var participants: List<UUID> = emptyList(),
  
  // Теги
  var tags: List<String> = emptyList(),
  
  // Количество лайков
  var likesCount: Int = 0,
  
  // Количество реакций "огонь"
  var fireCount: Int = 0,
  
  // Дата создания
  var createdAt: Instant,
  
  // Дата обновления
  var updatedAt: Instant,
  
  // Активно ли событие
  var isActive: Boolean = true,
  
  // Закреплено ли событие
  var isPinned: Boolean = false
) {
  fun toApi(
    host: String,
    port: Int,
    currentUserId: UUID? = null
  ): MutableMap<String, Any?> {
    return mutableMapOf(
      "id" to id,
      "creatorId" to creatorId,
      "title" to title,
      "description" to description,
      "category" to category,
      "startDate" to startDate,
      "endDate" to endDate,
      "location" to location,
      "coordinates" to coordinates?.let {
        mapOf("lat" to it.lat, "lng" to it.lng)
      },
      "imageUrl" to imageUrl,
      "maxParticipants" to maxParticipants,
      "participantsCount" to participants.size,
      "isParticipating" to (currentUserId?.let { participants.contains(it) } ?: false),
      "tags" to tags,
      "likesCount" to likesCount,
      "fireCount" to fireCount,
      "createdAt" to createdAt,
      "updatedAt" to updatedAt,
      "isPinned" to isPinned
    )
  }
}

data class EventCoordinates(
  var lat: Double,
  var lng: Double
)

enum class EventCategory {
  MEETUP,         // Встреча
  PARTY,          // Вечеринка
  SPORT,          // Спорт
  CULTURAL,       // Культурное мероприятие
  OUTDOOR,        // На природе
  WORKSHOP,       // Мастер-класс
  DATING,         // Свидание
  OTHER           // Другое
}

/**
 * Реакция на событие
 */
data class EventReactionM(
  var userId: UUID,
  var eventId: UUID,
  var reactionType: EventReactionType,
  var reactedAt: Instant
)

enum class EventReactionType {
  LIKE,           // Лайк
  FIRE            // Огонь
}

/**
 * Комментарий к событию
 */
data class EventCommentM(
  var id: UUID,
  var eventId: UUID,
  var userId: UUID,
  var text: String,
  var createdAt: Instant,
  var updatedAt: Instant,
  var isDeleted: Boolean = false
) {
  fun toApi(
    userName: String,
    userAva: String
  ): MutableMap<String, Any?> {
    return mutableMapOf(
      "id" to id,
      "eventId" to eventId,
      "userId" to userId,
      "userName" to userName,
      "userAva" to userAva,
      "text" to text,
      "createdAt" to createdAt,
      "updatedAt" to updatedAt
    )
  }
}
