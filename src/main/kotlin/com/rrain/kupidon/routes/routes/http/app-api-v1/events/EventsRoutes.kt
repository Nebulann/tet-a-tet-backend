package com.rrain.kupidon.routes.routes.http.`app-api-v1`.events

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.rrain.kupidon.models.db.EventCategory
import com.rrain.kupidon.models.db.EventCommentM
import com.rrain.kupidon.models.db.EventCoordinates
import com.rrain.kupidon.models.db.EventM
import com.rrain.kupidon.models.db.EventReactionM
import com.rrain.kupidon.models.db.EventReactionType
import com.rrain.kupidon.models.db.UserDataType
import com.rrain.kupidon.models.db.UserM
import com.rrain.kupidon.plugins.JacksonObjectMapper
import com.rrain.kupidon.plugins.authUserId
import com.rrain.kupidon.routes.`response-errors`.respondInvalidBody
import com.rrain.kupidon.routes.`response-errors`.respondNotFound
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.services.mongo.collEventComments
import com.rrain.kupidon.services.mongo.collEventReactions
import com.rrain.kupidon.services.mongo.collEvents
import com.rrain.kupidon.services.mongo.collUsers
import com.rrain.kupidon.services.mongo.useSingleDocTx
import com.rrain.utils.base.`date-time`.now
import kotlinx.datetime.toInstant
import com.rrain.utils.ktor.call.host
import com.rrain.utils.ktor.call.port
import com.rrain.utils.ktor.call.queryParams
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * GET /api/v1/events - получить список событий
 * POST /api/v1/events - создать событие
 * GET /api/v1/events/id/{id} - получить событие по ID
 * PUT /api/v1/events/id/{id} - обновить событие
 * DELETE /api/v1/events/id/{id} - удалить событие
 * POST /api/v1/events/id/{id}/attend - записаться на событие
 * DELETE /api/v1/events/id/{id}/attend - отписаться от события
 * POST /api/v1/events/id/{id}/react - поставить реакцию
 * GET /api/v1/events/id/{id}/comments - получить комментарии
 * POST /api/v1/events/id/{id}/comments - добавить комментарий
 */
