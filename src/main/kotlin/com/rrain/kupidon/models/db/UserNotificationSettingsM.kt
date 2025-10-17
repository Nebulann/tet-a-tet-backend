package com.rrain.kupidon.models.db

/**
 * Настройки уведомлений пользователя
 */
data class UserNotificationSettingsM(
  // Push уведомления включены
  var pushEnabled: Boolean = true,
  
  // Email уведомления включены
  var emailEnabled: Boolean = true,
  
  // Уведомления о новых сообщениях
  var messages: Boolean = true,
  
  // Уведомления о лайках
  var likes: Boolean = true,
  
  // Уведомления о взаимных симпатиях
  var matches: Boolean = true,
  
  // Уведомления о событиях
  var events: Boolean = true,
  
  // Уведомления об идеях для свиданий
  var dateIdeas: Boolean = false,
  
  // Системные обновления
  var systemUpdates: Boolean = true,
  
  // Маркетинговые рассылки
  var marketing: Boolean = false,
  
  // Звук уведомлений
  var soundEnabled: Boolean = true,
  
  // Вибрация
  var vibrationEnabled: Boolean = true,
  
  // Push subscription endpoint (для Web Push API)
  var pushSubscription: String? = null
)
