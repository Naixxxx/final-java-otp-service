# OTP Guardian

Учебный backend-сервис для подтверждения операций одноразовыми кодами. Реализация сделана на Java 17 без Spring: HTTP API построено на `com.sun.net.httpserver`, работа с PostgreSQL 17 выполняется через JDBC/HikariCP.

## Возможности

- регистрация пользователей с ролями `ADMIN` и `USER`;
- запрет второго администратора на уровне сервиса и БД;
- логин с выдачей JWT-токена с ограниченным сроком жизни;
- настройка длины OTP-кода и времени жизни кода;
- генерация OTP для операции;
- доставка OTP через Email, SMPP-эмулятор, Telegram Bot API или файл;
- проверка OTP и перевод статуса в `USED`;
- фоновая задача, которая переводит просроченные активные коды в `EXPIRED`;
- логирование каждого API-запроса.

## Стек

Java 17, Maven, PostgreSQL 17, JDBC, HikariCP, JUnit 5, Mockito, Log4j2, BCrypt, java-jwt, Jakarta Mail / Angus Mail, jSMPP.

## Запуск

1. Создайте БД:

```sql
CREATE DATABASE otp_guardian;
```

2. Настройте `src/main/resources/application.properties`:

```properties
jdbc.url=jdbc:postgresql://localhost:5432/otp_guardian
jdbc.user=postgres
jdbc.password=postgres
jwt.secret=replace-this-secret-with-long-random-string
```

3. Настройте каналы доставки:

- `email.properties` — SMTP;
- `sms.properties` — SMPPsim или другой SMPP-эмулятор;
- `telegram.properties` — токен Telegram-бота.

4. Соберите и запустите:

```bash
mvn clean package
java -jar target/otp-guardian-1.0.0.jar
```

При старте приложение само применяет `schema.sql`.

## API

### Регистрация

```http
POST /auth/register
Content-Type: application/json

{
  "login": "admin",
  "password": "secret123",
  "role": "ADMIN"
}
```

Для обычного пользователя:

```json
{
  "login": "ivan",
  "password": "secret123",
  "role": "USER"
}
```

### Логин

```http
POST /auth/login
Content-Type: application/json

{
  "login": "ivan",
  "password": "secret123"
}
```

Ответ:

```json
{
  "token": "...",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600
}
```

### Генерация OTP

Доступно только роли `USER`.

```http
POST /otp/generate
Authorization: Bearer <USER_TOKEN>
Content-Type: application/json

{
  "operationRef": "PAYMENT-1001",
  "channel": "FILE",
  "destination": "local-test"
}
```

Каналы: `EMAIL`, `SMS`, `TELEGRAM`, `FILE`.

Для Telegram в `destination` передаётся `chat_id` пользователя.

### Проверка OTP

```http
POST /otp/validate
Authorization: Bearer <USER_TOKEN>
Content-Type: application/json

{
  "operationRef": "PAYMENT-1001",
  "code": "123456"
}
```

### Получить конфигурацию OTP

Доступно только роли `ADMIN`.

```http
GET /admin/otp-policy
Authorization: Bearer <ADMIN_TOKEN>
```

### Изменить конфигурацию OTP

```http
PUT /admin/otp-policy
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "codeLength": 6,
  "lifetimeSeconds": 300
}
```

### Список обычных пользователей

```http
GET /admin/users
Authorization: Bearer <ADMIN_TOKEN>
```

### Удаление пользователя

```http
DELETE /admin/users/2
Authorization: Bearer <ADMIN_TOKEN>
```

Удаление пользователя каскадно удаляет связанные OTP-коды через `ON DELETE CASCADE`.

## Тесты

```bash
mvn test
```

Покрыты генерация кода, пароли, JWT, бизнес-логика авторизации и OTP-сервиса.

## Структура

```text
src/main/java/dev/naixxxx/guardcode
├── api          # HTTP handlers
├── auth         # reserved
├── config       # properties and datasource
├── dao          # JDBC repositories
├── domain       # entities/enums
├── dto          # request/response DTO
├── security     # JWT, BCrypt, filters
├── service      # business logic
├── service/delivery # Email/SMS/Telegram/File senders
└── util         # JSON, routing, HTTP helpers
```
