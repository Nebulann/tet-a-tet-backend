package com.rrain.kupidon

import io.ktor.server.application.*
import com.rrain.kupidon.plugins.*
import com.rrain.kupidon.plugins.configureJsonSerialization
import com.rrain.kupidon.plugins.configureStatusPages
import com.rrain.kupidon.plugins.configureWebSocketRouting
import com.rrain.kupidon.services.email.configureEmailService
import com.rrain.kupidon.services.jwt.configureJwtService
import com.rrain.kupidon.services.`pwd-hash`.configurePwdHashService
import com.rrain.kupidon.services.mongo.configureMongoDbService
import com.rrain.kupidon.services.env.Env
import com.rrain.kupidon.services.push.PushNotificationService



fun main(args: Array<String>) {
  io.ktor.server.jetty.jakarta.EngineMain.main(args)
}



// application.conf references this function.
fun Application.module() {
  configureLogging()
  configureJsonSerialization()
  
  configureJwtService()
  configurePwdHashService()
  configureEmailService()
  
  configureMongoDbService()
  
  configureCors()
  configureHttpCachingHeaders()
  configureHttpForwardedHeaders()
  configureHttpAutoHeadResponse()
  
  // Initialize Firebase Admin for FCM
  kotlin.run {
    try {
      val svc = Env.firebaseServiceAccountPath
      PushNotificationService.initialize(svc)
    }
    catch (ex: Exception) {
      println("FCM init failed: ${ex.message}")
      ex.printStackTrace()
    }
  }

  configureJwtAuthentication()
  configureStatusPages()
  configureRouting()
  configureHttpRouting()
  configurePushServerRouting()
  configureWebSocketRouting()
}
