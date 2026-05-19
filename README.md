# NoteApp – Microservices Todo/Notes Application

A beginner-friendly Notes application built with **Java Spring Boot** and **MongoDB**, following a **Microservices Architecture**.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        Browser (UI)                         │
│              Thymeleaf + Bootstrap 5 (port 8081)            │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP
          ┌──────────────▼──────────────┐
          │      User Service           │  port 8081
          │  (Spring Boot + MongoDB)    │
          │                             │
          │  POST /signup               │
          │  POST /login                │
          │  GET  /users/{id}           │
          └──────────────┬──────────────┘
                         │ REST call (RestTemplate)
                         │ POST /notes  (welcome note on signup)
          ┌──────────────▼──────────────┐
          │      Notes Service          │  port 8082
          │  (Spring Boot + MongoDB)    │
          │                             │
          │  POST   /notes              │
          │  PUT    /notes/{id}         │
          │  DELETE /notes/{id}         │
          │  GET    /notes/user/{userId}│
          └─────────────────────────────┘

MongoDB:
  userdb   → users collection
  notesdb  → notes collection
```

### Key Design Decisions
- **No foreign keys** – Notes store `userId` as a plain string reference only
- **No shared entity classes** – each service is fully independent
- **REST-only communication** – services talk via HTTP, never share a database
- **Loose coupling** – if Notes Service is down, signup still succeeds (welcome note is best-effort)

---

## Project Structure

```
Bractus-Assignment/
├── Assignment/          ← User Microservice (port 8081)
│   └── src/main/java/com/bractus/userservice/
│       ├── controller/
│       │   ├── UserController.java    (REST API)
│       │   └── WebController.java     (Thymeleaf UI)
│       ├── service/
│       │   └── UserService.java
│       ├── repository/
│       │   └── UserRepository.java
│       ├── model/
│       │   └── User.java
│       ├── dto/
│       │   ├── SignupRequest.java
│       │   ├── LoginRequest.java
│       │   ├── UserResponse.java
│       │   ├── NoteCreateRequest.java
│       │   └── ErrorResponse.java
│       ├── exception/
│       │   ├── GlobalExceptionHandler.java
│       │   ├── UserAlreadyExistsException.java
│       │   ├── UserNotFoundException.java
│       │   └── InvalidCredentialsException.java
│       └── config/
│           └── AppConfig.java
│
└── NotesService/        ← Notes Microservice (port 8082)
    └── src/main/java/com/bractus/notesservice/
        ├── controller/
        │   └── NoteController.java
        ├── service/
        │   └── NoteService.java
        ├── repository/
        │   └── NoteRepository.java
        ├── model/
        │   └── Note.java
        ├── dto/
        │   ├── NoteCreateRequest.java
        │   ├── NoteUpdateRequest.java
        │   └── ErrorResponse.java
        └── exception/
            ├── GlobalExceptionHandler.java
            ├── NoteNotFoundException.java
            └── NoteOwnershipException.java
```

---

## Prerequisites

- Java 21+
- Maven 3.8+
- MongoDB running locally on port `27017`

---

## Setup & Running Locally

### 1. Start MongoDB

Make sure MongoDB is running on `localhost:27017`. The two databases (`userdb` and `notesdb`) are created automatically on first use.

```bash
# If using MongoDB Community Server
mongod
```

### 2. Start Notes Service first

```bash
cd NotesService
mvn spring-boot:run
```

Notes Service starts on **http://localhost:8082**

### 3. Start User Service

```bash
cd Assignment
mvn spring-boot:run
```

User Service starts on **http://localhost:8081**

### 4. Open the app

Navigate to **http://localhost:8081** in your browser.

---

## API Endpoints

### User Service (port 8081)

| Method | Endpoint        | Description                  | Request Body                          |
|--------|-----------------|------------------------------|---------------------------------------|
| POST   | `/signup`       | Register a new user          | `{ "username": "...", "password": "..." }` |
| POST   | `/login`        | Authenticate a user          | `{ "username": "...", "password": "..." }` |
| GET    | `/users/{id}`   | Get user info by ID          | –                                     |

### Notes Service (port 8082)

| Method | Endpoint                  | Description                        | Notes                              |
|--------|---------------------------|------------------------------------|------------------------------------|
| POST   | `/notes`                  | Create a new note                  | Body: `{ userId, title, content }` |
| PUT    | `/notes/{id}?userId=...`  | Update a note (ownership checked)  | Body: `{ title, content }`         |
| DELETE | `/notes/{id}?userId=...`  | Delete a note (ownership checked)  | –                                  |
| GET    | `/notes/user/{userId}`    | Get all notes for a user           | Returns newest first               |

---

## Auto Welcome Note Feature

When a new user signs up:
1. User Service saves the user to `userdb`
2. User Service calls `POST http://localhost:8082/notes` with:
   ```json
   {
     "userId": "<new user's id>",
     "title": "Welcome",
     "content": "Welcome to Note <username>"
   }
   ```
3. Notes Service saves the welcome note to `notesdb`

This is a **manual string reference** — not a database foreign key.

---

## UI Screens

| Screen    | URL          | Description                              |
|-----------|--------------|------------------------------------------|
| Login     | `/login`     | Sign in with username and password       |
| Signup    | `/signup`    | Create a new account                     |
| Dashboard | `/dashboard` | View, create, edit, and delete notes     |
| Edit Note | `/notes/edit/{id}` | Edit an existing note             |

---

## Tech Stack

| Layer      | Technology                        |
|------------|-----------------------------------|
| Backend    | Java 21, Spring Boot 4.x          |
| Database   | MongoDB (via Spring Data MongoDB) |
| Frontend   | Thymeleaf + Bootstrap 5           |
| Build      | Maven                             |
| HTTP Client| RestTemplate (inter-service)      |

---

## MongoDB Collections

**userdb.users**
```json
{
  "_id": "ObjectId",
  "username": "string",
  "password": "string"
}
```

**notesdb.notes**
```json
{
  "_id": "ObjectId",
  "userId": "string",
  "title": "string",
  "content": "string",
  "createdAt": "ISODate",
  "updatedAt": "ISODate"
}
```

> `userId` in notes is a plain string — not a MongoDB DBRef or foreign key.

---

## Notes on Security

This project is intentionally kept simple for learning purposes:
- Passwords are stored as plain text (in production, use BCrypt)
- Session-based auth with no JWT
- No Spring Security configured

These are known trade-offs made to keep the code beginner-friendly.
