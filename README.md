# Explore With Me

**Сервис событий и подборок** — микросервисное приложение на Java и Spring Boot: пользователи создают события, другие пользователи отправляют заявки на участие и голосуют за событие, события собираются в подборки, а отдельный сервис считает статистику просмотров.

---

## Архитектура

Проект построен как два независимых приложения + клиентский модуль между ними.

```
            ┌────────────────────┐         ┌──────────────────────┐
   клиент → │     emw-service    │  HTTP   │    stats-server      │
            │   (порт 8080)      │ ──────► │     (порт 9090)      │
            │  основной сервис   │         │  сбор статистики     │
            └─────────┬──────────┘         └──────────┬───────────┘
                      │                               │
                ┌─────▼─────┐                   ┌─────▼──────┐
                │  ewm-db   │                   │  stats-db  │
                │ PostgreSQL│                   │ PostgreSQL │
                └───────────┘                   └────────────┘
```

Cервис статистики вынесен в отдельное приложение с отдельной базой — основной сервис не хранит телеметрию и не падает, если сервис статистики недоступен. Взаимодействие идёт через выделенный клиентский модуль `stats-client`.

### Структура модулей

```
explore-with-me/                 # корневой pom
├── emw-service/                 # основной сервис (Spring Boot, порт 8080)
├── stats-server/                # агрегирующий модуль
│   ├── stats-dto/               # общие DTO контракта между сервисами
│   ├── stats-client/            # HTTP-клиент для обращения к stats-server
│   └── stats-service/           # сервис статистики (Spring Boot, порт 9090)
├── docker-compose.yml           # 4 сервиса: 2 приложения + 2 PostgreSQL
├── emw-main-service-spec.json   # спецификация основного сервиса
├── emw-stats-service-spec.json  # спецификация сервиса статистики
└── postman/feature.json         # Postman-коллекция для ручной проверки сценариев
```

---

## Возможности основного сервиса

API разделён на три группы по уровню доступа.

### Публичные — доступны всем

| Метод | Путь | Назначение |
|-------|------|------------|
| `GET` | `/events` | Список событий с фильтрами: `text`, `categories`, `paid`, `rangeStart`, `rangeEnd`, `onlyAvailable`, `sortOption`, `from`, `size` |
| `GET` | `/events/{id}` | Событие по id |
| `GET` | `/categories` | Список категорий |
| `GET` | `/categories/{id}` | Категория по id |
| `GET` | `/compilations` | Список подборок |
| `GET` | `/compilations/{id}` | Подборка по id |
| `GET` | `/users/rating` | Топ пользователей по рейтингу их событий (`from`, `size`) |

**Сортировка** (`sortOption`):

| Значение | Что сортирует |
|----------|---------------|
| `EVENT_DATE` | по дате события (по умолчанию) |
| `VIEWS` | по количеству просмотров |
| `RATING` | по рейтингу события |

### Приватные — от имени пользователя

| Метод | Путь | Назначение |
|-------|------|------------|
| `POST` | `/users/{userId}/events` | Создать событие |
| `GET` | `/users/{userId}/events` | Свои события (в т. ч. по категориям и состояниям) |
| `GET` | `/users/{userId}/events/{eventId}` | Своё событие по id |
| `PATCH` | `/users/{userId}/events/{eventId}` | Изменить своё событие |
| `GET` | `/users/{userId}/events/{eventId}/requests` | Заявки на участие в своём событии |
| `PATCH` | `/users/{userId}/events/{eventId}/requests` | Одобрить/отклонить заявки |
| `POST` | `/users/{userId}/events/{eventId}/rate` | Поставить оценку событию |
| `PATCH` | `/users/{userId}/events/{eventId}/rate` | Изменить свою оценку |
| `DELETE` | `/users/{userId}/events/{eventId}/rate` | Убрать свою оценку |
| `POST` | `/users/{userId}/requests` | Отправить заявку на участие |
| `GET` | `/users/{userId}/requests` | Свои заявки |
| `PATCH` | `/users/{userId}/requests/{requestId}/cancel` | Отменить свою заявку |

### Административные

| Метод | Путь | Назначение |
|-------|------|------------|
| `GET` | `/admin/users` | Пользователи с фильтрами (min/max id, диапазон дат регистрации) |
| `POST` | `/admin/users` | Создать пользователя |
| `DELETE` | `/admin/users/{userId}` | Удалить пользователя |
| `GET` | `/admin/events` | События с фильтрами: `users`, `states`, `categories`, `rangeStart`, `rangeEnd`, `from`, `size` |
| `PATCH` | `/admin/events/{eventId}` | Изменить любое событие (в т. ч. состояние) |
| `POST` | `/admin/categories` | Создать категорию |
| `PATCH` | `/admin/categories/{catId}` | Изменить категорию |
| `DELETE` | `/admin/categories/{catId}` | Удалить категорию (только если не используется) |
| `POST` | `/admin/compilations` | Создать подборку |
| `PATCH` | `/admin/compilations/{compId}` | Изменить подборку |
| `DELETE` | `/admin/compilations/{compId}` | Удалить подборку |

### Модель события