fun Application.addEventsRoutes() {
  
  routing {
    authenticate {
      // GET список событий
      get(ApiV1Routes.events) {
        val userUuid = authUserId
        val queryParams = call.queryParams
        
        val page = queryParams["page"]?.toIntOrNull() ?: 0
        val limit = (queryParams["limit"]?.toIntOrNull() ?: 20).coerceIn(1, 100)
        val category = queryParams["category"]?.let { 
          try { EventCategory.valueOf(it) } catch (e: Exception) { null }
        }
        val search = queryParams["search"]?.takeIf { it.isNotBlank() }
        val upcoming = queryParams["upcoming"]?.toBooleanStrictOrNull() ?: true
        
        val filters = mutableListOf(
          Filters.eq(EventM::isActive.name, true)
        )
        
        if (category != null) {
          filters.add(Filters.eq(EventM::category.name, category))
        }
        
        if (search != null) {
          filters.add(
            Filters.or(
              Filters.regex(EventM::title.name, search, "i"),
              Filters.regex(EventM::description.name, search, "i"),
              Filters.`in`(EventM::tags.name, search)
            )
          )
        }
        
        if (upcoming) {
          filters.add(Filters.gte(EventM::startDate.name, now()))
        }
        
        val totalFilter = if (filters.size == 1) filters[0] else Filters.and(filters)
        
        val total = collEvents.countDocuments(totalFilter)
        val events = collEvents
          .find(totalFilter)
          .sort(
            if (upcoming) Sorts.ascending(EventM::startDate.name)
            else Sorts.descending(EventM::createdAt.name)
          )
          .skip(page * limit)
          .limit(limit)
          .toList()
        
        val eventsApi = events.map { event ->
          event.toApi(call.host, call.port, userUuid)
        }
        
        call.respond(mapOf(
          "events" to eventsApi,
          "pagination" to mapOf(
            "page" to page,
            "limit" to limit,
            "total" to total,
            "hasNext" to ((page + 1) * limit < total)
          )
        ))
      }
      
      // POST создать событие
      post(ApiV1Routes.events) {
        val userUuid = authUserId
        
        val data =
          try { call.receive<JsonNode>() }
          catch (ex: Exception) { return@post call.respondInvalidBody() }
        
        if (!data.isObject) {
          return@post call.respondInvalidBody("Body must be json object")
        }
        
        val now = now()
        val eventId = UUID.randomUUID()
        
        try {
          val title = data["title"]?.asText()?.takeIf { it.isNotBlank() }
            ?: return@post call.respondInvalidBody("Title is required")
          
          val description = data["description"]?.asText()?.takeIf { it.isNotBlank() }
            ?: return@post call.respondInvalidBody("Description is required")
          
          val category = data["category"]?.let { 
            JacksonObjectMapper.treeToValue<EventCategory>(it)
          } ?: return@post call.respondInvalidBody("Category is required")
          
          val startDate = data["startDate"]?.asText()?.let { 
            try { kotlinx.datetime.Instant.parse(it) } catch (e: Exception) {
              return@post call.respondInvalidBody("Invalid start date format")
            }
          } ?: return@post call.respondInvalidBody("Start date is required")
          
          val endDate = data["endDate"]?.asText()?.let { 
            try { kotlinx.datetime.Instant.parse(it) } catch (e: Exception) { null }
          }
          
          val location = data["location"]?.asText()?.takeIf { it.isNotBlank() }
          
          val coordinates = data["coordinates"]?.let { coordNode ->
            if (coordNode.has("lat") && coordNode.has("lng")) {
              EventCoordinates(
                lat = coordNode["lat"].asDouble(),
                lng = coordNode["lng"].asDouble()
              )
            } else null
          }
          
          val imageUrl = data["imageUrl"]?.asText()?.takeIf { it.isNotBlank() }
          
          val maxParticipants = data["maxParticipants"]?.asInt()?.takeIf { it > 0 }
          
          val tags = data["tags"]?.let { 
            JacksonObjectMapper.treeToValue<List<String>>(it)
          } ?: emptyList()
          
          val event = EventM(
            id = eventId,
            creatorId = userUuid,
            title = title,
            description = description,
            category = category,
            startDate = startDate,
            endDate = endDate,
            location = location,
            coordinates = coordinates,
            imageUrl = imageUrl,
            maxParticipants = maxParticipants,
            tags = tags,
            createdAt = now,
            updatedAt = now
          )
          
          collEvents.insertOne(event)
          
          call.respond(mapOf(
            "event" to event.toApi(call.host, call.port, userUuid)
          ))
          
        } catch (ex: Exception) {
          return@post call.respondInvalidBody("Invalid event data: ${ex.message}")
        }
      }
      
      // GET событие по ID
      get(ApiV1Routes.eventIdId) {
        val userUuid = authUserId
        val eventId = call.parameters["id"]?.let { 
          try { UUID.fromString(it) } catch (e: Exception) { 
            return@get call.respondInvalidBody("Invalid UUID format")
          }
        } ?: return@get call.respondInvalidBody("ID parameter is required")
        
        val event = collEvents
          .find(Filters.and(
            Filters.eq(EventM::id.name, eventId),
            Filters.eq(EventM::isActive.name, true)
          ))
          .firstOrNull()
        
        if (event == null) {
          return@get call.respondNotFound("Event not found")
        }
        
        call.respond(mapOf(
          "event" to event.toApi(call.host, call.port, userUuid)
        ))
      }
      
      // POST записаться на событие
      post("${ApiV1Routes.eventIdId}/attend") {
        val userUuid = authUserId
        val eventId = call.parameters["id"]?.let { 
          try { UUID.fromString(it) } catch (e: Exception) { 
            return@post call.respondInvalidBody("Invalid UUID format")
          }
        } ?: return@post call.respondInvalidBody("ID parameter is required")
        
        val event = collEvents
          .find(Filters.and(
            Filters.eq(EventM::id.name, eventId),
            Filters.eq(EventM::isActive.name, true)
          ))
          .firstOrNull()
        
        if (event == null) {
          return@post call.respondNotFound("Event not found")
        }
        
        if (event.participants.contains(userUuid)) {
          return@post call.respond(mapOf("message" to "Already attending"))
        }
        
        val maxParts = event.maxParticipants
        if (maxParts != null && event.participants.size >= maxParts) {
          return@post call.respondInvalidBody("Event is full")
        }
        
        collEvents.updateOne(
          Filters.eq(EventM::id.name, eventId),
          Updates.combine(
            Updates.addToSet(EventM::participants.name, userUuid),
            Updates.set(EventM::updatedAt.name, now())
          )
        )
        
        call.respond(mapOf("message" to "Successfully registered for event"))
      }
      
      // DELETE отписаться от события
      delete("${ApiV1Routes.eventIdId}/attend") {
        val userUuid = authUserId
        val eventId = call.parameters["id"]?.let { 
          try { UUID.fromString(it) } catch (e: Exception) { 
            return@delete call.respondInvalidBody("Invalid UUID format")
          }
        } ?: return@delete call.respondInvalidBody("ID parameter is required")
        
        collEvents.updateOne(
          Filters.eq(EventM::id.name, eventId),
          Updates.combine(
            Updates.pull(EventM::participants.name, userUuid),
            Updates.set(EventM::updatedAt.name, now())
          )
        )
        
        call.respond(mapOf("message" to "Successfully unregistered from event"))
      }
      
      // POST поставить реакцию
      post("${ApiV1Routes.eventIdId}/react") {
        val userUuid = authUserId
        val eventId = call.parameters["id"]?.let { 
          try { UUID.fromString(it) } catch (e: Exception) { 
            return@post call.respondInvalidBody("Invalid UUID format")
          }
        } ?: return@post call.respondInvalidBody("ID parameter is required")
        
        val data =
          try { call.receive<JsonNode>() }
          catch (ex: Exception) { return@post call.respondInvalidBody() }
        
        val reactionType = try {
          JacksonObjectMapper.treeToValue<EventReactionType>(data["type"])
        } catch (ex: Exception) {
          return@post call.respondInvalidBody("Invalid reaction type")
        }
        
        val existingReaction = collEventReactions
          .find(Filters.and(
            Filters.eq(EventReactionM::userId.name, userUuid),
            Filters.eq(EventReactionM::eventId.name, eventId)
          ))
          .firstOrNull()
        
        useSingleDocTx { session ->
          if (existingReaction != null) {
            // Обновляем существующую реакцию
            collEventReactions.updateOne(
              session,
              Filters.and(
                Filters.eq(EventReactionM::userId.name, userUuid),
                Filters.eq(EventReactionM::eventId.name, eventId)
              ),
              Updates.combine(
                Updates.set(EventReactionM::reactionType.name, reactionType),
                Updates.set(EventReactionM::reactedAt.name, now())
              )
            )
            
            // Обновляем счетчики
            val likeDelta = when {
              existingReaction.reactionType == EventReactionType.LIKE && reactionType != EventReactionType.LIKE -> -1
              existingReaction.reactionType != EventReactionType.LIKE && reactionType == EventReactionType.LIKE -> 1
              else -> 0
            }
            
            val fireDelta = when {
              existingReaction.reactionType == EventReactionType.FIRE && reactionType != EventReactionType.FIRE -> -1
              existingReaction.reactionType != EventReactionType.FIRE && reactionType == EventReactionType.FIRE -> 1
              else -> 0
            }
            
            if (likeDelta != 0) {
              collEvents.updateOne(
                session,
                Filters.eq(EventM::id.name, eventId),
                Updates.inc(EventM::likesCount.name, likeDelta)
              )
            }
            
            if (fireDelta != 0) {
              collEvents.updateOne(
                session,
                Filters.eq(EventM::id.name, eventId),
                Updates.inc(EventM::fireCount.name, fireDelta)
              )
            }
          } else {
            // Создаем новую реакцию
            collEventReactions.insertOne(session, EventReactionM(
              userId = userUuid,
              eventId = eventId,
              reactionType = reactionType,
              reactedAt = now()
            ))
            
            // Увеличиваем счетчик
            val updateField = when (reactionType) {
              EventReactionType.LIKE -> EventM::likesCount.name
              EventReactionType.FIRE -> EventM::fireCount.name
            }
            
            collEvents.updateOne(
              session,
              Filters.eq(EventM::id.name, eventId),
              Updates.inc(updateField, 1)
            )
          }
        }
        
        call.respond(mapOf("message" to "Reaction added successfully"))
      }
    }
  }
}
