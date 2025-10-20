# Решение проблем деплоя

## Проблема: GET /api/v1/events возвращает 401

### Симптомы:
```
events:1 Failed to load resource: the server responded with a status of 401 ()
GET https://tet-a-tet-backend.onrender.com/api/v1/events 401
Response: {"code": "NO_AUTHORIZATION_HEADER", "msg": "No \"Authorization\" header is present"}
```

### Причина:
Эндпоинт `/api/v1/events` требует авторизацию, но должен быть публичным.

### Решение:
1. ✅ **Код исправлен** - коммит `53c468c0` и `da2657bb` запушены в GitHub
2. ⏳ **Ожидание деплоя на Render** - занимает ~5-10 минут после push

### Как проверить статус деплоя:

#### Вариант 1: Через Render Dashboard
1. Откройте https://dashboard.render.com/
2. Найдите сервис `tet-a-tet-backend`
3. Проверьте статус последнего деплоя
4. Дождитесь статуса "Live"

#### Вариант 2: Через API
```bash
# Проверить, что эндпоинт стал публичным
curl https://tet-a-tet-backend.onrender.com/api/v1/events

# Должен вернуть JSON с событиями или пустой массив:
# {"events":[],"pagination":{"page":0,"limit":20,"total":0,"hasNext":false}}

# Если все еще возвращает 401, деплой еще не завершен
```

#### Вариант 3: Через браузер
Откройте в браузере: https://tet-a-tet-backend.onrender.com/api/v1/events

**Ожидаемый результат:**
```json
{
  "events": [],
  "pagination": {
    "page": 0,
    "limit": 20,
    "total": 0,
    "hasNext": false
  }
}
```

**Если видите ошибку 401:**
```json
{
  "code": "NO_AUTHORIZATION_HEADER",
  "msg": "No \"Authorization\" header is present"
}
```
Значит деплой еще не завершен. Подождите еще 2-3 минуты.

---

## Проблема: GET /api/v1/auth/refresh-tokens 400

### Симптомы:
```
GET https://tet-a-tet-backend.onrender.com/api/v1/auth/refresh-tokens 400 (Bad Request)
Response: {"code": "NO_REFRESH_TOKEN_COOKIE", "msg": "No refresh token cookie"}
```

### Причина:
Это **нормальное поведение** для неавторизованного пользователя. Axios пытается автоматически обновить токен при получении 401, но у пользователя нет refresh токена.

### Решение:
Не требуется. После того как эндпоинт `/api/v1/events` станет публичным, эта ошибка исчезнет, так как 401 больше не будет возвращаться.

---

## Проблема: @import rules are not allowed

### Симптомы:
```
@import rules are not allowed here. See https://github.com/WICG/construct-stylesheets/issues/119
```

### Причина:
Предупреждение от браузера о CSS @import в constructable stylesheets.

### Решение:
Это предупреждение, не ошибка. Можно игнорировать или исправить позже, переместив @import в обычные стили.

---

## Проблема: feed:1 Failed to load resource: 404

### Симптомы:
```
feed:1 Failed to load resource: the server responded with a status of 404 ()
```

### Причина:
Браузер пытается загрузить `/feed` как ресурс, но это SPA роут.

### Решение:
Не требуется. Это нормальное поведение для SPA. Vercel правильно обрабатывает роутинг.

---

## Таймлайн деплоя на Render

После `git push origin main`:

1. **0-2 минуты** - Render обнаруживает изменения в GitHub
2. **2-5 минут** - Сборка Docker образа
3. **5-7 минут** - Деплой нового образа
4. **7-10 минут** - Сервис становится доступен

**Общее время:** ~5-10 минут

### Как ускорить проверку:
```bash
# Запустите эту команду каждые 30 секунд
while true; do
  echo "Checking at $(date '+%H:%M:%S')..."
  curl -s https://tet-a-tet-backend.onrender.com/api/v1/events | jq .
  sleep 30
done
```

Когда увидите JSON с `events` и `pagination`, деплой завершен!

---

## Принудительный редеплой

Если изменения не деплоятся автоматически:

```bash
cd backend
git commit --allow-empty -m "trigger: принудительный редеплой"
git push origin main
```

Это создаст пустой коммит, который триггернет деплой на Render.

---

## Проверка после успешного деплоя

### 1. Проверить бэкенд
```bash
curl https://tet-a-tet-backend.onrender.com/api/v1/events
```

### 2. Проверить фронтенд
Откройте https://tet-a-tet-frontend.vercel.app/feed

**Ожидаемый результат:**
- Страница загружается
- Показываются события (реальные или демо-данные)
- Нет ошибок 401 в консоли

### 3. Проверить консоль браузера
Откройте DevTools (F12) → Console

**Не должно быть:**
- ❌ `events:1 Failed to load resource: 401`
- ❌ `GET .../api/v1/events 401`

**Может быть (это нормально):**
- ⚠️ `@import rules are not allowed` - предупреждение CSS
- ⚠️ `feed:1 Failed to load resource: 404` - SPA роутинг
- ℹ️ `API недоступен, используем моковые данные` - если база пустая

---

## Добавление тестовых данных

Если база данных пустая, добавьте тестовое событие:

```bash
# Сначала получите admin токен (нужно залогиниться как админ)
# Затем создайте событие:

curl -X POST https://tet-a-tet-backend.onrender.com/api/v1/events \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Концерт в парке",
    "description": "Живая музыка под открытым небом",
    "category": "CULTURAL",
    "startDate": "2025-10-25T18:00:00Z",
    "location": "Центральный парк",
    "tags": ["музыка", "концерт"]
  }'
```

После этого события появятся на странице `/feed`.

---

## Контакты для поддержки

- **Backend Repo:** https://github.com/Nebulann/tet-a-tet-backend
- **Frontend Repo:** https://github.com/Nebulann/tet-a-tet-frontend
- **Render Dashboard:** https://dashboard.render.com/
- **Vercel Dashboard:** https://vercel.com/dashboard

**Последнее обновление:** 20 октября 2025, 17:48
