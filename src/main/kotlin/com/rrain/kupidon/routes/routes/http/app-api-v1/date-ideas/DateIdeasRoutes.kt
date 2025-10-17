package com.rrain.kupidon.routes.routes.http.`app-api-v1`.`date-ideas`

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.rrain.kupidon.models.db.DateIdeaCategory
import com.rrain.kupidon.models.db.DateIdeaLikeM
import com.rrain.kupidon.models.db.DateIdeaM
import com.rrain.kupidon.plugins.JacksonObjectMapper
import com.rrain.kupidon.plugins.authUserId
import com.rrain.kupidon.routes.`response-errors`.respondInvalidBody
import com.rrain.kupidon.routes.`response-errors`.respondNotFound
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.services.mongo.collDateIdeas
import com.rrain.kupidon.services.mongo.collDateIdeaLikes
import com.rrain.kupidon.services.mongo.useSingleDocTx
import com.rrain.utils.base.`date-time`.now
import com.rrain.utils.ktor.call.queryParams
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import java.util.UUID

/**
 * GET /api/v1/date-ideas - получить список идей для свиданий
 * GET /api/v1/date-ideas/id/{id} - получить идею по ID
 * POST /api/v1/date-ideas/id/{id}/like - лайкнуть идею
 * DELETE /api/v1/date-ideas/id/{id}/like - убрать лайк с идеи
 * GET /api/v1/date-ideas/favorites - получить избранные идеи
 */
