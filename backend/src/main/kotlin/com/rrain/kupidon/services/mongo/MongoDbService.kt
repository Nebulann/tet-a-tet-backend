package com.rrain.kupidon.services.mongo

import com.mongodb.*
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.rrain.kupidon.models.db.ChatMessageM
import com.rrain.kupidon.models.db.ChatM
import com.rrain.kupidon.models.db.DateIdeaLikeM
import com.rrain.kupidon.models.db.DateIdeaM
import com.rrain.kupidon.models.db.EventCommentM
import com.rrain.kupidon.models.db.EventM
import com.rrain.kupidon.models.db.EventReactionM
import com.rrain.kupidon.models.db.UserM
import com.rrain.kupidon.models.db.UserToUserLikeM
import com.rrain.kupidon.services.env.Env
import com.rrain.utils.bson.appBsonCodecRegistry
import io.ktor.http.*
import io.ktor.server.application.*
import org.bson.UuidRepresentation
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*




fun Application.configureMongoDbService() {
  
  val appName = Env.dbConnectionApplicationName
  val host = Env.dbConnectionMongoHost
  val port = Env.dbConnectionMongoPort
  val rs = Env.dbConnectionMongoRs
  val database = Env.dbConnectionMongoDatabase
  val username = Env.dbConnectionMongoUsername
  val password = Env.dbConnectionMongoPassword
  val backendClientCert = Env.dbConnectionMongoBackendClientCert
  val caCert = Env.dbConnectionMongoCaCert
  
  
  // Проверяем, нужно ли использовать SSL с сертификатами
  val useSslCerts = backendClientCert.isNotEmpty() && caCert.isNotEmpty()
  val useCredentials = username != null && password != null
  
  val connectionString = when {
    useSslCerts -> {
      URLBuilder(
        $$"mongodb://$$host:$$port/" +
          $$"?replicaSet=$$rs&tls=true&authSource=$external&authMechanism=MONGODB-X509"
      ).buildString()
    }
    useCredentials -> {
      // Для MongoDB Atlas с username/password
      val rsParam = if (rs.isNotEmpty()) "replicaSet=$rs&" else ""
      $$"mongodb+srv://$$username:$$password@$$host/$$database?$${rsParam}retryWrites=true&w=majority"
    }
    else -> {
      // Без аутентификации (локальная разработка)
      val rsParam = if (rs.isNotEmpty()) "?replicaSet=$rs" else ""
      $$"mongodb://$$host:$$port/$$database$$rsParam"
    }
  }
  
  
  // https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/fundamentals/connection/connection-options/
  val connectionSettings = MongoClientSettings.builder()
    .applicationName(appName)
    .applyConnectionString(ConnectionString(connectionString))
    .apply {
      if (useSslCerts) {
        applyToSslSettings { builder ->
          builder.enabled(true)
          builder.context(getMongoSslContext(backendClientCert, caCert))
        }
      }
    }
    .uuidRepresentation(UuidRepresentation.STANDARD)
    .codecRegistry(appBsonCodecRegistry)
    .build()
  
  // Reuse Your Client
  // As each MongoClient represents a thread-safe pool of connections to the database,
  // most applications only require a single instance of a MongoClient,
  // even across multiple threads.
  val mongoClient = MongoClient.create(connectionSettings)
  
  MongoDbService.config = MongoDbService.Config(
    client = mongoClient,
    dbName = database,
  )
  
}





fun getMongoSslContext(backendClientCert: String, caCert: String) = run {
  val pkcs12CertPwd = "0000".toCharArray()
  val keyStore = KeyStore.getInstance("PKCS12").apply {
    FileInputStream(backendClientCert).use {
      load(it, pkcs12CertPwd)
    }
  }
  val keyManagerFactory = KeyManagerFactory
    .getInstance(KeyManagerFactory.getDefaultAlgorithm())
    .apply {
      init(keyStore, pkcs12CertPwd)
    }
  
  
  
  val caCert = CertificateFactory.getInstance("X.509")
    .generateCertificate(FileInputStream(File(caCert))) as X509Certificate
  
  // Create a custom TrustManager that trusts this certificate
  val trustManagers = arrayOf<TrustManager>(object: X509TrustManager {
    override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOf(caCert)
    override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) { }
    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String?) {
      // Optionally validate the server certificate here
      if (certs.any { it == caCert }) return
      throw SecurityException("Server certificate not trusted")
    }
  })
  
  
  
  // Initialize SSLContext
  val sslContext = SSLContext.getInstance("TLS").apply {
    init(keyManagerFactory.keyManagers, trustManagers, SecureRandom())
  }
  
  sslContext
}




object MongoDbService {
  
  data class Config(
    val client: MongoClient,
    val dbName: String,
  )
  
  lateinit var config: Config
  
  val client get() = config.client
  val appDbName get() = config.dbName
}

val mongo get() = MongoDbService.client

fun MongoClient.db(dbName: String) = this.getDatabase(dbName)
val MongoClient.appDb get() = this.getDatabase(MongoDbService.appDbName)

inline fun <reified T : Any> MongoDatabase.coll(collName : String) = (
  this.getCollection<T>(collName)
)


fun mongoDb(dbName: String) = mongo.db(dbName)
val mongoAppDb get() = mongo.appDb

object CollNames {
  val users = "users"
  val userToUserLikes = "userToUserLikes"
  val chats = "chats"
  val chatMessages = "chatMessages"
  val dateIdeas = "dateIdeas"
  val dateIdeaLikes = "dateIdeaLikes"
  val events = "events"
  val eventReactions = "eventReactions"
  val eventComments = "eventComments"
}

inline fun <reified T : Any> collUsers() = mongoAppDb.coll<T>(CollNames.users)
inline fun <reified T : Any> collUserToUserLikes() = mongoAppDb.coll<T>(CollNames.userToUserLikes)
inline fun <reified T : Any> collChats() = mongoAppDb.coll<T>(CollNames.chats)
inline fun <reified T : Any> collChatMessages() = mongoAppDb.coll<T>(CollNames.chatMessages)
inline fun <reified T : Any> collDateIdeas() = mongoAppDb.coll<T>(CollNames.dateIdeas)
inline fun <reified T : Any> collDateIdeaLikes() = mongoAppDb.coll<T>(CollNames.dateIdeaLikes)
inline fun <reified T : Any> collEvents() = mongoAppDb.coll<T>(CollNames.events)
inline fun <reified T : Any> collEventReactions() = mongoAppDb.coll<T>(CollNames.eventReactions)
inline fun <reified T : Any> collEventComments() = mongoAppDb.coll<T>(CollNames.eventComments)

val collUsers get() = collUsers<UserM>()
val collUserToUserLikes get() = collUserToUserLikes<UserToUserLikeM>()
val collChats get() = collChats<ChatM>()
val collChatMessages get() = collChatMessages<ChatMessageM>()
val collDateIdeas get() = collDateIdeas<DateIdeaM>()
val collDateIdeaLikes get() = collDateIdeaLikes<DateIdeaLikeM>()
val collEvents get() = collEvents<EventM>()
val collEventReactions get() = collEventReactions<EventReactionM>()
val collEventComments get() = collEventComments<EventCommentM>()