| Поле | Смысл |
|------|-------|
| `title`, `annotation`, `description` | Заголовок, краткое описание, полное описание |
| `category` | Категория события |
| `eventDate`, `createdOn`, `publishedOn` | Дата события, дата создания, дата публикации |
| `location` | Координаты  |
| `paid` | Платное ли событие |
| `participantLimit` | Максимальное число участников |
| `requestModeration` | Нужно ли подтверждение заявок инициатором |
| `state` | `PENDING` → `PUBLISHED` / `CANCELED` |
| `initiator` | Инициатор события |

### Заявки на участие

Статусы заявки: `PENDING` → `CONFIRMED` / `REJECTED`, `CANCELED`. Стоит ограничение уникальности — один пользователь не может отправить две заявки на одно событие.

При одобрении заявок инициатор учитывает `participantLimit`: подтверждаются заявки до достижения лимита, лишние автоматически отклоняются, а оставшиеся в `PENDING` — тоже отклоняются, если лимит уже выбран.

### Рейтинг событий

Участники события оценивают его лайком или дизлайком. 

**Правила выставления оценки**:

| Правило | Поведение |
|---------|-----------|
| Пользователь и событие должны существовать | иначе `404` |
| Инициатор не может оценивать своё событие | `RatingCreationException` |
| Событие должно быть опубликовано (`PUBLISHED`) | `RatingCreationException` |
| У пользователя должна быть **подтверждённая** заявка на участие | `RequestCreationException` |
| Повторная оценка того же события | `RatingCreationException` |

### Подборки (compilations)

Подборка — это набор событий. Поле `pinned` помечает подборку, которую нужно показывать вверху списка.

---

## Сервис статистики

| Метод | Путь | Назначение |
|-------|------|------------|
| `POST` | `/hit` | Зафиксировать обращение к эндпоинту (приложение, URI, IP, timestamp) |
| `GET` | `/stats?start=&end=&uris=&unique=` | Статистика за период: по URI, с флагом `unique` (уникальные IP) |

Основной сервис отправляет «хит» при каждом обращении к `/events` и `/events/{id}` через `StatsClient`.

---

## Стек технологий

| Слой | Технологии |
|------|------------|
| Язык | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Доступ к данным | Spring Data JPA |
| Агрегация в БД | нативные запросы с `GROUP BY`, `SUM(CASE WHEN ...)`, `COALESCE` |
| БД | PostgreSQL 16.1 (две отдельные базы), H2 в режиме PostgreSQL — для тестов |
| Межсервисное взаимодействие | модуль `stats-client`, общий модуль `stats-dto` |
| Сборка | Multi-module Maven (2 модуля верхнего уровня, 4 с учётом вложенных) |
| Контейнеризация | Docker, Docker Compose |
| Тесты | JUnit 5, Spring Boot Test, MockMvc |
| Сборка | Maven, Checkstyle, SpotBugs, JaCoCo |
| CI | GitHub Actions — прогон API-тестов на каждый pull request |
| Документация | OpenAPI-спецификации (JSON) + Postman-коллекция для ручной проверки |

---

## Как запустить

Требования: **Java 21**, **Maven 3.9+**, **Docker + Docker Compose**.

```bash
git clone https://github.com/sellohay/java-explore-with-me.git
cd java-explore-with-me

mvn clean package                       # собрать jar-файлы обоих сервисов
docker compose up --build
```

Поднимется:

| Сервис | Адрес |
|--------|-------|
| Основной сервис (`emw-service`) | `http://localhost:8080` |
| Сервис статистики (`stats-server`) | `http://localhost:9090` |
| БД основного сервиса | `localhost:5433` |
| БД статистики | `localhost:5432` |

### Пример запроса

```bash
curl -X POST http://localhost:8080/admin/users \
  -H 'Content-Type: application/json' \
  -d '{"name": "User", "email": "user@example.com"}'

curl "http://localhost:8080/events?categories=1&sortOption=RATING&from=0&size=10"
```

---

## Тесты 

```bash
mvn test
mvn -Pcheck verify 
mvn -Pcoverage verify
```

Тесты контроллеров написаны на MockMvc (`@SpringBootTest` + `@AutoConfigureMockMvc`): проверяются HTTP-коды, тела ответов и бизнес-правила — создание события, заявки, их одобрение, публикация и отмена событий, работа с категориями, подборками и пользователями. Отдельно покрыт сервис статистики.
Внешний сервис статистики в тестах основного сервиса заменён моком.
Тестовый профиль использует H2 в режиме PostgreSQL, поэтому схема и запросы совпадают с боевой базой.

---

## Схема данных

| Таблица | Поля |
|---------|------|
| `users` | `id`, `name`, `email` |
| `categories` | `id`, `name` |
| `events` | `id`, `annotation`, `description`, `category_id`, `initiator_id`, `event_date`, `created_on`, `published_on`, `lat`, `lon`, `paid`, `participant_limit`, `request_moderation`, `state`, `title` |
| `requests` | `id`, `requester_id`, `event_id`, `created`, `status` + unique `(requester_id, event_id)` |
| `compilations` | `id`, `title`, `pinned` |
| `compilation_events` | `compilation_id`, `event_id` (PK из двух колонок) |
| `ratings` | `id`, `event_id`, `user_id`, `liked` + unique `(event_id, user_id)` |
| `endpoint_hits` (в БД статистики) | `id`, `app`, `uri`, `ip`, `timestamp` |
