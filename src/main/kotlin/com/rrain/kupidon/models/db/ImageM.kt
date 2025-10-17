package com.rrain.kupidon.models.db

import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Метаданные изображения
 */
data class ImageM(
  var id: UUID,
  
  // ID пользователя, загрузившего изображение
  var uploadedBy: UUID,
  
  // Оригинальное имя файла
  var originalFileName: String,
  
  // MIME тип
  var mimeType: String,
  
  // Размер файла в байтах
  var fileSize: Long,
  
  // Ширина изображения
  var width: Int,
  
  // Высота изображения
  var height: Int,
  
  // ID файла в GridFS
  var gridFsId: String,
  
  // Тип изображения (для какой сущности)
  var imageType: ImageType,
  
  // ID связанной сущности (события, идеи и т.д.)
  var relatedEntityId: UUID? = null,
  
  // Дата загрузки
  var uploadedAt: Instant,
  
  // Активно ли изображение
  var isActive: Boolean = true
) {
  fun toApi(host: String, port: Int): MutableMap<String, Any?> {
    return mutableMapOf(
      "id" to id,
      "uploadedBy" to uploadedBy,
      "originalFileName" to originalFileName,
      "mimeType" to mimeType,
      "fileSize" to fileSize,
      "width" to width,
      "height" to height,
      "imageType" to imageType,
      "relatedEntityId" to relatedEntityId,
      "uploadedAt" to uploadedAt,
      "url" to "http://$host:$port/api/v1/images/$id"
    )
  }
}

enum class ImageType {
  EVENT,          // Изображение события
  DATE_IDEA,      // Изображение идеи для свидания
  USER_PROFILE,   // Фото профиля пользователя
  CHAT_MESSAGE,   // Изображение в сообщении чата
  OTHER           // Другое
}
