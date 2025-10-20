package com.rrain.kupidon.routes.routes.http.test

import com.mongodb.client.model.Filters
import com.rrain.kupidon.models.Gender
import com.rrain.kupidon.models.LookingFor
import com.rrain.kupidon.models.db.EventCategory
import com.rrain.kupidon.models.db.EventCoordinates
import com.rrain.kupidon.models.db.EventM
import com.rrain.kupidon.models.db.UserM
import com.rrain.kupidon.services.mongo.collEvents
import com.rrain.kupidon.services.mongo.collUsers
import com.rrain.kupidon.services.`pwd-hash`.PwdHashService
import com.rrain.utils.base.`date-time`.now
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Instant
import java.util.UUID
import kotlin.time.Duration.Companion.days

/**
 * Роуты для заполнения базы данных тестовыми данными
 */
fun Application.addSeedDataRoutes() {
  
  routing {
    
    // Заполнить базу тестовыми данными
    get("/test/seed/all") {
      val now = now()
      
      // Создаем тестовых пользователей
      val testUsers = listOf(
        UserM(
          id = UUID.randomUUID(),
          email = "alice@test.com",
          emailVerified = true,
          pwdHash = PwdHashService.hashPassword("password123"),
          name = "Алиса",
          birthDate = Instant.parse("1995-05-15T00:00:00Z"),
          gender = Gender.FEMALE,
          lookingFor = LookingFor.MALE,
          bio = "Люблю путешествия, книги и хорошую компанию ☕",
          city = "Москва",
          createdAt = now,
          updatedAt = now
        ),
        UserM(
          id = UUID.randomUUID(),
          email = "bob@test.com",
          emailVerified = true,
          pwdHash = PwdHashService.hashPassword("password123"),
          name = "Боб",
          birthDate = Instant.parse("1992-08-20T00:00:00Z"),
          gender = Gender.MALE,
          lookingFor = LookingFor.FEMALE,
          bio = "Программист, любитель спорта и активного отдыха 🏃‍♂️",
          city = "Санкт-Петербург",
          createdAt = now,
          updatedAt = now
        ),
        UserM(
          id = UUID.randomUUID(),
          email = "charlie@test.com",
          emailVerified = true,
          pwdHash = PwdHashService.hashPassword("password123"),
          name = "Чарли",
          birthDate = Instant.parse("1998-03-10T00:00:00Z"),
          gender = Gender.MALE,
          lookingFor = LookingFor.BOTH,
          bio = "Музыкант, фотограф, мечтатель 🎸📷",
          city = "Москва",
          createdAt = now,
          updatedAt = now
        ),
        UserM(
          id = UUID.randomUUID(),
          email = "diana@test.com",
          emailVerified = true,
          pwdHash = PwdHashService.hashPassword("password123"),
          name = "Диана",
          birthDate = Instant.parse("1996-11-25T00:00:00Z"),
          gender = Gender.FEMALE,
          lookingFor = LookingFor.MALE,
          bio = "Дизайнер, йога, здоровый образ жизни 🧘‍♀️",
          city = "Казань",
          createdAt = now,
          updatedAt = now
        ),
        UserM(
          id = UUID.randomUUID(),
          email = "eve@test.com",
          emailVerified = true,
          pwdHash = PwdHashService.hashPassword("password123"),
          name = "Ева",
          birthDate = Instant.parse("1994-07-08T00:00:00Z"),
          gender = Gender.FEMALE,
          lookingFor = LookingFor.BOTH,
          bio = "Художница, люблю искусство и культуру 🎨",
          city = "Москва",
          createdAt = now,
          updatedAt = now
        )
      )
      
      // Вставляем пользователей (если их еще нет)
      val insertedUsers = mutableListOf<UserM>()
      for (user in testUsers) {
        val existing = collUsers.find(Filters.eq(UserM::email.name, user.email)).firstOrNull()
        if (existing == null) {
          collUsers.insertOne(user)
          insertedUsers.add(user)
        } else {
          insertedUsers.add(existing)
        }
      }
      
      // Создаем тестовые события
      val testEvents = listOf(
        EventM(
          id = UUID.randomUUID(),
          creatorId = insertedUsers[0].id,
          title = "Вечеринка в стиле 90-х",
          description = "Ностальгическая вечеринка с музыкой 90-х! Приходите в образах той эпохи 🎉",
          category = EventCategory.PARTY,
          startDate = now + 7.days,
          endDate = now + 7.days + 4.days.inWholeHours.toInt().days,
          location = "Клуб Retrowave, ул. Тверская, 15",
          coordinates = EventCoordinates(55.7558, 37.6173),
          imageUrl = "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=800",
          maxParticipants = 50,
          tags = listOf("вечеринка", "90-е", "ретро", "музыка"),
          participants = listOf(insertedUsers[1].id, insertedUsers[2].id),
          likesCount = 15,
          fireCount = 8,
          createdAt = now,
          updatedAt = now,
          isPinned = true
        ),
        EventM(
          id = UUID.randomUUID(),
          creatorId = insertedUsers[1].id,
          title = "Забег в парке Горького",
          description = "Утренняя пробежка для всех уровней подготовки. Встречаемся у главного входа 🏃",
          category = EventCategory.SPORT,
          startDate = now + 2.days,
          location = "Парк Горького, главный вход",
          coordinates = EventCoordinates(55.7312, 37.6018),
          maxParticipants = 30,
          tags = listOf("спорт", "бег", "здоровье", "утро"),
          participants = listOf(insertedUsers[0].id, insertedUsers[3].id),
          likesCount = 22,
          fireCount = 12,
          createdAt = now,
          updatedAt = now
        ),
        EventM(
          id = UUID.randomUUID(),
          creatorId = insertedUsers[2].id,
          title = "Концерт джазовой музыки",
          description = "Живая джазовая музыка в уютной атмосфере. Приглашаем всех любителей хорошей музыки 🎷",
          category = EventCategory.CULTURAL,
          startDate = now + 5.days,
          endDate = now + 5.days + 3.days.inWholeHours.toInt().days,
          location = "Джаз-клуб Эссе, Потаповский переулок, 5",
          coordinates = EventCoordinates(55.7558, 37.6278),
          imageUrl = "https://images.unsplash.com/photo-1415201364774-f6f0bb35f28f?w=800",
          maxParticipants = 40,
          tags = listOf("музыка", "джаз", "концерт", "культура"),
          participants = listOf(insertedUsers[4].id),
          likesCount = 18,
          fireCount = 10,
          createdAt = now,
          updatedAt = now
        ),
        EventM(
          id = UUID.randomUUID(),
          creatorId = insertedUsers[3].id,
          title = "Мастер-класс по йоге",
          description = "Йога для начинающих и продолжающих. Коврики предоставляются 🧘‍♀️",
          category = EventCategory.WORKSHOP,
          startDate = now + 3.days,
          location = "Йога-студия Прана, ул. Маросейка, 9",
          coordinates = EventCoordinates(55.7580, 37.6360),
          maxParticipants = 15,
          tags = listOf("йога", "здоровье", "медитация", "мастер-класс"),
          participants = listOf(insertedUsers[0].id, insertedUsers[4].id),
          likesCount = 25,
          fireCount = 15,
          createdAt = now,
          updatedAt = now
        ),
        EventM(
          id = UUID.randomUUID(),
          creatorId = insertedUsers[4].id,
          title = "Пикник на природе",
          description = "Выезд на природу с играми, едой и хорошей компанией! Берите с собой хорошее настроение 🌳",
          category = EventCategory.OUTDOOR,
          startDate = now + 10.days,
          location = "Серебряный бор",
          coordinates = EventCoordinates(55.7833, 37.4167),
          imageUrl = "https://images.unsplash.com/photo-1506929562872-bb421503ef21?w=800",
          maxParticipants = 25,
          tags = listOf("природа", "пикник", "отдых", "друзья"),
          participants = listOf(insertedUsers[1].id, insertedUsers[2].id, insertedUsers[3].id),
          likesCount = 30,
          fireCount = 20,
          createdAt = now,
          updatedAt = now,
          isPinned = true
        ),
        EventM(
          id = UUID.randomUUID(),
          creatorId = insertedUsers[0].id,
          title = "Романтический ужин",
          description = "Вечер для двоих в романтической обстановке 🕯️",
          category = EventCategory.DATING,
          startDate = now + 4.days,
          location = "Ресторан White Rabbit, Смоленская площадь, 3",
          coordinates = EventCoordinates(55.7467, 37.5814),
          imageUrl = "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800",
          maxParticipants = 2,
          tags = listOf("романтика", "ужин", "свидание"),
          likesCount = 12,
          fireCount = 8,
          createdAt = now,
          updatedAt = now
        ),
        EventM(
          id = UUID.randomUUID(),
          creatorId = insertedUsers[1].id,
          title = "Встреча в кофейне",
          description = "Неформальная встреча для знакомства и общения за чашкой кофе ☕",
          category = EventCategory.MEETUP,
          startDate = now + 1.days,
          location = "Кофейня Кофемания, Никольская ул., 10",
          coordinates = EventCoordinates(55.7558, 37.6211),
          maxParticipants = 10,
          tags = listOf("кофе", "общение", "знакомства", "встреча"),
          participants = listOf(insertedUsers[2].id, insertedUsers[4].id),
          likesCount = 8,
          fireCount = 5,
          createdAt = now,
          updatedAt = now
        ),
        EventM(
          id = UUID.randomUUID(),
          creatorId = insertedUsers[2].id,
          title = "Фотопрогулка по городу",
          description = "Прогулка с фотоаппаратами по красивым местам Москвы 📷",
          category = EventCategory.CULTURAL,
          startDate = now + 6.days,
          location = "Красная площадь",
          coordinates = EventCoordinates(55.7539, 37.6208),
          imageUrl = "https://images.unsplash.com/photo-1452421822248-d4c2b47f0c81?w=800",
          maxParticipants = 12,
          tags = listOf("фотография", "прогулка", "город", "искусство"),
          participants = listOf(insertedUsers[3].id),
          likesCount = 14,
          fireCount = 9,
          createdAt = now,
          updatedAt = now
        )
      )
      
      // Вставляем события (если их еще нет)
      val insertedEvents = mutableListOf<EventM>()
      for (event in testEvents) {
        val existing = collEvents.find(Filters.eq(EventM::title.name, event.title)).firstOrNull()
        if (existing == null) {
          collEvents.insertOne(event)
          insertedEvents.add(event)
        } else {
          insertedEvents.add(existing)
        }
      }
      
      call.respond(HttpStatusCode.OK, mapOf(
        "message" to "База данных успешно заполнена тестовыми данными",
        "users" to mapOf(
          "inserted" to insertedUsers.size,
          "emails" to insertedUsers.map { it.email }
        ),
        "events" to mapOf(
          "inserted" to insertedEvents.size,
          "titles" to insertedEvents.map { it.title }
        )
      ))
    }
    
    // Очистить все тестовые данные
    get("/test/seed/clear") {
      val testEmails = listOf(
        "alice@test.com",
        "bob@test.com",
        "charlie@test.com",
        "diana@test.com",
        "eve@test.com"
      )
      
      // Удаляем тестовых пользователей
      val deletedUsers = collUsers.deleteMany(
        Filters.`in`(UserM::email.name, testEmails)
      ).deletedCount
      
      // Удаляем все события (осторожно!)
      val deletedEvents = collEvents.deleteMany(Filters.empty()).deletedCount
      
      call.respond(HttpStatusCode.OK, mapOf(
        "message" to "Тестовые данные удалены",
        "deletedUsers" to deletedUsers,
        "deletedEvents" to deletedEvents
      ))
    }
  }
}
