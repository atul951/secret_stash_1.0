# Secret Stash - Secure Note-Taking API

A secure note-taking API built with Spring Boot and Kotlin, featuring JWT authentication, user isolation, and rate limiting.

## Features

- **JWT-based Authentication**: Secure user registration and login
- **CRUD Operations for Notes**: Create, read, update, and delete notes
- **User Isolation**: Each user can only access their own notes
- **Note Expiry**: Optional expiry timestamps for notes
- **Rate Limiting**: Protection against brute-force attacks
- **Latest Notes Endpoint**: Retrieve the latest 1,000 notes sorted by creation date

## Technology Stack

- **Backend**: Kotlin with Spring Boot v3.5.3
- **Database**: H2 (in-memory for development)
- **Security**: Spring Security with JWT
- **Build Tool**: Gradle v8+
- **Testing**: JUnit 5 with Spring Boot Test

## Prerequisites

- Java 17 or higher
- Gradle (included in the project)

## How to Run

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd note
   ```

2. **Run the application**:
   ```bash
   ./gradlew bootRun
   ```

3. **Access the application**:
   - API Base URL: `http://localhost:8080`
   - Swagger UI: http://localhost:8080/swagger-ui/index.html

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login user

### Notes

- `POST /api/notes` - Create a new note
- `GET /api/notes/{noteId}` - Get a specific note
- `GET /api/notes` - Get all notes (paginated - default 20)
- `GET /api/notes/latest` - Get latest notes (limited - default 1000)
- `PUT /api/notes/{noteId}` - Update a note
- `DELETE /api/notes/{noteId}` - Delete a note

## API Usage Examples

### 1. Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testUser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testUser",
    "password": "password123"
  }'
```

### 3. Create a Note
```bash
curl -X POST http://localhost:8080/api/notes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "My Secret Note",
    "content": "This is my secret content",
    "expiresAt": "2024-12-31T23:59:59"
  }'
```

### 4. Get All Notes
```bash
curl -X GET http://localhost:8080/api/notes \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. Get Latest Notes
```bash
curl -X GET http://localhost:8080/api/notes/latest \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Running Tests

```bash
./gradlew test
```

## Architectural Decisions

### 1. **Security First Approach**
- JWT tokens for stateless authentication
- BCrypt password hashing for secure password storage
- User isolation ensures data privacy
- Rate limiting prevents brute-force attacks

### 2. **Database Design**
- H2 in-memory database for development simplicity
- JPA/Hibernate for ORM
- Custom queries to filter expired notes automatically
- Using username for foreign key to avoid joins in queries

### 3. **API Design**
- RESTful principles with proper HTTP status codes
- Input validation using Bean Validation
- Consistent error responses
- Pagination for large datasets

### 4. **Code Organization**
- Clear separation of concerns (Controller → Service → Repository)
- DTOs for request/response handling
- Global exception handling
- Comprehensive test coverage

### 5. **Performance Considerations**
- Lazy loading for user relationships with notes
- Efficient queries with proper filtering
- Rate limiting to prevent abuse
- Pagination for large result sets

## Configuration

Key configuration properties in `application.properties`:

- `jwt.secret`: Secret key for JWT signing
- `jwt.expiration`: JWT token expiration time (24 hours)
- `rate.limit.requests-per-minute`: Rate limiting threshold (60 requests/minute)
- `server.port`: Application port (8080)

## Security Features

1. **Authentication**: JWT-based authentication
2. **Authorization**: User-specific data access
3. **Password Security**: BCrypt hashing
4. **Rate Limiting**: Per IP and per username rate limiting for login attempts 
5. **Input Validation**: Comprehensive request validation
6. **Error Handling**: Secure error messages without information leakage

## Testing Strategy

- Unit tests for services
- Integration tests for controllers for API endpoints
- Security tests for authentication and authorization
- Validation tests for request/response handling
- End-to-End tests for overall functionality for user journey

## Future Enhancements

- Database migration to PostgreSQL for production
- Social authentication integration for OAUTH2
- APIs to list and delete expired user Notes
- Setting up email client to send user registration confirmation email
- API to verify email and reset user password via emails
- API to export notes
- Notifications to user when notes are expired or about to be expired 
- Docker containerization
- CI/CD pipeline setup
- Monitoring and logging improvements
