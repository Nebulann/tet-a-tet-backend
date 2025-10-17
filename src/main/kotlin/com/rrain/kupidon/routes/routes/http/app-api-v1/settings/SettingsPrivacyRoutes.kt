package com.rrain.kupidon.routes.routes.http.`app-api-v1`.settings

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.rrain.kupidon.models.db.MessagePermission
import com.rrain.kupidon.models.db.ProfileVisibility
import com.rrain.kupidon.models.db.UserDataType
import com.rrain.kupidon.models.db.UserM
import com.rrain.kupidon.models.db.UserPrivacySettingsM
import com.rrain.kupidon.models.db.projectionUserM
import com.rrain.kupidon.plugins.JacksonObjectMapper
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
 * GET /api/v1/settings/privacy - получить настройки приватности
 * PUT /api/v1/settings/privacy - обновить настройки приватности
 */
fun Application.addSettingsPrivacyRoutes() {
  
  routing {
    authenticate {
      // GET настройки приватности
      get("${ApiV1Routes.settings}/privacy") {
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
          "privacySettings" to user.privacySettings
        ))
      }
      
      // PUT обновить настройки приватности
      put("${ApiV1Routes.settings}/privacy") {
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
        
        data.properties().forEach { (k, v) -> when(k) {
          "profileVisibility" -> {
            try {
              val visibility = JacksonObjectMapper.treeToValue<ProfileVisibility>(v)
              updates.add(Updates.set("${UserM::privacySettings.name}.${UserPrivacySettingsM::profileVisibility.name}", visibility))
            }
            catch (ex: Exception) {
              return@put call.respondInvalidBody(
                "profileVisibility must be one of ${ProfileVisibility.entries}"
              )
            }
          }
          
          "canMessage" -> {
            try {
              val permission = JacksonObjectMapper.treeToValue<MessagePermission>(v)
              updates.add(Updates.set("${UserM::privacySettings.name}.${UserPrivacySettingsM::canMessage.name}", permission))
            }
            catch (ex: Exception) {
              return@put call.respondInvalidBody(
                "canMessage must be one of ${MessagePermission.entries}"
              )
            }
          }
          
          "showOnlineStatus" -> {
            try {
              if (!v.isBoolean) throw RuntimeException()
              val value = v.asBoolean()
              updates.add(Updates.set("${UserM::privacySettings.name}.${UserPrivacySettingsM::showOnlineStatus.name}", value))
            }
            catch (ex: Exception) {
              return@put call.respondInvalidBody("showOnlineStatus must be boolean")
            }
          }
          
          "showDistance" -> {
            try {
              if (!v.isBoolean) throw RuntimeException()
              val value = v.asBoolean()
              updates.add(Updates.set("${UserM::privacySettings.name}.${UserPrivacySettingsM::showDistance.name}", value))
            }
            catch (ex: Exception) {
              return@put call.respondInvalidBody("showDistance must be boolean")
            }
          }
          
          "showLastSeen" -> {
            try {
              if (!v.isBoolean) throw RuntimeException()
              val value = v.asBoolean()
              updates.add(Updates.set("${UserM::privacySettings.name}.${UserPrivacySettingsM::showLastSeen.name}", value))
            }
            catch (ex: Exception) {
              return@put call.respondInvalidBody("showLastSeen must be boolean")
            }
          }
          
          "blockedUsers" -> {
            try {
              val blockedUsers = JacksonObjectMapper.treeToValue<List<String>>(v)
              updates.add(Updates.set("${UserM::privacySettings.name}.${UserPrivacySettingsM::blockedUsers.name}", blockedUsers))
            }
            catch (ex: Exception) {
              return@put call.respondInvalidBody("blockedUsers must be array of strings")
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
          "privacySettings" to user.privacySettings
        ))
      }
    }
  }
}
