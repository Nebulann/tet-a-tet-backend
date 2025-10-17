package com.rrain.kupidon.models.db

/**
 * Настройки приватности пользователя
 */
data class UserPrivacySettingsM(
  // Видимость профиля
  var profileVisibility: ProfileVisibility = ProfileVisibility.EVERYONE,
  
  // Кто может писать сообщения
  var canMessage: MessagePermission = MessagePermission.MATCHES_ONLY,
  
  // Показывать онлайн статус
  var showOnlineStatus: Boolean = true,
  
  // Показывать расстояние до пользователя
  var showDistance: Boolean = true,
  
  // Показывать время последнего визита
  var showLastSeen: Boolean = false,
  
  // Список заблокированных пользователей (UUID)
  var blockedUsers: List<String> = emptyList()
)

enum class ProfileVisibility {
  EVERYONE,       // Все пользователи
  MATCHES_ONLY,   // Только совпадения
  NOBODY          // Никто
}

enum class MessagePermission {
  EVERYONE,       // Все пользователи
  MATCHES_ONLY    // Только совпадения
}
