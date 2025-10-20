package com.rrain.kupidon.models.db

import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Идея для свидания
 */
data class DateIdeaM(
  var id: UUID,
  
  // Название идеи
  var title: String,
  
  // Описание
  var description: String,
  
  // Категория
  var category: DateIdeaCategory,
  
  // Теги
  var tags: List<String> = emptyList(),
  
  // URL изображения
  var imageUrl: String? = null,
  
  // Бюджет (в рублях)
  var budget: DateIdeaBudget = DateIdeaBudget.MEDIUM,
  
  // Сложность организации
  var difficulty: DateIdeaDifficulty = DateIdeaDifficulty.MEDIUM,
  
  // Сезонность
  var season: DateIdeaSeason = DateIdeaSeason.ALL_YEAR,
  
  // Продолжительность (в минутах)
  var duration: Int? = null,
  
  // Рейтинг (0-5)
  var rating: Double = 0.0,
  
  // Количество лайков
  var likesCount: Int = 0,
  
  // Дата создания
  var createdAt: Instant,
  
  // Дата обновления
  var updatedAt: Instant,
  
  // Активна ли идея
  var isActive: Boolean = true
) {
  fun toApi(): MutableMap<String, Any?> {
    return mutableMapOf(
      "id" to id,
      "title" to title,
      "description" to description,
      "category" to category,
      "tags" to tags,
      "imageUrl" to imageUrl,
      "budget" to budget,
      "difficulty" to difficulty,
      "season" to season,
      "duration" to duration,
      "rating" to rating,
      "likesCount" to likesCount,
      "createdAt" to createdAt,
      "updatedAt" to updatedAt
    )
  }
}

enum class DateIdeaCategory {
  ROMANTIC,       // Романтика
  ACTIVE,         // Активный отдых
  CULTURAL,       // Культурный отдых
  FOOD,           // Еда и напитки
  ENTERTAINMENT,  // Развлечения
  NATURE,         // Природа
  HOME,           // Дома
  ADVENTURE       // Приключения
}

enum class DateIdeaBudget {
  FREE,           // Бесплатно
  LOW,            // До 1000₽
  MEDIUM,         // 1000-3000₽
  HIGH,           // 3000-10000₽
  LUXURY          // 10000₽+
}

enum class DateIdeaDifficulty {
  EASY,           // Легко
  MEDIUM,         // Средне
  HARD            // Сложно
}

enum class DateIdeaSeason {
  ALL_YEAR,       // Круглый год
  SPRING,         // Весна
  SUMMER,         // Лето
  AUTUMN,         // Осень
  WINTER          // Зима
}

/**
 * Лайк идеи пользователем
 */
data class DateIdeaLikeM(
  var userId: UUID,
  var dateIdeaId: UUID,
  var likedAt: Instant
)
