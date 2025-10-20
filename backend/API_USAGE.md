# Документация API для платформы "Тет-а-тет"

## Базовый URL

**Production:** `https://tet-a-tet-backend.onrender.com`  
**Local:** `http://localhost:8080`

## Заполнение базы тестовыми данными

### 1. Заполнить базу данных

```http
GET /test/seed/all
```

**Описание:** Создает тестовых пользователей и события в базе данных.

**Ответ:**
```json
{
  "message": "База данных успешно заполнена тестовыми данными",
  "users": {
    "inserted": 5,
    "emails": [
      "alice@test.com",
      "bob@test.com",
      "charlie@test.com",
      "diana@test.com",
      "eve@test.com"
    ]
  },
  "events": {
    "inserted": 8,
    "titles": [
      "Вечеринка в стиле 90-х",
      "Забег в парке Горького",
      "..."
    ]
  }
}
```

**Тестовые пользователи:**
- Email: `alice@test.com`, Пароль: `password123`
- Email: `bob@test.com`, Пароль: `password123`
- Email: `charlie@test.com`, Пароль: `password123`
- Email: `diana@test.com`, Пароль: `password123`
- Email: `eve@test.com`, Пароль: `password123`

### 2. Очистить тестовые данные

```http
GET /test/seed/clear
```

**Описание:** Удаляет все тестовые данные из базы.

---

## Аутентификация

### 1. Вход в систему

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "alice@test.com",
  "password": "password123"
}
```

**Ответ:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "email": "alice@test.com",
    "name": "Алиса",
    "..."
  }
}
```

### 2. Обновление токенов

```http
POST /api/v1/auth/refresh-tokens
Content-Type: application/json

{
  "refreshToken": "your-refresh-token"
}
```

---

## События (Events)

### 1. Получить список событий

```http
GET /api/v1/events?page=0&limit=20&upcoming=true
```

**Query параметры:**
- `page` (optional, default: 0) - номер страницы
- `limit` (optional, default: 20, max: 100) - количество событий на странице
- `category` (optional) - фильтр по категории (MEETUP, PARTY, SPORT, CULTURAL, OUTDOOR, WORKSHOP, DATING, OTHER)
- `search` (optional) - поиск по названию, описанию или тегам
- `upcoming` (optional, default: true) - только предстоящие события

**Ответ:**
```json
{
  "events": [
    {
      "id": "uuid",
      "creatorId": "uuid",
      "title": "Вечеринка в стиле 90-х",
      "description": "Ностальгическая вечеринка...",
      "category": "PARTY",
      "startDate": "2025-10-27T00:00:00Z",
      "endDate": "2025-10-31T00:00:00Z",
      "location": "Клуб Retrowave, ул. Тверская, 15",
      "coordinates": {
        "lat": 55.7558,
        "lng": 37.6173
      },
      "imageUrl": "https://...",
      "maxParticipants": 50,
      "participantsCount": 2,
      "isParticipating": false,
      "tags": ["вечеринка", "90-е", "ретро", "музыка"],
      "likesCount": 15,
      "fireCount": 8,
      "createdAt": "2025-10-20T00:00:00Z",
      "updatedAt": "2025-10-20T00:00:00Z",
      "isPinned": true
    }
  ],
  "pagination": {
    "page": 0,
    "limit": 20,
    "total": 8,
    "hasNext": false
  }
}
```

### 2. Создать событие (требуется авторизация)

```http
POST /api/v1/events
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "title": "Название события",
  "description": "Описание события",
  "category": "MEETUP",
  "startDate": "2025-11-01T18:00:00Z",
  "endDate": "2025-11-01T22:00:00Z",
  "location": "Адрес места проведения",
  "coordinates": {
    "lat": 55.7558,
    "lng": 37.6173
  },
  "imageUrl": "https://example.com/image.jpg",
  "maxParticipants": 30,
  "tags": ["тег1", "тег2"]
}
```

