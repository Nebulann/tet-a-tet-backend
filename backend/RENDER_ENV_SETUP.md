# Настройка переменных окружения на Render.com

## Обязательные переменные

Эти переменные **ОБЯЗАТЕЛЬНО** нужно установить в Render Dashboard → Environment:

### 1. Ktor Server
```
KTOR_PORT=8080
```

### 2. MongoDB Connection
```
MONGO_HOST=<your-mongodb-host>
MONGO_PORT=27017
MONGO_RS=
MONGO_DATABASE=kupidon
MONGO_BACKEND_CLIENT_CERT=
MONGO_CA_CERT=
```

**Примечание:** Если используешь MongoDB Atlas, получи connection string и настрой переменные соответственно.

### 3. Password Hashing
```
PWD_HASHING_ALGORITHM=PBKDF2WithHmacSHA256
PWD_HASHING_SECRET=<generate-random-secret-here>
PWD_HASHING_ITERATIONS=10000
PWD_HASHING_HASH_LEN=256
```

**Важно:** Сгенерируй случайный секретный ключ для `PWD_HASHING_SECRET` (минимум 32 символа).

### 4. JWT Authentication
```
AUTH_REFRESH_TOKEN_SECRET=<generate-random-secret-here>
AUTH_ACCESS_TOKEN_SECRET=<generate-random-secret-here>
```

**Важно:** Сгенерируй два разных случайных секретных ключа (минимум 32 символа каждый).

## Опциональные переменные

Эти переменные можно добавить позже, когда понадобится соответствующий функционал:

### Firebase (для push-уведомлений)
```
FIREBASE_SERVICE_ACCOUNT_PATH=/app/firebase-service-account.json
FIREBASE_SENDER_ID=<your-sender-id>
FIREBASE_VAPID_PUBLIC_KEY=<your-vapid-public-key>
```

### Email (для отправки писем)
```
MAIL_EMAIL=<your-email@example.com>
MAIL_PWD=<your-email-app-password>
```

## Как добавить переменные на Render.com

1. Открой **Render Dashboard**
2. Выбери свой backend сервис
3. Перейди в **Environment**
4. Нажми **Add Environment Variable**
5. Добавь каждую переменную по очереди
6. Нажми **Save Changes**
7. Render автоматически пересоберёт и перезапустит сервис

## Генерация секретных ключей

Используй один из способов:

### Node.js
```bash
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
```

### Python
```bash
python -c "import secrets; print(secrets.token_hex(32))"
```

### OpenSSL
```bash
openssl rand -hex 32
```

### Online
https://www.random.org/strings/?num=1&len=64&digits=on&upperalpha=on&loweralpha=on&unique=on&format=html&rnd=new
