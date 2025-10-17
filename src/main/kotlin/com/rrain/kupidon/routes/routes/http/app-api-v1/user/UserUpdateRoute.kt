package com.rrain.kupidon.routes.routes.http.`app-api-v1`.user

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.client.model.WriteModel
import com.rrain.kupidon.models.Gender
import com.rrain.kupidon.models.LookingFor
import com.rrain.kupidon.plugins.JacksonObjectMapper
import com.rrain.kupidon.plugins.authUserId
import com.rrain.kupidon.services.`pwd-hash`.PwdHashService
import com.rrain.kupidon.routes.`response-errors`.respondBadRequest
import com.rrain.kupidon.routes.`response-errors`.respondInvalidBody
import com.rrain.kupidon.routes.`response-errors`.respondNoUserById
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.services.mongo.UpdatesUpdatedAt
import com.rrain.kupidon.services.mongo.collUsers
import com.rrain.kupidon.models.db.UserDataType
import com.rrain.kupidon.models.db.UserM
import com.rrain.kupidon.models.db.UserProfilePhotoMetadataM
import com.rrain.kupidon.models.db.UserProfilePhotoM
import com.rrain.kupidon.models.db.projectionUserM
import com.rrain.kupidon.services.mongo.useSingleDocTx
import com.rrain.utils.ktor.call.host
import com.rrain.utils.ktor.call.port
import com.rrain.utils.ktor.call.queryParams
import com.rrain.utils.base.`date-time`.now
import com.rrain.utils.base.`date-time`.toLocalDate
import com.rrain.utils.base.`date-time`.toZonedInstant
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.yearsUntil
import org.bson.Document
import java.util.UUID