**Обязательные поля:**
- `title` - название события
- `description` - описание
- `category` - категория
- `startDate` - дата начала (ISO 8601)

**Опциональные поля:**
- `endDate` - дата окончания
- `location` - место проведения
- `coordinates` - координаты
- `imageUrl` - URL изображения
- `maxParticipants` - максимальное количество участников
- `tags` - теги

### 3. Получить событие по ID (требуется авторизация)

```http
GET /api/v1/events/id/{eventId}
Authorization: Bearer <access-token>
```

### 4. Записаться на событие (требуется авторизация)

```http
POST /api/v1/events/id/{eventId}/attend
Authorization: Bearer <access-token>
```

**Ответ:**
```json
{
  "message": "Successfully registered for event"
}
```

### 5. Отписаться от события (требуется авторизация)

```http
DELETE /api/v1/events/id/{eventId}/attend
Authorization: Bearer <access-token>
```

### 6. Поставить реакцию (требуется авторизация)

```http
POST /api/v1/events/id/{eventId}/react
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "type": "LIKE"
}
```

**Типы реакций:**
- `LIKE` - лайк
- `FIRE` - огонь

---

## Категории событий

- `MEETUP` - Встреча
- `PARTY` - Вечеринка
- `SPORT` - Спорт
- `CULTURAL` - Культурное мероприятие
- `OUTDOOR` - На природе
- `WORKSHOP` - Мастер-класс
- `DATING` - Свидание
- `OTHER` - Другое

---

## Коды ошибок

- `200` - Успешно
- `400` - Неверный запрос
- `401` - Не авторизован
- `404` - Не найдено
- `500` - Внутренняя ошибка сервера

---

## Примеры использования

### Curl

```bash
# Заполнить базу тестовыми данными
curl https://tet-a-tet-backend.onrender.com/test/seed/all

# Войти в систему
curl -X POST https://tet-a-tet-backend.onrender.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@test.com","password":"password123"}'

# Получить список событий
curl https://tet-a-tet-backend.onrender.com/api/v1/events?limit=10

# Записаться на событие
curl -X POST https://tet-a-tet-backend.onrender.com/api/v1/events/id/{eventId}/attend \
  -H "Authorization: Bearer <your-access-token>"
```

### JavaScript (Fetch)

```javascript
// Заполнить базу тестовыми данными
fetch('https://tet-a-tet-backend.onrender.com/test/seed/all')
  .then(res => res.json())
  .then(data => console.log(data));

// Войти в систему
fetch('https://tet-a-tet-backend.onrender.com/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'alice@test.com',
    password: 'password123'
  })
})
  .then(res => res.json())
  .then(data => {
    const accessToken = data.accessToken;
    console.log('Access Token:', accessToken);
  });

// Получить список событий
fetch('https://tet-a-tet-backend.onrender.com/api/v1/events?limit=10')
  .then(res => res.json())
  .then(data => console.log(data.events));

// Записаться на событие
fetch(`https://tet-a-tet-backend.onrender.com/api/v1/events/id/${eventId}/attend`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
})
  .then(res => res.json())
  .then(data => console.log(data));
```

---

## Быстрый старт

1. **Заполните базу тестовыми данными:**
   ```
   GET https://tet-a-tet-backend.onrender.com/test/seed/all
   ```

2. **Войдите в систему:**
   ```
   POST https://tet-a-tet-backend.onrender.com/api/v1/auth/login
   Body: {"email":"alice@test.com","password":"password123"}
   ```

3. **Получите список событий:**
   ```
   GET https://tet-a-tet-backend.onrender.com/api/v1/events
   ```

4. **Используйте полученный `accessToken` для защищенных эндпоинтов**

---

## Примечания

- Все даты в формате ISO 8601 (например: `2025-10-20T14:30:00Z`)
- Все UUID в формате стандартного UUID v4
- Для защищенных эндпоинтов требуется JWT токен в заголовке `Authorization: Bearer <token>`
- Публичный эндпоинт `/api/v1/events` (GET) не требует авторизации
