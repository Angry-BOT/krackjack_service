package com.krackjack.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_sessions")
public class ChatSession {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private String sessionId;

        @Column(nullable = false)
        private LocalDateTime createdAt;

        @Column(columnDefinition = "TEXT")
        private String jobDescription;

        @Column(columnDefinition = "TEXT")
        private String intervieweeBackground;

        @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<ChatMessage> messages = new ArrayList<>();

        // Getters and setters

        public String getSessionId() {
                return sessionId;
        }

        public void setSessionId(String sessionId) {
                this.sessionId = sessionId;
        }

        public LocalDateTime getCreatedAt() {
                return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
                this.createdAt = createdAt;
        }

        public String getJobDescription() {
                return jobDescription;
        }

        public void setJobDescription(String jobDescription) {
                this.jobDescription = jobDescription;
        }

        public String getIntervieweeBackground() {
                return intervieweeBackground;
        }

        public void setIntervieweeBackground(String intervieweeBackground) {
                this.intervieweeBackground = intervieweeBackground;
        }

        public List<ChatMessage> getMessages() {
                return messages;
        }

        public void setMessages(List<ChatMessage> messages) {
                this.messages = messages;
        }
}