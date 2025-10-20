# Быстрый старт - Бэкенд "Тет-а-тет"

## 🚀 Текущее состояние

Бэкенд **полностью готов** и развернут на Render.com:
- ✅ URL: `https://tet-a-tet-backend.onrender.com`
- ✅ База данных MongoDB настроена
- ✅ JWT аутентификация работает
- ✅ API для событий готово
- ✅ Тестовые данные можно загрузить одним запросом

## 📝 Шаг 1: Заполнить базу тестовыми данными

Откройте в браузере или выполните curl:

```
https://tet-a-tet-backend.onrender.com/test/seed/all
```

Это создаст:
- **5 тестовых пользователей** (alice, bob, charlie, diana, eve)
- **8 тестовых событий** (вечеринки, спорт, культура, свидания и т.д.)

Все пользователи имеют пароль: `password123`

## 📝 Шаг 2: Протестировать API

### Вариант A: Через браузер

1. Откройте: `https://tet-a-tet-backend.onrender.com/api/v1/events`
2. Вы увидите список всех событий в формате JSON

### Вариант B: Через Postman/Insomnia

1. **Получить события:**
   ```
   GET https://tet-a-tet-backend.onrender.com/api/v1/events
   ```

2. **Войти в систему:**
   ```
   POST https://tet-a-tet-backend.onrender.com/api/v1/auth/login
   Content-Type: application/json
   
   {
     "email": "alice@test.com",
     "password": "password123"
   }
   ```

3. **Записаться на событие (нужен токен из шага 2):**
   ```
   POST https://tet-a-tet-backend.onrender.com/api/v1/events/id/{eventId}/attend
   Authorization: Bearer <ваш-access-token>
   ```

### Вариант C: Через консоль браузера (для фронтенда)

Откройте консоль разработчика (F12) на любой странице и выполните:

```javascript
// 1. Заполнить базу данных
fetch('https://tet-a-tet-backend.onrender.com/test/seed/all')
  .then(r => r.json())
  .then(console.log);

// 2. Получить события
fetch('https://tet-a-tet-backend.onrender.com/api/v1/events')
  .then(r => r.json())
  .then(data => {
    console.log('События:', data.events);
  });

// 3. Войти в систему
fetch('https://tet-a-tet-backend.onrender.com/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'alice@test.com',
    password: 'password123'
  })
})
  .then(r => r.json())
  .then(data => {
    console.log('Токен:', data.accessToken);
    window.accessToken = data.accessToken; // Сохраняем для дальнейшего использования
  });

// 4. Записаться на событие (сначала выполните шаг 3)
const eventId = 'скопируйте-id-из-списка-событий';
fetch(`https://tet-a-tet-backend.onrender.com/api/v1/events/id/${eventId}/attend`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${window.accessToken}`
  }
})
  .then(r => r.json())
  .then(console.log);
```

## 🔧 Локальная разработка

Если хотите запустить локально:

1. **Установите зависимости:**
   ```bash
   # Убедитесь, что у вас установлен Java 21
   java -version
   ```

2. **Настройте переменные окружения:**
   ```bash
   # Скопируйте файл с примером
   cp .env.example .env
   
   # Отредактируйте .env и добавьте свои значения MongoDB
   ```

3. **Запустите сервер:**
   ```bash
   ./gradlew run
   ```

4. **Сервер запустится на:**
   ```
   http://localhost:8080
   ```

## 📚 Полная документация

- **API документация:** см. `API_USAGE.md`
- **Настройка Render:** см. `RENDER_ENV_SETUP.md`
- **Основной README:** см. `README.md`

## 🐛 Решение проблем

### Ошибка 401 (Unauthorized)
- Убедитесь, что вы используете правильный `accessToken`
- Токен должен быть в заголовке: `Authorization: Bearer <token>`

### Ошибка 404 (Not Found)
- Проверьте URL эндпоинта
- Убедитесь, что база заполнена тестовыми данными

### База данных пустая
- Выполните: `GET /test/seed/all`

### Очистить тестовые данные
- Выполните: `GET /test/seed/clear`

## 📞 Контакты

Если возникли вопросы - проверьте логи на Render Dashboard или документацию в репозитории.
