package com.school360.controller;

import com.school360.service.AILearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIChatController {

    @Autowired
    private AILearningService aiLearningService;

    /**
     * Real-time Chat Endpoint: Process natural language queries with live system data & learned knowledge
     */
    @PostMapping("/chat")
    public ResponseEntity<?> processChatMessage(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        String userRole = payload.get("userRole");
        String username = payload.get("username");
        String pageContext = payload.get("pageContext");

        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message content cannot be empty."));
        }

        Map<String, Object> result = aiLearningService.generateAIResponse(message, userRole, username, pageContext);
        return ResponseEntity.ok(result);
    }

    /**
     * Dynamic Training Endpoint: Train the chatbot with custom Q&A dataset pairs
     */
    @PostMapping("/train")
    public ResponseEntity<?> trainAIChatbot(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        String answer = payload.get("answer");

        String statusMessage = aiLearningService.trainKnowledge(question, answer);
        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "message", statusMessage,
            "trainedKnowledge", aiLearningService.getTrainedKnowledge()
        ));
    }

    /**
     * View Trained Knowledge Dataset
     */
    @GetMapping("/knowledge")
    public ResponseEntity<?> getTrainedKnowledge() {
        return ResponseEntity.ok(aiLearningService.getTrainedKnowledge());
    }
}
