package com.rrain.kupidon.services.email

import com.rrain.kupidon.services.env.Env
import io.ktor.server.application.*
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.HtmlEmail
import org.apache.commons.mail.SimpleEmail
import java.nio.charset.StandardCharsets



fun Application.configureEmailService() {
  
  val email = Env.mailEmail
  val pwd = Env.mailPwd
  
  if (email != null && pwd != null) {
    EmailService.config = EmailService.Config(
      fromEmail = email,
      fromPwd = pwd,
    )
  } else {
    log.warn("Email service not configured: MAIL_EMAIL or MAIL_PWD not set")
  }
  
}



object EmailService {
  
  data class Config(
    val fromEmail: String,
    val fromPwd: String,
  )
  
  lateinit var config: Config
  
  
  // https://commons.apache.org/proper/commons-email/userguide.html
  
  
  fun sendEmail(emailMessage: EmailMessage) {
    SimpleEmail().run {
      hostName = "smtp.yandex.ru"
      setSmtpPort(465)
      setAuthenticator(DefaultAuthenticator(config.fromEmail, config.fromPwd))
      isSSLOnConnect = true
      setFrom(config.fromEmail)
      addTo(emailMessage.to)
      subject = emailMessage.title
      setMsg(emailMessage.content)
      send()
    }
  }
  
  
  fun sendHtmlEmail(emailMessage: EmailMessage) {
    HtmlEmail().run {
      hostName = "smtp.yandex.ru"
      setSmtpPort(465)
      setAuthenticator(DefaultAuthenticator(config.fromEmail, config.fromPwd))
      isSSLOnConnect = true
      setFrom(config.fromEmail, emailMessage.fromName)
      addTo(emailMessage.to)
      subject = emailMessage.title
      setCharset(StandardCharsets.UTF_8.name())
      setHtmlMsg(emailMessage.content)
      send()
    }
  }
  
}

data class EmailMessage(
  val fromName: String, // Отображаемое имя рядом с почтой отправителя
  val to: String, // получатель
  val title: String, // тема письма
  val content: String, // тело письма
)