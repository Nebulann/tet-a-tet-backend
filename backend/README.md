
# 🚀 Тет-а-тет Backend API

Ktor-based REST API для приложения знакомств "Тет-а-тет" с поддержкой MongoDB, аутентификации и push-уведомлений.

## 📋 Требования

- **Java 17+**
- **Docker & Docker Compose**
- **MongoDB** (запускается через Docker)
- **Firebase Service Account** (для push-уведомлений)

## 🛠️ Быстрый запуск

### 1. Клонирование репозитория
```bash
git clone https://github.com/Nebulann/tet-a-tet-backend.git
cd tet-a-tet-backend
```

### 2. Настройка окружения

Создайте файл `.env` в корне проекта:
```env
# База данных
MONGO_HOSTNAME=kupidon-mongo
MONGO_PORT=27017
MONGO_RS=rs0
MONGO_ROOT_USERNAME=admin
MONGO_ROOT_PASSWORD=strong_password
MONGO_APP_DB_DATABASE=kupidon
MONGO_APP_DB_USERNAME=kupidon_user
MONGO_APP_DB_PASSWORD=kupidon_password

# Сервер
KTOR_PORT=8080
APP_MODE=development

# Аутентификация
AUTH_REFRESH_TOKEN_SECRET=your-refresh-secret-key
AUTH_ACCESS_TOKEN_SECRET=your-access-secret-key

# Firebase (для push-уведомлений)
FIREBASE_SERVER_KEY=your-server-key
FIREBASE_PROJECT_ID=tet-a-tet-e98c8
```

### 3. Сборка и запуск

```bash
# Сборка проекта
./gradlew build

# Запуск через Docker Compose
docker compose -f docker-compose-local.yml up -d

# Или запуск локально
./gradlew run
```

### 4. Проверка работы

- **API Health:** http://localhost:8080/actuator/health
- **Swagger документация:** http://localhost:8080/swagger-ui/index.html

## 🔧 Разработка

### Структура проекта
```
src/main/kotlin/com/rrain/kupidon/
├── controllers/        # REST контроллеры
├── models/            # Модели данных
├── routes/            # Маршрутизация
├── services/          # Бизнес-логика
├── plugins/           # Ktor плагины
└── utils/             # Утилиты
```

### Основные API endpoints

#### Аутентификация
- `POST /api/v1/auth/login` - Вход в систему
- `POST /api/v1/auth/refresh` - Обновление токенов
- `POST /api/v1/auth/logout` - Выход

#### Пользователи
- `GET /api/v1/users/{id}` - Получить пользователя
- `PUT /api/v1/users/{id}` - Обновить профиль
- `GET /api/v1/users` - Поиск пользователей

#### События
- `GET /api/v1/events` - Список событий
- `POST /api/v1/events` - Создать событие
- `PUT /api/v1/events/{id}` - Обновить событие
- `DELETE /api/v1/events/{id}` - Удалить событие

#### Идеи для свиданий
- `GET /api/v1/date-ideas` - Список идей
- `POST /api/v1/date-ideas` - Создать идею
- `PUT /api/v1/date-ideas/{id}` - Обновить идею

#### Изображения
- `POST /api/v1/upload/image` - Загрузить изображение
- `GET /api/v1/images/{id}` - Получить изображение
- `DELETE /api/v1/images/{id}` - Удалить изображение

#### Push-уведомления
- `POST /api/v1/notifications/send` - Отправить уведомление

## 🔐 Аутентификация

API использует JWT токены:
- `Authorization: Bearer <access_token>` - для доступа к защищенным ресурсам

## 📦 Сборка и деплой

### Production сборка
```bash
./gradlew build -x test
```

### Docker деплой
```bash
docker compose -f docker-compose-prod.yml up -d
```

## 🧪 Тестирование

```bash
# Запуск тестов
./gradlew test

# Тестирование конкретного модуля
./gradlew :module:test
```

## 📚 Документация

- **OpenAPI/Swagger:** http://localhost:8080/swagger-ui/index.html
- **Архитектура:** [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- **API документация:** [docs/API.md](docs/API.md)

## 🤝 Команда

- **Backend:** Kotlin + Ktor + MongoDB
- **Frontend:** React + TypeScript + Vite
- **База данных:** MongoDB с репликацией
- **Аутентификация:** JWT токены
- **Уведомления:** Firebase Cloud Messaging

## 📄 Лицензия

MIT License - см. [LICENSE](LICENSE) файл.

---

**Статус:** ✅ Готов к разработке и деплою
