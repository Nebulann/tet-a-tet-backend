package com.rrain.kupidon.plugins

import com.rrain.kupidon.routes.routes.http.`app-api-v1`.auth.addAuthOtherRoutes
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.`chat-item`.addRouteGetChatItemIdId
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.`chat-item`.addRouteGetChatItemToUserIdId
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.`chat-message`.addRoutePostChatMessageToChatIdId
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.`chat-message`.addRoutePostChatMessageToUserIdId
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.`chat-messages`.addRouteGetChatMessages
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.`chat-items`.addRouteGetChatItems
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.users.addRouteGetUsersNewPairs
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.`user-to-user`.addRoutePostUserToUserLike
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.user.addRouteGetUserTypeAcquaintanceShortIdId
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.user.addUserCreateRoute
import com.rrain.kupidon.routes.routes.http.main.addMainRoutes
import com.rrain.kupidon.routes.routes.http.`app-pwa-manifest`.addPwaManifestRoute
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.user.addUserCurrentRoute
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.user.addUserEmailInitialVerifyRoute
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.user.addUserIdIdRoute
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.user.addUserUpdateRoute
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.user.addUserProfilePhotoPostRoute
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.user.addUserProfilePhotoGetRoute
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.users.addRouteGetUsers
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.auth.addRoutePostAuthLogin
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.auth.addRoutePostAuthLoginTestUser
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.auth.addAuthRefreshTokensRoute
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.`date-ideas`.addDateIdeasRoutes
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.events.addEventsRoutes
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.settings.addSettingsNotificationsRoutes
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.settings.addSettingsPrivacyRoutes
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.images.addImagesRoutes
import com.rrain.kupidon.routes.routes.http.test.addAuthorizationTestRoutes
import com.rrain.kupidon.routes.routes.http.test.addHttpTestRoutes
import com.rrain.kupidon.routes.routes.http.test.addImgTestRoutes
import com.rrain.kupidon.routes.routes.http.test.addJsonSerializationTestRoutes
import com.rrain.kupidon.routes.routes.http.test.addMongoTestRoutes
import com.rrain.kupidon.routes.routes.http.test.addSendEmailTestRoutes
import com.rrain.kupidon.routes.routes.http.test.addSeedDataRoutes
import io.ktor.server.application.*




fun Application.configureHttpRouting() {
  
  addMainRoutes() // TODO
  
  addAuthorizationTestRoutes() // TODO
  addHttpTestRoutes() // TODO
  addImgTestRoutes() // TODO
  addJsonSerializationTestRoutes() // TODO
  addMongoTestRoutes() // TODO
  addSendEmailTestRoutes() // TODO
  addSeedDataRoutes() // TODO
  
  
  addPwaManifestRoute() // TODO
  
  addRoutePostAuthLogin()
  addRoutePostAuthLoginTestUser()
  addAuthRefreshTokensRoute() // TODO
  addAuthOtherRoutes() // TODO
  
  addUserCurrentRoute() // TODO
  addUserCreateRoute() // TODO
  addUserUpdateRoute() // TODO
  addUserIdIdRoute() // TODO
  addRouteGetUserTypeAcquaintanceShortIdId() // TODO
  addUserProfilePhotoPostRoute() // TODO
  addUserProfilePhotoGetRoute() // TODO
  addUserEmailInitialVerifyRoute() // TODO
  
  addRoutePostUserToUserLike()
  
  addRouteGetUsers()
  addRouteGetUsersNewPairs()
  
  addRouteGetChatItemIdId()
  addRouteGetChatItemToUserIdId()
  addRouteGetChatItems()
  
  addRoutePostChatMessageToUserIdId()
  addRoutePostChatMessageToChatIdId()
  
  addRouteGetChatMessages()
  
  // Settings routes
  addSettingsPrivacyRoutes()
  addSettingsNotificationsRoutes()
  
  // Date ideas routes
  addDateIdeasRoutes()
  
  // Events routes
  addEventsRoutes()
  
  // Images routes
  addImagesRoutes()
  
}


