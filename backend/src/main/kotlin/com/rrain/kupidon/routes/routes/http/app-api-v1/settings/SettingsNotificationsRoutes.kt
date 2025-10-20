package com.rrain.kupidon.routes.routes.http.`app-api-v1`.settings

import com.fasterxml.jackson.databind.JsonNode
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.rrain.kupidon.models.db.UserDataType
import com.rrain.kupidon.models.db.UserM
import com.rrain.kupidon.models.db.UserNotificationSettingsM
import com.rrain.kupidon.models.db.projectionUserM
import com.rrain.kupidon.plugins.authUserId
import com.rrain.kupidon.routes.`response-errors`.respondInvalidBody
import com.rrain.kupidon.routes.`response-errors`.respondNoUserById
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.services.mongo.UpdatesUpdatedAt
import com.rrain.kupidon.services.mongo.collUsers
import com.rrain.kupidon.services.mongo.useSingleDocTx
import com.rrain.utils.base.`date-time`.now
import com.rrain.utils.ktor.call.host
import com.rrain.utils.ktor.call.port
import com.rrain.utils.ktor.call.queryParams
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.TimeZone

/**
 * GET /api/v1/settings/notifications - получить настройки уведомлений
 * PUT /api/v1/settings/notifications - обновить настройки уведомлений
 */
fun Application.addSettingsNotificationsRoutes() {
  
  routing {
    authenticate {
      // GET настройки уведомлений
      get("${ApiV1Routes.settings}/notifications") {
        val userUuid = authUserId
        val timeZone = call.queryParams["timeZone"].let { TimeZone.of(it ?: "UTC+0") }
        
        val user = collUsers
          .find(Filters.eq(UserM::id.name, userUuid))
          .projectionUserM()
          .firstOrNull()
        
        if (user == null) {
          return@get call.respondNoUserById()
        }
        
        call.respond(mapOf(
          "notificationSettings" to user.notificationSettings
        ))
      }
      
      // PUT обновить настройки уведомлений
      put("${ApiV1Routes.settings}/notifications") {
        val userUuid = authUserId
        val timeZone = call.queryParams["timeZone"].let { TimeZone.of(it ?: "UTC+0") }
        
        val data =
          try { call.receive<JsonNode>() }
          catch (ex: Exception) { return@put call.respondInvalidBody() }
        
        if (!data.isObject) {
          return@put call.respondInvalidBody("Body must be json object")
        }
        
        val now = now()
        val updates = mutableListOf<org.bson.conversions.Bson>()
        
        val booleanFields = listOf(
          "pushEnabled", "emailEnabled", "messages", "likes", "matches",
          "events", "dateIdeas", "systemUpdates", "marketing",
          "soundEnabled", "vibrationEnabled"
        )
        
        data.properties().forEach { (k, v) -> when(k) {
          in booleanFields -> {
            try {
              if (!v.isBoolean) throw RuntimeException()
              val value = v.asBoolean()
              updates.add(Updates.set("${UserM::notificationSettings.name}.$k", value))
            }
            catch (ex: Exception) {
              return@put call.respondInvalidBody("$k must be boolean")
            }
          }
          
          "pushSubscription" -> {
            try {
              val value = if (v.isNull) null else v.asText()
              updates.add(Updates.set("${UserM::notificationSettings.name}.${UserNotificationSettingsM::pushSubscription.name}", value))
            }
            catch (ex: Exception) {
              return@put call.respondInvalidBody("pushSubscription must be string or null")
            }
          }
          
          else -> {
            return@put call.respondInvalidBody("Unknown property '$k'")
          }
        }}
        
        if (updates.isEmpty()) {
          return@put call.respondInvalidBody("No updates provided")
        }
        
        val user = useSingleDocTx { session ->
          updates.add(UpdatesUpdatedAt(UserM::updatedAt.name, now))
          
          collUsers.updateOne(
            session,
            Filters.eq(UserM::id.name, userUuid),
            Updates.combine(updates)
          )
          
          collUsers
            .find(session, Filters.eq(UserM::id.name, userUuid))
            .projectionUserM()
            .first()
        }
        
        call.respond(mapOf(
          "user" to user.toApi(UserDataType.Current, call.host, call.port, timeZone),
          "notificationSettings" to user.notificationSettings
        ))
      }
    }
  }
}