fun Application.addDateIdeasRoutes() {
  
  routing {
    authenticate {
      // GET список идей для свиданий
      get(ApiV1Routes.dateIdeas) {
        val userUuid = authUserId
        val queryParams = call.queryParams
        
        val page = queryParams["page"]?.toIntOrNull() ?: 0
        val limit = (queryParams["limit"]?.toIntOrNull() ?: 20).coerceIn(1, 100)
        val category = queryParams["category"]?.let { 
          try { DateIdeaCategory.valueOf(it) } catch (e: Exception) { null }
        }
        val search = queryParams["search"]?.takeIf { it.isNotBlank() }
        
        val filters = mutableListOf(
          Filters.eq(DateIdeaM::isActive.name, true)
        )
        
        if (category != null) {
          filters.add(Filters.eq(DateIdeaM::category.name, category))
        }
        
        if (search != null) {
          filters.add(
            Filters.or(
              Filters.regex(DateIdeaM::title.name, search, "i"),
              Filters.regex(DateIdeaM::description.name, search, "i"),
              Filters.`in`(DateIdeaM::tags.name, search)
            )
          )
        }
        
        val totalFilter = if (filters.size == 1) filters[0] else Filters.and(filters)
        
        val total = collDateIdeas.countDocuments(totalFilter)
        val ideas = collDateIdeas
          .find(totalFilter)
          .sort(Sorts.descending(DateIdeaM::rating.name, DateIdeaM::likesCount.name))
          .skip(page * limit)
          .limit(limit)
          .toList()
        
        // Получаем лайки пользователя
        val likedIdeaIds = collDateIdeaLikes
          .find(Filters.eq(DateIdeaLikeM::userId.name, userUuid))
          .toList()
          .map { it.dateIdeaId }
          .toSet()
        
        val ideasWithLikes = ideas.map { idea ->
          val ideaApi = idea.toApi().toMutableMap()
          ideaApi["isLiked"] = likedIdeaIds.contains(idea.id)
          ideaApi
        }
        
        call.respond(mapOf(
          "ideas" to ideasWithLikes,
          "pagination" to mapOf(
            "page" to page,
            "limit" to limit,
            "total" to total,
            "hasNext" to ((page + 1) * limit < total)
          )
        ))
      }
      
      // GET идея по ID
      get(ApiV1Routes.dateIdeaIdId) {
        val userUuid = authUserId
        val ideaId = call.parameters["id"]?.let { 
          try { UUID.fromString(it) } catch (e: Exception) { 
            return@get call.respondInvalidBody("Invalid UUID format")
          }
        } ?: return@get call.respondInvalidBody("ID parameter is required")
        
        val idea = collDateIdeas
          .find(Filters.and(
            Filters.eq(DateIdeaM::id.name, ideaId),
            Filters.eq(DateIdeaM::isActive.name, true)
          ))
          .firstOrNull()
        
        if (idea == null) {
          return@get call.respondNotFound("Date idea not found")
        }
        
        val isLiked = collDateIdeaLikes
          .find(Filters.and(
            Filters.eq(DateIdeaLikeM::userId.name, userUuid),
            Filters.eq(DateIdeaLikeM::dateIdeaId.name, ideaId)
          ))
          .firstOrNull() != null
        
        val ideaApi = idea.toApi().toMutableMap()
        ideaApi["isLiked"] = isLiked
        
        call.respond(mapOf("idea" to ideaApi))
      }
      
      // POST лайкнуть идею
      post("${ApiV1Routes.dateIdeaIdId}/like") {
        val userUuid = authUserId
        val ideaId = call.parameters["id"]?.let { 
          try { UUID.fromString(it) } catch (e: Exception) { 
            return@post call.respondInvalidBody("Invalid UUID format")
          }
        } ?: return@post call.respondInvalidBody("ID parameter is required")
        
        val idea = collDateIdeas
          .find(Filters.and(
            Filters.eq(DateIdeaM::id.name, ideaId),
            Filters.eq(DateIdeaM::isActive.name, true)
          ))
          .firstOrNull()
        
        if (idea == null) {
          return@post call.respondNotFound("Date idea not found")
        }
        
        val existingLike = collDateIdeaLikes
          .find(Filters.and(
            Filters.eq(DateIdeaLikeM::userId.name, userUuid),
            Filters.eq(DateIdeaLikeM::dateIdeaId.name, ideaId)
          ))
          .firstOrNull()
        
        if (existingLike != null) {
          return@post call.respond(mapOf("message" to "Already liked"))
        }
        
        useSingleDocTx { session ->
          // Добавляем лайк
          collDateIdeaLikes.insertOne(session, DateIdeaLikeM(
            userId = userUuid,
            dateIdeaId = ideaId,
            likedAt = now()
          ))
          
          // Увеличиваем счетчик лайков
          collDateIdeas.updateOne(
            session,
            Filters.eq(DateIdeaM::id.name, ideaId),
            Updates.inc(DateIdeaM::likesCount.name, 1)
          )
        }
        
        call.respond(mapOf("message" to "Liked successfully"))
      }
      
      // DELETE убрать лайк
      delete("${ApiV1Routes.dateIdeaIdId}/like") {
        val userUuid = authUserId
        val ideaId = call.parameters["id"]?.let { 
          try { UUID.fromString(it) } catch (e: Exception) { 
            return@delete call.respondInvalidBody("Invalid UUID format")
          }
        } ?: return@delete call.respondInvalidBody("ID parameter is required")
        
        val existingLike = collDateIdeaLikes
          .find(Filters.and(
            Filters.eq(DateIdeaLikeM::userId.name, userUuid),
            Filters.eq(DateIdeaLikeM::dateIdeaId.name, ideaId)
          ))
          .firstOrNull()
        
        if (existingLike == null) {
          return@delete call.respond(mapOf("message" to "Not liked"))
        }
        
        useSingleDocTx { session ->
          // Удаляем лайк
          collDateIdeaLikes.deleteOne(
            session,
            Filters.and(
              Filters.eq(DateIdeaLikeM::userId.name, userUuid),
              Filters.eq(DateIdeaLikeM::dateIdeaId.name, ideaId)
            )
          )
          
          // Уменьшаем счетчик лайков
          collDateIdeas.updateOne(
            session,
            Filters.eq(DateIdeaM::id.name, ideaId),
            Updates.inc(DateIdeaM::likesCount.name, -1)
          )
        }
        
        call.respond(mapOf("message" to "Unliked successfully"))
      }
      
      // GET избранные идеи
      get("${ApiV1Routes.dateIdeas}/favorites") {
        val userUuid = authUserId
        val queryParams = call.queryParams
        
        val page = queryParams["page"]?.toIntOrNull() ?: 0
        val limit = (queryParams["limit"]?.toIntOrNull() ?: 20).coerceIn(1, 100)
        
        val likedIdeaIds = collDateIdeaLikes
          .find(Filters.eq(DateIdeaLikeM::userId.name, userUuid))
          .toList()
          .map { it.dateIdeaId }
        
        if (likedIdeaIds.isEmpty()) {
          return@get call.respond(mapOf(
            "ideas" to emptyList<Any>(),
            "pagination" to mapOf(
              "page" to page,
              "limit" to limit,
              "total" to 0,
              "hasNext" to false
            )
          ))
        }
        
        val total = likedIdeaIds.size.toLong()
        val ideas = collDateIdeas
          .find(Filters.and(
            Filters.`in`(DateIdeaM::id.name, likedIdeaIds),
            Filters.eq(DateIdeaM::isActive.name, true)
          ))
          .sort(Sorts.descending(DateIdeaM::rating.name))
          .skip(page * limit)
          .limit(limit)
          .toList()
        
        val ideasWithLikes = ideas.map { idea ->
          val ideaApi = idea.toApi().toMutableMap()
          ideaApi["isLiked"] = true
          ideaApi
        }
        
        call.respond(mapOf(
          "ideas" to ideasWithLikes,
          "pagination" to mapOf(
            "page" to page,
            "limit" to limit,
            "total" to total,
            "hasNext" to ((page + 1) * limit < total)
          )
        ))
      }
    }
  }
}