fun Application.addUserUpdateRoute() {
  
  data class ReplacePhoto(
    val id: UUID,
    val index: Int,
  )
  data class PhotosUpdates (
    val remove: List<UUID>,
    val replace: List<ReplacePhoto>,
  )
  
  data class PreparedPhotosUpdates(
    val remove: List<UUID>,
    val replace: List<ReplacePhoto>,
  )
  
  
  class Update {
    var props: MutableMap<String, Any?> = mutableMapOf(
      "currentPwdHashed" to null,
      "newPwdHashed" to null,
    )
    
    var name: String by props
    var birthDate: LocalDate by props
    var gender: Gender by props
    var aboutMe: String by props
    var lookingFor: LookingFor? by props
    
    var currentPwdHashed: String? by props
    var newPwdHashed: String? by props
    
    var photos: PreparedPhotosUpdates by props
  }
  
  
  
  
  routing {
    authenticate {
      put(ApiV1Routes.user) {
        val userUuid = authUserId
        
        val data =
          try { call.receive<JsonNode>() }
          catch (ex: Exception) { return@put call.respondInvalidBody() }
        val timeZone = call.queryParams["timeZone"].let { TimeZone.of(it ?: "UTC+0") }
        
        if (!data.isObject) {
          return@put call.respondInvalidBody("Body must be json object")
        }
        /*run {
          val allowedFields = setOf("name", "birthDate","gender","aboutMe","currentPwd","pwd","photos")
          if ((data.fieldNames().asSequence().toSet() - allowedFields).isNotEmpty()) {
            return@put call.respondInvalidBody(
              "Allowed fileds: $allowedFields"
            )
          }
        }*/
        
        val now = now()
        val update = Update()
        
        
        data.properties().forEach { (k,v) -> when(k) {
          "name" -> {
            try {
              if (!v.isTextual) throw RuntimeException()
              val name = v.asText()
              if (name.isEmpty()) throw RuntimeException()
              if (name.length>100) throw RuntimeException()
              update.name = name
            }
            catch (ex: Exception) {
              return@put call.respondInvalidBody(
                "Name must be string and must not be empty and name max length is 100"
              )
            }
          }
          
          "birthDate" -> {
            try {
              if (!v.isTextual) throw RuntimeException()
              val birthDate = v.asText().toLocalDate()
              if (birthDate.toZonedInstant(timeZone)
                  .yearsUntil(now, timeZone) < 18
              ) {
                return@put call.respondInvalidBody("You must be 18+ years old")
              }
              update.birthDate = birthDate
            }
            catch (ex: Exception) {
              return@put call.respondInvalidBody(
                "'birthDate' must be string 'yyyy-MM-dd', e.g. '2005-01-01'"
              )
            }
          }
          
          "gender" -> {
            try {
              val gender = JacksonObjectMapper.treeToValue<Gender>(v)
              update.gender = gender
            }
            catch (ex: Exception) {
              return@put call.respondInvalidBody(
                "Gender must be string of ${Gender.entries}"
              )
            }
          }
          
          "aboutMe" -> {
            try {
              if (!v.isTextual) throw RuntimeException()
              val aboutMe = v.asText()
              if (aboutMe.length>2000) throw RuntimeException()
              update.aboutMe = aboutMe
            }
            catch (ex: Exception) {
              return@put call.respondInvalidBody(
                "'About me' must be string and must have max 2000 chars"
              )
            }
          }
          
          "lookingFor" -> {
            try {
              if (v.isNull) {
                update.lookingFor = null
              } else {
                val lookingFor = JacksonObjectMapper.treeToValue<LookingFor>(v)
                update.lookingFor = lookingFor
              }
            }
            catch (ex: Exception) {
              return@put call.respondInvalidBody(
                "LookingFor must be string of ${LookingFor.entries} or null"
              )
            }
          }
          
          "currentPwd" -> {
            try {
              if (!v.isTextual) throw RuntimeException()
              var currentPwd = v.asText()
              if (currentPwd.length !in 1..200) throw RuntimeException()
              currentPwd = currentPwd.let(PwdHashService::generateHash)
              update.currentPwdHashed = currentPwd
            }
            catch (ex: Exception) {
              return@put call.respondInvalidBody(
                "Current password must be string and its length must be from 1 to 200 chars"
              )
            }
          }
          
          "pwd" -> {
            try {
              if (!v.isTextual) throw RuntimeException()
              var pwd = v.asText()
              if (pwd.length !in 6..200) throw RuntimeException()
              pwd = pwd.let(PwdHashService::generateHash)
              update.newPwdHashed = pwd
            }
            catch (ex: Exception) {
              return@put call.respondInvalidBody(
                "Password must be string and its length must be from 6 to 200 chars"
              )
            }
          }
          
          "photos" -> {
            val photosUpdates =
              try { JacksonObjectMapper.treeToValue<PhotosUpdates>(v) }
              catch (ex: Exception) {
                ex.printStackTrace()
                println(ex.message)
                return@put call.respondInvalidBody(
                  "Invalid photos format: ${ex.message}"
                )
              }
            
            photosUpdates.replace.forEachIndexed { i, it ->
              if (it.index !in 0..5) return@put call.respondInvalidBody(
                "photos.replace[$i].index must be in range ${0..5}"
              )
            }
            
            
            update.photos = PreparedPhotosUpdates(
              remove = photosUpdates.remove,
              replace = photosUpdates.replace,
            )
          }
          
          else -> {
            return@put call.respondInvalidBody("Unknown property '$k'")
          }
        } }
        
        
        
        
        
        
        val user = useSingleDocTx { session ->
          val nUserId = UserM::id.name
          val nUserPwd = UserM::pwd.name
          val nUserName = UserM::name.name
          val nUserBirthDate = UserM::birthDate.name
          val nUserGender = UserM::gender.name
          val nUserAboutMe = UserM::aboutMe.name
          val nUserLookingFor = UserM::lookingFor.name
          val nUserPhotos = UserM::photos.name
          val nUserUpdated = UserM::updatedAt.name
          
          val nPhotoId = UserProfilePhotoMetadataM::id.name
          val nPhotoIndex = UserProfilePhotoMetadataM::index.name
          val nPhotoBinData = UserProfilePhotoM::binData.name
          
          
          val userById = collUsers
            .find(session, Filters.eq(nUserId, userUuid))
            .projectionUserM()
            .firstOrNull()
          
          if (userById == null) {
            session.abortTransaction()
            return@put call.respondNoUserById()
          }
          
          
          
          val writeList = mutableListOf<WriteModel<UserM>>()
          
          
          if (update.newPwdHashed != null) {
            if (userById.pwd != update.currentPwdHashed) {
              session.abortTransaction()
              return@put call.respondBadRequest(
                "INVALID_PWD",
                "Invalid current password"
              )
            }
            writeList += UpdateOneModel(
              Filters.eq(nUserId, userUuid),
              Updates.set(nUserPwd, update.newPwdHashed),
            )
          }
          
          
          
          run {
            val updates = buildList {
              if (update::name.name in update.props)
                add(Updates.set(nUserName, update.name))
              if (update::birthDate.name in update.props)
                add(Updates.set(nUserBirthDate, update.birthDate))
              if (update::gender.name in update.props)
                add(Updates.set(nUserGender, update.gender))
              if (update::aboutMe.name in update.props)
                add(Updates.set(nUserAboutMe, update.aboutMe))
              if (update::lookingFor.name in update.props)
                add(Updates.set(nUserLookingFor, update.lookingFor))
            }
            if (updates.isNotEmpty()) {
              writeList += UpdateOneModel(
                Filters.eq(nUserId, userUuid),
                updates.let(Updates::combine)
              )
            }
          }
          
          
          
          if (update::photos.name in update.props) {
            
            // remove photos by id
            writeList += UpdateOneModel(
              Filters.eq(nUserId, userUuid),
              Updates.pull(
                nUserPhotos,
                Filters.`in`(nPhotoId, update.photos.remove)
              )
            )
            
            // replace photos by id & new index
            update.photos.replace.forEach {
              writeList += UpdateOneModel(
                Filters.eq(nUserId, userUuid),
                Updates.set(
                  "$nUserPhotos.$[i].$nPhotoIndex",
                  it.index
                ),
                UpdateOptions().arrayFilters(listOf(
                  Document("i.$nPhotoId", it.id)
                ))
              )
            }
            val replaceJson = $$"""
              db.users.updateOne(
                { id: UUID("<user-uuid-as-string>") },
                { $set: { "photos.$[i].index": <new-photo-index> } },
                { arrayFilters: [{ "i.id": UUID("<photo-uuid-as-string>") }] }
              )
            """.trimIndent()
            
          }
          
          writeList += UpdateOneModel(
            Filters.eq(nUserId, userUuid),
            listOf(UpdatesUpdatedAt(UserM::updatedAt.name, now)),
          )
          
          collUsers.bulkWrite(session,writeList)
          
          
          val updatedUser = collUsers
            .find(session, Filters.eq(UserM::id.name, userUuid))
            .projectionUserM()
            .first()
          
          // check photo indices uniqueness
          if (updatedUser.photos.size != updatedUser.photos.map { it.index }.toSet().size) {
            session.abortTransaction()
            return@put call.respondInvalidBody("Duplicate photo index")
          }
          
          updatedUser
        }
        
        
        
        call.respond(mapOf(
          "user" to user.toApi(UserDataType.Current, call.host, call.port, timeZone),
        ))
      }
    }
  }
}

