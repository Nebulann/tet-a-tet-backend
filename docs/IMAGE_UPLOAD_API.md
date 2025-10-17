# API для загрузки изображений

## Обзор

API для загрузки, получения и управления изображениями через MongoDB GridFS.

## Endpoints

### 1. Загрузить изображение

**POST** `/api/v1/upload/image`

**Требуется авторизация:** Да (JWT токен)

**Content-Type:** `multipart/form-data`

**Параметры:**
- `file` (обязательный) - файл изображения
- `imageType` (опциональный) - тип изображения: `EVENT`, `DATE_IDEA`, `USER_PROFILE`, `CHAT_MESSAGE`, `OTHER` (по умолчанию: `OTHER`)
- `relatedEntityId` (опциональный) - UUID связанной сущности (события, идеи и т.д.)

**Ограничения:**
- Максимальный размер файла: 10 МБ
- Разрешенные форматы: JPEG, JPG, PNG, WebP, GIF
- Максимальные размеры: 2048x2048 px (автоматически изменяется при превышении)

**Пример запроса (curl):**
```bash
curl -X POST http://localhost:8080/api/v1/upload/image \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg" \
  -F "imageType=EVENT" \
  -F "relatedEntityId=123e4567-e89b-12d3-a456-426614174000"
```

**Пример ответа:**
```json
{
  "success": true,
  "message": "Изображение успешно загружено",
  "data": {
    "id": "987fcdeb-51a2-43d7-b890-123456789abc",
    "uploadedBy": "123e4567-e89b-12d3-a456-426614174000",
    "originalFileName": "image.jpg",
    "mimeType": "image/jpeg",
    "fileSize": 245678,
    "width": 1920,
    "height": 1080,
    "imageType": "EVENT",
    "relatedEntityId": "123e4567-e89b-12d3-a456-426614174000",
    "uploadedAt": "2025-10-14T10:30:00Z",
    "url": "http://localhost:8080/api/v1/images/987fcdeb-51a2-43d7-b890-123456789abc"
  }
}
```

---

### 2. Получить изображение

**GET** `/api/v1/images/{id}`

**Требуется авторизация:** Нет (публичный endpoint)

**Параметры:**
- `id` (path) - UUID изображения

**Пример запроса:**
```bash
curl http://localhost:8080/api/v1/images/987fcdeb-51a2-43d7-b890-123456789abc
```

**Ответ:** Бинарные данные изображения с соответствующим Content-Type

---

### 3. Получить метаданные изображения

**GET** `/api/v1/images/{id}/metadata`

**Требуется авторизация:** Да (JWT токен)

**Параметры:**
- `id` (path) - UUID изображения

**Пример запроса:**
```bash
curl http://localhost:8080/api/v1/images/987fcdeb-51a2-43d7-b890-123456789abc/metadata \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Пример ответа:**
```json
{
  "success": true,
  "data": {
    "id": "987fcdeb-51a2-43d7-b890-123456789abc",
    "uploadedBy": "123e4567-e89b-12d3-a456-426614174000",
    "originalFileName": "image.jpg",
    "mimeType": "image/jpeg",
    "fileSize": 245678,
    "width": 1920,
    "height": 1080,
    "imageType": "EVENT",
    "relatedEntityId": "123e4567-e89b-12d3-a456-426614174000",
    "uploadedAt": "2025-10-14T10:30:00Z",
    "url": "http://localhost:8080/api/v1/images/987fcdeb-51a2-43d7-b890-123456789abc"
  }
}
```

---

### 4. Удалить изображение

**DELETE** `/api/v1/images/{id}`

**Требуется авторизация:** Да (JWT токен)

**Параметры:**
- `id` (path) - UUID изображения

**Примечание:** Удаляет только изображения, загруженные текущим пользователем (soft delete)

**Пример запроса:**
```bash
curl -X DELETE http://localhost:8080/api/v1/images/987fcdeb-51a2-43d7-b890-123456789abc \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Пример ответа:**
```json
{
  "success": true,
  "message": "Изображение успешно удалено"
}
```

---

### 5. Получить все изображения пользователя

**GET** `/api/v1/user/images`

