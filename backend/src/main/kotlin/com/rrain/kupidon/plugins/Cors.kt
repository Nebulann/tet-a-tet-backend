package com.rrain.kupidon.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*



fun Application.configureCors() {
  install(CORS) {
    allowCredentials = true

    // Methods
    allowMethod(HttpMethod.Get)
    allowMethod(HttpMethod.Post)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Options)

    // Headers
    allowHeader(HttpHeaders.Authorization)
    allowHeader(HttpHeaders.ContentType)
    allowHeader(HttpHeaders.Accept)
    allowHeader(HttpHeaders.Origin)
    allowHeader(HttpHeaders.AccessControlRequestHeaders)
    allowHeader(HttpHeaders.AccessControlRequestMethod)
    allowHeader(HttpHeaders.AccessControlAllowOrigin)

    // Content types
    allowNonSimpleContentTypes = true

    // NOTE: For development we allow any host; tighten in production via env
    anyHost()
  }
}


