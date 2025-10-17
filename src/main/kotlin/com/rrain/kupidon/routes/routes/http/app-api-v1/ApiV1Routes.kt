package com.rrain.kupidon.routes.routes.http.`app-api-v1`



object ApiV1Routes {
  val pwaManifest = "/manifest.json"
  
  
  val apiV1 = "/api/v1"
  
  val auth = "$apiV1/auth"
  val authLogin = "$auth/login"
  val authLoginTestUser = "$authLogin/test-user"
  val authRefreshTokens = "$auth/refresh-tokens"
  
  val user = "$apiV1/user"
  val userCurrent = "$user/current"
  val userIdId = "$user/id/{id}"
  val userTypeAcquaintanceShortIdId = "$user/type/acquaintance-short/id/{id}"
  val userProfilePhoto = "$user/profile-photo"
  val userProfilePhotoName = "$user/profile-photo/*"
  object userProfilePhotoNameParams {
    val userId = "userId"
    val photoId = "photoId"
  }
  val userVerificationEmailInitial = "$user/verification/email/initial"
  object userVerificationEmailInitialParams {
    val verificationToken = "verification-token"
  }
  
  val users = "$apiV1/users"
  val usersNewPairs = "$users/new-pairs"
  
  val userToUser = "$apiV1/user-to-user"
  val userToUserLike = "$userToUser/like"
  
  val chatItemIdId = "$apiV1/chat-item/id/{id}"
  val chatItemToUserIdId = "$apiV1/chat-item/to-user-id/{id}"
  
  val chatItems = "$apiV1/chat-items"
  
  val chatMessage = "$apiV1/chat-message"
  val chatMessageToUserIdId = "$chatMessage/to-user-id/{id}"
  val chatMessageToChatIdId = "$chatMessage/to-chat-id/{id}"
  
  val chatMessages = "$apiV1/chat-messages"
  
  val settings = "$apiV1/settings"
  
  val dateIdeas = "$apiV1/date-ideas"
  val dateIdeaIdId = "$dateIdeas/id/{id}"
  
  val events = "$apiV1/events"
  val eventIdId = "$events/id/{id}"
  
  val upload = "$apiV1/upload"
  val uploadImage = "$upload/image"
  
  val images = "$apiV1/images"
  val imagesIdId = "$images/{id}"
  val imagesIdMetadata = "$images/{id}/metadata"
  
  val userImages = "$user/images"
}


