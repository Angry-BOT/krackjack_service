# Krackjack Service - AI-Powered Interview Platform

A Spring Boot-based backend service that provides real-time interview capabilities using WebSocket connections. The service converts speech to text in real-time and generates contextual responses using Google's Gemini AI.

## Features

- Real-time audio processing and transcription
- AI-powered interview responses using Gemini API
- WebSocket-based real-time communication
- Persistent chat history using PostgreSQL
- Scalable architecture with thread pooling
- Comprehensive error handling and logging

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher
- Google Cloud Platform account with:
  - Gemini API access
  - API key configured

## Setup Instructions

1. Clone the repository
2. Create `application.properties` in `src/main/resources/`:
   - Gemini API Configuration
     gemini.api.key=your-gemini-api-key
   - Database Configuration
     spring.datasource.url=jdbc:postgresql://localhost:5432/krackjack_db
     spring.datasource.username=krackjack_local_user
     spring.datasource.password=local@123
     spring.jpa.hibernate.ddl-auto=update
   - sql
     CREATE DATABASE krackjack_db;
   - bash
     mvn clean install
     java -jar target/services-0.0.1-SNAPSHOT.jar

## API Documentation

### WebSocket Endpoints

- **Endpoint**: `/interview`
- **Supported Operations**:
  1. Setup Connection (Text Message):
  ```json
  {
    "type": "setup",
    "jobDescription": "Job details...",
    "intervieweeBackground": "Candidate background..."
  }
  ```
  2. Audio Processing (Binary Message):
  - Send: Raw audio data (WAV format)
  - Receive:
    ```json
    {
      "type": "transcription",
      "content": "Transcribed text"
    }
    ```
    ```json
    {
      "type": "assistant_response",
      "content": "AI-generated response"
    }
    ```

## Project Structure

src/main/java/com/krackjack/
├── config/
│ ├── WebSocketConfig.java # WebSocket and thread pool configuration
│ ├── DatabaseConfig.java # Database connection setup
│ └── RestTemplateConfig.java # HTTP client configuration
├── handler/
│ └── InterviewWebSocketHandler.java # WebSocket message processing
├── services/
│ ├── SpeechToTextService.java # Audio transcription using Gemini
│ └── GeminiService.java # AI response generation
├── entity/
│ ├── ChatSession.java # Interview session entity
│ └── ChatMessage.java # Message entity
└── repository/
├── ChatSessionRepository.java
└── ChatMessageRepository.java

## Error Handling

- Invalid audio data: Returns empty transcription
- Connection issues: Automatic retry with exponential backoff
- Database errors: Transactional rollback
- WebSocket errors: Error message sent to client

## Logging

- Application logs: `logs/application.log`
- Error logs: `logs/error.log`
- Debug logs: Enable with `logging.level.com.krackjack=DEBUG`

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## Key Features

- Real-time WebSocket communication
- Speech-to-Text conversion
- AI-powered responses using Gemini API
- Comprehensive logging system

## License

This project is licensed under the MIT License - see the LICENSE file for details.
