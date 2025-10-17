package com.rrain.kupidon.services.data

import com.rrain.kupidon.models.db.DateIdeaBudget
import com.rrain.kupidon.models.db.DateIdeaCategory
import com.rrain.kupidon.models.db.DateIdeaDifficulty
import com.rrain.kupidon.models.db.DateIdeaM
import com.rrain.kupidon.models.db.DateIdeaSeason
import com.rrain.kupidon.services.mongo.collDateIdeas
import com.rrain.utils.base.`date-time`.now
import java.util.UUID

/**
 * Сервис для наполнения базы данных идеями для свиданий
 */
object DateIdeasSeedData {
  
  suspend fun seedDateIdeas() {
    val existingCount = collDateIdeas.countDocuments()
    if (existingCount > 0) {
      println("Date ideas already exist in database ($existingCount items)")
      return
    }
    
    val now = now()
    val dateIdeas = listOf(
      // Романтические идеи
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Пикник в парке на закате",
        description = "Романтический пикник с видом на закат. Возьмите плед, вкусную еду и наслаждайтесь природой вместе.",
        category = DateIdeaCategory.ROMANTIC,
        tags = listOf("пикник", "закат", "природа", "романтика"),
        imageUrl = "https://example.com/images/sunset-picnic.jpg",
        budget = DateIdeaBudget.LOW,
        difficulty = DateIdeaDifficulty.EASY,
        season = DateIdeaSeason.SUMMER,
        duration = 180,
        rating = 4.8,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      ),
      
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Ужин при свечах дома",
        description = "Приготовьте ужин вместе и устройте романтический вечер дома при свечах.",
        category = DateIdeaCategory.ROMANTIC,
        tags = listOf("ужин", "свечи", "дом", "готовка"),
        imageUrl = "https://example.com/images/candlelight-dinner.jpg",
        budget = DateIdeaBudget.MEDIUM,
        difficulty = DateIdeaDifficulty.MEDIUM,
        season = DateIdeaSeason.ALL_YEAR,
        duration = 240,
        rating = 4.7,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      ),
      
