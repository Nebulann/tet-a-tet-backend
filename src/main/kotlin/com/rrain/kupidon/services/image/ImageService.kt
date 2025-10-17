package com.rrain.kupidon.services.image

import com.mongodb.client.gridfs.GridFSBucket
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.gridfs.model.GridFSUploadOptions
import com.rrain.kupidon.models.db.ImageM
import com.rrain.kupidon.models.db.ImageType
import com.rrain.kupidon.services.mongo.MongoService
import kotlinx.datetime.Clock
import org.bson.Document
import org.bson.types.ObjectId
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID
import javax.imageio.ImageIO

/**
 * Сервис для работы с изображениями через MongoDB GridFS
 */
class ImageService(private val mongoService: MongoService) {
  
  private val gridFsBucket: GridFSBucket by lazy {
    GridFSBuckets.create(mongoService.database, "images")
  }
  
  private val imagesCollection by lazy {
    mongoService.database.getCollection("images_metadata")
  }
  
  companion object {
    // Максимальный размер файла: 10 МБ
    const val MAX_FILE_SIZE = 10 * 1024 * 1024
    
    // Разрешенные MIME типы
    val ALLOWED_MIME_TYPES = setOf(
      "image/jpeg",
      "image/jpg",
      "image/png",
      "image/webp",
      "image/gif"
    )
    
    // Максимальные размеры изображения
    const val MAX_WIDTH = 2048
    const val MAX_HEIGHT = 2048
  }
  
