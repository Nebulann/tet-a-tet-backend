package com.rrain.kupidon.routes.routes.http.`app-api-v1`.images

import com.rrain.kupidon.models.db.ImageType
import com.rrain.kupidon.plugins.authUserId
import com.rrain.kupidon.routes.`response-errors`.respondError
import com.rrain.kupidon.routes.`response-errors`.respondInvalidBody
import com.rrain.kupidon.routes.`response-errors`.respondNotFound
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.services.image.ImageService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

/**
 * API для работы с изображениями
 * POST /api/v1/upload/image - загрузить изображение
 * GET /api/v1/images/{id} - получить изображение по ID
 * GET /api/v1/images/{id}/metadata - получить метаданные изображения
 * DELETE /api/v1/images/{id} - удалить изображение
 * GET /api/v1/user/images - получить все изображения пользователя
 */
fun Application.addImagesRoutes() {
  
  val imageService = ImageService()
  
  routing {
    
    // POST - Загрузить изображение
    authenticate {
      post(ApiV1Routes.uploadImage) {
        try {
          val userId = authUserId
          
          val multipart = call.receiveMultipart()
          var fileName: String? = null
          var mimeType: String? = null
          var inputStream: java.io.InputStream? = null
          var imageType: ImageType = ImageType.OTHER
          var relatedEntityId: UUID? = null
          
          multipart.forEachPart { part ->
            when (part) {
              is PartData.FileItem -> {
                fileName = part.originalFileName ?: "image"
                mimeType = part.contentType?.toString() ?: "image/jpeg"
                inputStream = part.streamProvider()
              }
              is PartData.FormItem -> {
                when (part.name) {
                  "imageType" -> {
                    imageType = try {
                      ImageType.valueOf(part.value.uppercase())
                    } catch (e: Exception) {
                      ImageType.OTHER
                    }
                  }
                  "relatedEntityId" -> {
                    relatedEntityId = try {
                      UUID.fromString(part.value)
                    } catch (e: Exception) {
                      null
                    }
                  }
                }
              }
              else -> {}
            }
            part.dispose()
          }
          
          if (fileName == null || mimeType == null || inputStream == null) {
            return@post call.respondInvalidBody("Не указан файл изображения")
          }
          
          val image = imageService.uploadImage(
            userId = userId,
            originalFileName = fileName!!,
            mimeType = mimeType!!,
            inputStream = inputStream!!,
            imageType = imageType,
            relatedEntityId = relatedEntityId
          )
          
          val host = call.request.host()
          val port = call.request.port()
          
          call.respond(
            HttpStatusCode.Created,
            mapOf(
              "success" to true,
              "message" to "Изображение успешно загружено",
              "data" to image.toApi(host, port)
            )
          )
        } catch (e: IllegalArgumentException) {
          call.respondError(HttpStatusCode.BadRequest, e.message ?: "Ошибка валидации")
        } catch (e: Exception) {
          e.printStackTrace()
          call.respondError(HttpStatusCode.InternalServerError, "Ошибка при загрузке изображения")
        }
      }
    }
    
    // GET - Получить изображение по ID (публичный endpoint)
    get(ApiV1Routes.imagesIdId) {
      try {
        val imageId = call.parameters["id"]?.let { 
          try { UUID.fromString(it) } catch (e: Exception) { null }
        } ?: return@get call.respondInvalidBody("Неверный ID изображения")
        
        val result = imageService.getImageWithData(imageId)
        
        if (result == null) {
          return@get call.respondNotFound("Изображение не найдено")
        }
        
        val (imageM, inputStream) = result
        
        call.response.header(HttpHeaders.ContentType, imageM.mimeType)
        call.response.header(HttpHeaders.ContentLength, imageM.fileSize.toString())
        call.response.header(
          HttpHeaders.ContentDisposition,
          "inline; filename=\"${imageM.originalFileName}\""
        )
        
        call.respondOutputStream(ContentType.parse(imageM.mimeType)) {
          inputStream.use { input ->
            input.copyTo(this)
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
        call.respondError(HttpStatusCode.InternalServerError, "Ошибка при получении изображения")
      }
    }
    
    // GET - Получить метаданные изображения
    authenticate {
      get(ApiV1Routes.imagesIdMetadata) {
        try {
          val imageId = call.parameters["id"]?.let { 
            try { UUID.fromString(it) } catch (e: Exception) { null }
          } ?: return@get call.respondInvalidBody("Неверный ID изображения")
          
          val imageM = imageService.getImageMetadata(imageId)
          
          if (imageM == null) {
            return@get call.respondNotFound("Изображение не найдено")
          }
          
          val host = call.request.host()
          val port = call.request.port()
          
          call.respond(
            mapOf(
              "success" to true,
              "data" to imageM.toApi(host, port)
            )
          )
        } catch (e: Exception) {
          e.printStackTrace()
          call.respondError(HttpStatusCode.InternalServerError, "Ошибка при получении метаданных")
        }
      }
    }
    
    // DELETE - Удалить изображение
    authenticate {
      delete(ApiV1Routes.imagesIdId) {
        try {
          val userId = authUserId
          
          val imageId = call.parameters["id"]?.let { 
            try { UUID.fromString(it) } catch (e: Exception) { null }
          } ?: return@delete call.respondInvalidBody("Неверный ID изображения")
          
          val deleted = imageService.deleteImage(imageId, userId)
          
          if (!deleted) {
            return@delete call.respondNotFound("Изображение не найдено")
          }
          
          call.respond(
            mapOf(
              "success" to true,
              "message" to "Изображение успешно удалено"
            )
          )
        } catch (e: IllegalArgumentException) {
          call.respondError(HttpStatusCode.Forbidden, e.message ?: "Нет прав на удаление")
        } catch (e: Exception) {
          e.printStackTrace()
          call.respondError(HttpStatusCode.InternalServerError, "Ошибка при удалении изображения")
        }
      }
    }
    
    // GET - Получить все изображения пользователя
    authenticate {
      get(ApiV1Routes.userImages) {
        try {
          val userId = authUserId
          
          val imageTypeParam = call.request.queryParameters["imageType"]
          val imageType = imageTypeParam?.let {
            try {
              ImageType.valueOf(it.uppercase())
            } catch (e: Exception) {
              null
            }
          }
          
          val images = imageService.getUserImages(userId, imageType)
          
          val host = call.request.host()
          val port = call.request.port()
          
          call.respond(
            mapOf(
              "success" to true,
              "data" to images.map { it.toApi(host, port) }
            )
          )
        } catch (e: Exception) {
          e.printStackTrace()
          call.respondError(HttpStatusCode.InternalServerError, "Ошибка при получении изображений")
        }
      }
    }
  }
}