      // Активные идеи
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Велопрогулка по городу",
        description = "Исследуйте город на велосипедах, откройте новые места и насладитесь активным времяпрепровождением.",
        category = DateIdeaCategory.ACTIVE,
        tags = listOf("велосипед", "город", "спорт", "прогулка"),
        imageUrl = "https://example.com/images/bike-ride.jpg",
        budget = DateIdeaBudget.LOW,
        difficulty = DateIdeaDifficulty.MEDIUM,
        season = DateIdeaSeason.SUMMER,
        duration = 120,
        rating = 4.5,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      ),
      
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Скалолазание в зале",
        description = "Попробуйте скалолазание вместе - это отличный способ поддержать друг друга и весело провести время.",
        category = DateIdeaCategory.ACTIVE,
        tags = listOf("скалолазание", "спорт", "адреналин", "поддержка"),
        imageUrl = "https://example.com/images/rock-climbing.jpg",
        budget = DateIdeaBudget.MEDIUM,
        difficulty = DateIdeaDifficulty.HARD,
        season = DateIdeaSeason.ALL_YEAR,
        duration = 150,
        rating = 4.6,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      ),
      
      // Культурные идеи
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Поход в музей современного искусства",
        description = "Откройте для себя современное искусство и обсудите впечатления за чашкой кофе в музейном кафе.",
        category = DateIdeaCategory.CULTURAL,
        tags = listOf("музей", "искусство", "культура", "обсуждение"),
        imageUrl = "https://example.com/images/art-museum.jpg",
        budget = DateIdeaBudget.MEDIUM,
        difficulty = DateIdeaDifficulty.EASY,
        season = DateIdeaSeason.ALL_YEAR,
        duration = 180,
        rating = 4.4,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      ),
      
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Театральный спектакль",
        description = "Сходите на театральный спектакль и окунитесь в мир искусства и эмоций.",
        category = DateIdeaCategory.CULTURAL,
        tags = listOf("театр", "спектакль", "искусство", "эмоции"),
        imageUrl = "https://example.com/images/theater.jpg",
        budget = DateIdeaBudget.HIGH,
        difficulty = DateIdeaDifficulty.EASY,
        season = DateIdeaSeason.ALL_YEAR,
        duration = 210,
        rating = 4.7,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      ),
      
      // Еда и напитки
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Дегустация вин",
        description = "Посетите винный бар или винодельню для дегустации различных сортов вин.",
        category = DateIdeaCategory.FOOD,
        tags = listOf("вино", "дегустация", "алкоголь", "атмосфера"),
        imageUrl = "https://example.com/images/wine-tasting.jpg",
        budget = DateIdeaBudget.HIGH,
        difficulty = DateIdeaDifficulty.EASY,
        season = DateIdeaSeason.ALL_YEAR,
        duration = 120,
        rating = 4.6,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      ),
      
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Мастер-класс по приготовлению суши",
        description = "Научитесь готовить суши вместе под руководством профессионального шеф-повара.",
        category = DateIdeaCategory.FOOD,
        tags = listOf("суши", "готовка", "мастер-класс", "японская кухня"),
        imageUrl = "https://example.com/images/sushi-class.jpg",
        budget = DateIdeaBudget.HIGH,
        difficulty = DateIdeaDifficulty.MEDIUM,
        season = DateIdeaSeason.ALL_YEAR,
        duration = 180,
        rating = 4.8,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      ),
      
      // Развлечения
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Боулинг и коктейли",
        description = "Классическое свидание в боулинге с коктейлями и легкими закусками.",
        category = DateIdeaCategory.ENTERTAINMENT,
        tags = listOf("боулинг", "коктейли", "игры", "веселье"),
        imageUrl = "https://example.com/images/bowling.jpg",
        budget = DateIdeaBudget.MEDIUM,
        difficulty = DateIdeaDifficulty.EASY,
        season = DateIdeaSeason.ALL_YEAR,
        duration = 150,
        rating = 4.3,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      ),
      
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Квест-комната",
        description = "Решайте головоломки и загадки вместе в увлекательной квест-комнате.",
        category = DateIdeaCategory.ENTERTAINMENT,
        tags = listOf("квест", "головоломки", "командная работа", "адреналин"),
        imageUrl = "https://example.com/images/escape-room.jpg",
        budget = DateIdeaBudget.MEDIUM,
        difficulty = DateIdeaDifficulty.MEDIUM,
        season = DateIdeaSeason.ALL_YEAR,
        duration = 90,
        rating = 4.7,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      ),
      
      // Природа
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Поход в горы",
        description = "Отправьтесь в однодневный поход в горы, насладитесь природой и красивыми видами.",
        category = DateIdeaCategory.NATURE,
        tags = listOf("горы", "поход", "природа", "виды"),
        imageUrl = "https://example.com/images/mountain-hike.jpg",
        budget = DateIdeaBudget.LOW,
        difficulty = DateIdeaDifficulty.HARD,
        season = DateIdeaSeason.SUMMER,
        duration = 360,
        rating = 4.9,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      ),
      
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Наблюдение за звездами",
        description = "Найдите место вдали от городских огней и наблюдайте за звездным небом.",
        category = DateIdeaCategory.NATURE,
        tags = listOf("звезды", "ночь", "романтика", "космос"),
        imageUrl = "https://example.com/images/stargazing.jpg",
        budget = DateIdeaBudget.FREE,
        difficulty = DateIdeaDifficulty.EASY,
        season = DateIdeaSeason.ALL_YEAR,
        duration = 180,
        rating = 4.8,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      ),
      
      // Дома
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Марафон фильмов",
        description = "Выберите серию фильмов или сериал и проведите уютный вечер дома с попкорном.",
        category = DateIdeaCategory.HOME,
        tags = listOf("фильмы", "дом", "уют", "попкорн"),
        imageUrl = "https://example.com/images/movie-marathon.jpg",
        budget = DateIdeaBudget.FREE,
        difficulty = DateIdeaDifficulty.EASY,
        season = DateIdeaSeason.ALL_YEAR,
        duration = 300,
        rating = 4.2,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      ),
      
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Настольные игры",
        description = "Проведите вечер за настольными играми - от классических до современных стратегий.",
        category = DateIdeaCategory.HOME,
        tags = listOf("настольные игры", "дом", "стратегия", "веселье"),
        imageUrl = "https://example.com/images/board-games.jpg",
        budget = DateIdeaBudget.LOW,
        difficulty = DateIdeaDifficulty.EASY,
        season = DateIdeaSeason.ALL_YEAR,
        duration = 180,
        rating = 4.4,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      ),
      
      // Приключения
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Полет на воздушном шаре",
        description = "Незабываемое приключение с видом на город или природу с высоты птичьего полета.",
        category = DateIdeaCategory.ADVENTURE,
        tags = listOf("воздушный шар", "полет", "виды", "приключение"),
        imageUrl = "https://example.com/images/hot-air-balloon.jpg",
        budget = DateIdeaBudget.LUXURY,
        difficulty = DateIdeaDifficulty.MEDIUM,
        season = DateIdeaSeason.SUMMER,
        duration = 240,
        rating = 4.9,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      ),
      
      DateIdeaM(
        id = UUID.randomUUID(),
        title = "Картинг",
        description = "Почувствуйте адреналин на картинговой трассе - соревнуйтесь друг с другом в гонках.",
        category = DateIdeaCategory.ADVENTURE,
        tags = listOf("картинг", "гонки", "адреналин", "соревнование"),
        imageUrl = "https://example.com/images/go-karting.jpg",
        budget = DateIdeaBudget.MEDIUM,
        difficulty = DateIdeaDifficulty.MEDIUM,
        season = DateIdeaSeason.ALL_YEAR,
        duration = 120,
        rating = 4.5,
        likesCount = 0,
        createdAt = now,
        updatedAt = now
      )
    )
    
    collDateIdeas.insertMany(dateIdeas)
    println("Successfully seeded ${dateIdeas.size} date ideas")
  }
}
