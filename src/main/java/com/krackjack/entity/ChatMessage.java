package com.krackjack.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "session_id", nullable = false)
        private ChatSession chatSession;

        @Column(columnDefinition = "TEXT", nullable = false)
        private String content;

        @Column(nullable = false)
        private String messageType; // "QUESTION" or "ANSWER"

        @Column(nullable = false)
        private LocalDateTime timestamp;

        // Getters and setters

        public Long getId() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public ChatSession getChatSession() {
                return chatSession;
        }

        public void setChatSession(ChatSession chatSession) {
                this.chatSession = chatSession;
        }

        public String getMessageType() {
                return messageType;
        }

        public void setMessageType(String messageType) {
                this.messageType = messageType;
        }

        public String getContent() {
                return content;
        }

        public void setContent(String content) {
                this.content = content;
        }

        public LocalDateTime getTimestamp() {
                return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
        }
}