**Требуется авторизация:** Да (JWT токен)

**Query параметры:**
- `imageType` (опциональный) - фильтр по типу: `EVENT`, `DATE_IDEA`, `USER_PROFILE`, `CHAT_MESSAGE`, `OTHER`

**Пример запроса:**
```bash
curl http://localhost:8080/api/v1/user/images?imageType=EVENT \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Пример ответа:**
```json
{
  "success": true,
  "data": [
    {
      "id": "987fcdeb-51a2-43d7-b890-123456789abc",
      "uploadedBy": "123e4567-e89b-12d3-a456-426614174000",
      "originalFileName": "event1.jpg",
      "mimeType": "image/jpeg",
      "fileSize": 245678,
      "width": 1920,
      "height": 1080,
      "imageType": "EVENT",
      "relatedEntityId": "123e4567-e89b-12d3-a456-426614174000",
      "uploadedAt": "2025-10-14T10:30:00Z",
      "url": "http://localhost:8080/api/v1/images/987fcdeb-51a2-43d7-b890-123456789abc"
    },
    {
      "id": "456def78-90ab-cdef-1234-567890abcdef",
      "uploadedBy": "123e4567-e89b-12d3-a456-426614174000",
      "originalFileName": "event2.png",
      "mimeType": "image/png",
      "fileSize": 189234,
      "width": 1280,
      "height": 720,
      "imageType": "EVENT",
      "relatedEntityId": "789abc12-3def-4567-8901-234567890abc",
      "uploadedAt": "2025-10-14T11:00:00Z",
      "url": "http://localhost:8080/api/v1/images/456def78-90ab-cdef-1234-567890abcdef"
    }
  ]
}
```

---

## Типы изображений (ImageType)

- `EVENT` - Изображение события
- `DATE_IDEA` - Изображение идеи для свидания
- `USER_PROFILE` - Фото профиля пользователя
- `CHAT_MESSAGE` - Изображение в сообщении чата
- `OTHER` - Другое

---

## Обработка ошибок

### 400 Bad Request
```json
{
  "error": "Не указан файл изображения"
}
```

### 400 Bad Request (неподдерживаемый формат)
```json
{
  "error": "Недопустимый тип файла. Разрешены: image/jpeg, image/jpg, image/png, image/webp, image/gif"
}
```

### 400 Bad Request (превышен размер)
```json
{
  "error": "Размер файла превышает максимально допустимый (10 МБ)"
}
```

### 401 Unauthorized
```json
{
  "error": "Требуется авторизация"
}
```

### 403 Forbidden
```json
{
  "error": "У вас нет прав на удаление этого изображения"
}
```

### 404 Not Found
```json
{
  "error": "Изображение не найдено"
}
```

### 500 Internal Server Error
```json
{
  "error": "Ошибка при загрузке изображения"
}
```

---

## Особенности реализации

1. **MongoDB GridFS** - изображения хранятся в GridFS для эффективного управления большими файлами
2. **Автоматическая оптимизация** - изображения автоматически изменяются до максимального размера 2048x2048 px
3. **Soft delete** - удаленные изображения помечаются как неактивные, но не удаляются физически
4. **Валидация** - проверка формата, размера и MIME-типа файлов
5. **Метаданные** - хранение информации о размерах, типе и связанных сущностях

---

## Интеграция с событиями и идеями

### Загрузка изображения для события

```javascript
const formData = new FormData();
formData.append('file', imageFile);
formData.append('imageType', 'EVENT');
formData.append('relatedEntityId', eventId);

const response = await fetch('http://localhost:8080/api/v1/upload/image', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
});

const data = await response.json();
const imageUrl = data.data.url;

// Теперь обновите событие с imageUrl
```

### Загрузка изображения для идеи свидания

```javascript
const formData = new FormData();
formData.append('file', imageFile);
formData.append('imageType', 'DATE_IDEA');
formData.append('relatedEntityId', dateIdeaId);

const response = await fetch('http://localhost:8080/api/v1/upload/image', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
});

const data = await response.json();
const imageUrl = data.data.url;

// Теперь обновите идею с imageUrl
```
