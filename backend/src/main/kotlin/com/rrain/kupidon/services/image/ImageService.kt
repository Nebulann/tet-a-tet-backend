package com.rrain.kupidon.services.image

import com.mongodb.client.gridfs.GridFSBucket
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.MongoClients
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.rrain.kupidon.models.db.ImageM
import com.rrain.kupidon.models.db.ImageType
import com.rrain.kupidon.services.mongo.mongoAppDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.bson.Document
import org.bson.types.ObjectId
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID
import javax.imageio.ImageIO

/**
 * Сервис для работы с изображениями через MongoDB GridFS
 */
class ImageService(private val database: MongoDatabase = mongoAppDb) {

  private val gridFsBucket: GridFSBucket by lazy {
    val syncDb = MongoClients.create().getDatabase(database.name)
    GridFSBuckets.create(syncDb, "images")
  }

  private val imagesCollection by lazy {
    database.getCollection<Document>("images_metadata")
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
    inputStream: InputStream,
    originalFileName: String,
    mimeType: String,
    relatedEntityId: UUID? = null,
    imageType: ImageType = ImageType.OTHER
  ): ImageM = withContext(Dispatchers.IO) {
    // Валидация
    require(mimeType in ALLOWED_MIME_TYPES) { "Неподдерживаемый тип файла: $mimeType" }

    // Читаем изображение для получения размеров
    val bufferedImage = ImageIO.read(inputStream) ?: throw IllegalArgumentException("Невозможно прочитать изображение")
    val (width, height) = bufferedImage.width to bufferedImage.height

    // Проверяем размеры
    if (width > MAX_WIDTH || height > MAX_HEIGHT) {
      throw IllegalArgumentException("Размер изображения превышает максимальный: ${MAX_WIDTH}x${MAX_HEIGHT}")
    }

    // Сбрасываем inputStream для повторного чтения
    inputStream.reset()

    // Оптимизируем изображение если нужно
    val optimizedBytes = if (width > MAX_WIDTH || height > MAX_HEIGHT) {
      val scale = minOf(MAX_WIDTH.toDouble() / width, MAX_HEIGHT.toDouble() / height)
      val newWidth = (width * scale).toInt()
      val newHeight = (height * scale).toInt()

      resizeAndConvert(bufferedImage, newWidth, newHeight, mimeType)
    } else {
      inputStream.readBytes()
    }

    // Загружаем в GridFS
    val objectId = gridFsBucket.uploadFromStream(originalFileName, ByteArrayInputStream(optimizedBytes))

    // Создаем метаданные
    val imageM = ImageM(
      id = UUID.randomUUID(),
      uploadedBy = userId,
      originalFileName = originalFileName,
      mimeType = mimeType,
      fileSize = optimizedBytes.size.toLong(),
      width = width,
      height = height,
      gridFsId = objectId.toString(),
      imageType = imageType,
      relatedEntityId = relatedEntityId,
      uploadedAt = Clock.System.now(),
      isActive = true
    )

    // Сохраняем метаданные в коллекцию
    val doc = Document()
      .append("_id", imageM.id.toString())
      .append("uploadedBy", imageM.uploadedBy.toString())
      .append("originalFileName", imageM.originalFileName)
      .append("mimeType", imageM.mimeType)
      .append("fileSize", imageM.fileSize)
      .append("width", imageM.width)
      .append("height", imageM.height)
      .append("gridFsId", imageM.gridFsId)
      .append("imageType", imageM.imageType.name)
      .append("relatedEntityId", imageM.relatedEntityId?.toString())
      .append("uploadedAt", imageM.uploadedAt.toString())
      .append("isActive", imageM.isActive)

    imagesCollection.insertOne(doc)

    imageM
  }

  /**
   * Изменить размер и конвертировать изображение
   */
  private fun resizeAndConvert(
    image: BufferedImage,
    newWidth: Int,
    newHeight: Int,
    mimeType: String
  ): ByteArray {
    val resized = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
    val graphics = resized.createGraphics()
    graphics.drawImage(image, 0, 0, newWidth, newHeight, null)
    graphics.dispose()

    val outputStream = ByteArrayOutputStream()
    val format = when (mimeType) {
      "image/jpeg", "image/jpg" -> "jpg"
      "image/png" -> "png"
      "image/webp" -> "webp"
      else -> "jpg"
    }
    ImageIO.write(resized, format, outputStream)
    return outputStream.toByteArray()
  }

  /**
   * Получить изображение с данными
   */
  suspend fun getImageWithData(imageId: UUID): Pair<ImageM, InputStream>? = withContext(Dispatchers.IO) {
    // Получаем метаданные
    val doc = imagesCollection.find(Document("_id", imageId.toString())).firstOrNull()
      ?: return@withContext null

    if (doc.getBoolean("isActive") != true) {
      return@withContext null
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
      imageType = ImageType.valueOf(doc.getString("imageType") ?: "OTHER"),
      relatedEntityId = doc.getString("relatedEntityId")?.let { UUID.fromString(it) },
      uploadedAt = kotlinx.datetime.Instant.parse(doc.getString("uploadedAt") ?: ""),
      isActive = doc.getBoolean("isActive")
    )

    // Получаем файл из GridFS
    val gridFsFile = gridFsBucket.find(Document("_id", ObjectId(imageM.gridFsId))).firstOrNull()
      ?: return@withContext null

    val inputStream = gridFsBucket.openDownloadStream(gridFsFile.objectId)

    return@withContext Pair(imageM, inputStream)
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
      imageType = ImageType.valueOf(doc.getString("imageType") ?: "OTHER"),
      relatedEntityId = doc.getString("relatedEntityId")?.let { UUID.fromString(it) },
      uploadedAt = kotlinx.datetime.Instant.parse(doc.getString("uploadedAt") ?: ""),
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

    return imagesCollection.find(filter).toList().map { doc ->
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
    }
  }

}