  /**
   * Загрузить изображение
   */
  suspend fun uploadImage(
    userId: UUID,
    fileName: String,
    mimeType: String,
    inputStream: InputStream,
    imageType: ImageType,
    relatedEntityId: UUID? = null
  ): ImageM {
    // Валидация MIME типа
    if (mimeType !in ALLOWED_MIME_TYPES) {
      throw IllegalArgumentException("Недопустимый тип файла. Разрешены: ${ALLOWED_MIME_TYPES.joinToString()}")
    }
    
    // Читаем изображение в память
    val imageBytes = inputStream.readBytes()
    
    // Валидация размера файла
    if (imageBytes.size > MAX_FILE_SIZE) {
      throw IllegalArgumentException("Размер файла превышает максимально допустимый (${MAX_FILE_SIZE / 1024 / 1024} МБ)")
    }
    
    // Читаем изображение для получения размеров и оптимизации
    val bufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))
      ?: throw IllegalArgumentException("Не удалось прочитать изображение")
    
    var width = bufferedImage.width
    var height = bufferedImage.height
    
    // Оптимизация: изменение размера если изображение слишком большое
    val optimizedBytes = if (width > MAX_WIDTH || height > MAX_HEIGHT) {
      val scale = minOf(MAX_WIDTH.toDouble() / width, MAX_HEIGHT.toDouble() / height)
      val newWidth = (width * scale).toInt()
      val newHeight = (height * scale).toInt()
      
      width = newWidth
      height = newHeight
      
      resizeImage(bufferedImage, newWidth, newHeight, mimeType)
    } else {
      imageBytes
    }
    
    // Загружаем в GridFS
    val metadata = Document()
      .append("uploadedBy", userId.toString())
      .append("imageType", imageType.name)
      .append("relatedEntityId", relatedEntityId?.toString())
    
    val options = GridFSUploadOptions()
      .metadata(metadata)
    
    val gridFsId = gridFsBucket.uploadFromStream(
      fileName,
      ByteArrayInputStream(optimizedBytes),
      options
    )
    
    // Создаем метаданные
    val imageId = UUID.randomUUID()
    val now = Clock.System.now()
    
    val imageM = ImageM(
      id = imageId,
      uploadedBy = userId,
      originalFileName = fileName,
      mimeType = mimeType,
      fileSize = optimizedBytes.size.toLong(),
      width = width,
      height = height,
      gridFsId = gridFsId.toString(),
      imageType = imageType,
      relatedEntityId = relatedEntityId,
      uploadedAt = now,
      isActive = true
    )
    
    // Сохраняем метаданные в коллекцию
    val doc = Document()
      .append("_id", imageId.toString())
      .append("uploadedBy", userId.toString())
      .append("originalFileName", fileName)
      .append("mimeType", mimeType)
      .append("fileSize", imageM.fileSize)
      .append("width", width)
      .append("height", height)
      .append("gridFsId", gridFsId.toString())
      .append("imageType", imageType.name)
      .append("relatedEntityId", relatedEntityId?.toString())
      .append("uploadedAt", now.toString())
      .append("isActive", true)
    
    imagesCollection.insertOne(doc)
    
    return imageM
  }
  
  /**
   * Получить изображение по ID
   */
  suspend fun getImage(imageId: UUID): Pair<ImageM, InputStream>? {
    // Получаем метаданные
    val doc = imagesCollection.find(Document("_id", imageId.toString())).firstOrNull()
      ?: return null
    
    if (doc.getBoolean("isActive") != true) {
      return null
    }
    
    val imageM = ImageM(
      id = UUID.fromString(doc.getString("_id")),
      uploadedBy = UUID.fromString(doc.getString("uploadedBy")),
      originalFileName = doc.getString("originalFileName"),
      mimeType = doc.getString("mimeType"),
      fileSize = doc.getLong("fileSize"),
      width = doc.getInteger("width"),
      height = doc.getInteger("height"),
      gridFsId = doc.getString("gridFsId"),
      imageType = ImageType.valueOf(doc.getString("imageType")),
      relatedEntityId = doc.getString("relatedEntityId")?.let { UUID.fromString(it) },
      uploadedAt = kotlinx.datetime.Instant.parse(doc.getString("uploadedAt")),
      isActive = doc.getBoolean("isActive")
    )
    
    // Получаем файл из GridFS
    val gridFsFile = gridFsBucket.find(Document("_id", ObjectId(imageM.gridFsId))).firstOrNull()
      ?: return null
    
    val inputStream = gridFsBucket.openDownloadStream(gridFsFile.objectId)
    
    return Pair(imageM, inputStream)
  }
  
  /**
   * Получить метаданные изображения
   */
  suspend fun getImageMetadata(imageId: UUID): ImageM? {
    val doc = imagesCollection.find(Document("_id", imageId.toString())).firstOrNull()
      ?: return null
    
    if (doc.getBoolean("isActive") != true) {
      return null
    }
    
    return ImageM(
      id = UUID.fromString(doc.getString("_id")),
      uploadedBy = UUID.fromString(doc.getString("uploadedBy")),
      originalFileName = doc.getString("originalFileName"),
      mimeType = doc.getString("mimeType"),
      fileSize = doc.getLong("fileSize"),
      width = doc.getInteger("width"),
      height = doc.getInteger("height"),
      gridFsId = doc.getString("gridFsId"),
      imageType = ImageType.valueOf(doc.getString("imageType")),
      relatedEntityId = doc.getString("relatedEntityId")?.let { UUID.fromString(it) },
      uploadedAt = kotlinx.datetime.Instant.parse(doc.getString("uploadedAt")),
      isActive = doc.getBoolean("isActive")
    )
  }
  
  /**
   * Удалить изображение (soft delete)
   */
  suspend fun deleteImage(imageId: UUID, userId: UUID): Boolean {
    val doc = imagesCollection.find(Document("_id", imageId.toString())).firstOrNull()
      ?: return false
    
    // Проверяем, что пользователь является владельцем изображения
    if (doc.getString("uploadedBy") != userId.toString()) {
      throw IllegalArgumentException("У вас нет прав на удаление этого изображения")
    }
    
    // Soft delete - помечаем как неактивное
    imagesCollection.updateOne(
      Document("_id", imageId.toString()),
      Document("\$set", Document("isActive", false))
    )
    
    return true
  }
  
  /**
   * Удалить изображение физически (для администраторов)
   */
  suspend fun deleteImagePermanently(imageId: UUID): Boolean {
    val doc = imagesCollection.find(Document("_id", imageId.toString())).firstOrNull()
      ?: return false
    
    val gridFsId = doc.getString("gridFsId")
    
    // Удаляем из GridFS
    gridFsBucket.delete(ObjectId(gridFsId))
    
    // Удаляем метаданные
    imagesCollection.deleteOne(Document("_id", imageId.toString()))
    
    return true
  }
  
  /**
   * Получить все изображения пользователя
   */
  suspend fun getUserImages(userId: UUID, imageType: ImageType? = null): List<ImageM> {
    val filter = Document("uploadedBy", userId.toString())
      .append("isActive", true)
    
    if (imageType != null) {
      filter.append("imageType", imageType.name)
    }
    
    return imagesCollection.find(filter).map { doc ->
      ImageM(
        id = UUID.fromString(doc.getString("_id")),
        uploadedBy = UUID.fromString(doc.getString("uploadedBy")),
        originalFileName = doc.getString("originalFileName"),
        mimeType = doc.getString("mimeType"),
        fileSize = doc.getLong("fileSize"),
        width = doc.getInteger("width"),
        height = doc.getInteger("height"),
        gridFsId = doc.getString("gridFsId"),
        imageType = ImageType.valueOf(doc.getString("imageType") ?: "OTHER"),
        relatedEntityId = doc.getString("relatedEntityId")?.let { UUID.fromString(it) },
        uploadedAt = kotlinx.datetime.Instant.parse(doc.getString("uploadedAt") ?: ""),
        isActive = doc.getBoolean("isActive")
      )
    }.toList()
  }
  
  /**
   * Изменить размер изображения
   */
  private fun resizeImage(
    originalImage: BufferedImage,
    targetWidth: Int,
    targetHeight: Int,
    mimeType: String
  ): ByteArray {
    val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
    val graphics = resizedImage.createGraphics()
    
    graphics.drawImage(
      originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH),
      0,
      0,
      null
    )
    graphics.dispose()
    
    val outputStream = ByteArrayOutputStream()
    val format = when (mimeType) {
      "image/png" -> "png"
      "image/webp" -> "webp"
      "image/gif" -> "gif"
      else -> "jpg"
    }
    
    ImageIO.write(resizedImage, format, outputStream)
    
    return outputStream.toByteArray()
  }
}
