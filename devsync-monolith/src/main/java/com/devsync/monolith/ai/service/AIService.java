package com.devsync.monolith.ai.service;

import com.devsync.monolith.ai.entity.AIRequestLog;
import com.devsync.monolith.ai.repository.AIRequestLogRepository;
import com.devsync.monolith.task.entity.Sprint;
import com.devsync.monolith.task.entity.Task;
import com.devsync.monolith.task.repository.SprintRepository;
import com.devsync.monolith.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatClient.Builder chatClientBuilder;
    private final AIRequestLogRepository aiRequestLogRepository;
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;

    public String generateSprintSummary(UUID sprintId, UUID userId) {
        Sprint sprint = sprintRepository.findByIdAndDeletedFalse(sprintId).orElseThrow();
        List<Task> tasks = taskRepository.findBySprintIdAndDeletedFalse(sprintId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        String taskSummaries = tasks.stream()
                .map(t -> String.format("- %s (Status: %s)", t.getTitle(), t.getStatus()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("Generate a concise executive summary for the sprint '%s' which has the goal '%s'. Here are the tasks:\n%s",
                sprint.getName(), sprint.getGoal(), taskSummaries);

        return callAiAndLog(prompt, "SPRINT_SUMMARY", "SPRINT", sprintId, userId);
    }
    
    public String estimatePoints(UUID taskId, UUID userId) {
        Task task = taskRepository.findByIdAndDeletedFalse(taskId).orElseThrow();
        String prompt = String.format("Estimate story points (Fibonacci sequence: 1, 2, 3, 5, 8, 13) for this task based on its description.\nTitle: %s\nDescription: %s\nPriority: %s. Respond with JUST the number and a 1-sentence justification.",
                task.getTitle(), task.getDescription(), task.getPriority());

        return callAiAndLog(prompt, "ESTIMATE_POINTS", "TASK", taskId, userId);
    }
    
    public String explainBug(UUID taskId, UUID userId) {
        Task task = taskRepository.findByIdAndDeletedFalse(taskId).orElseThrow();
        String prompt = String.format("Explain this bug and suggest a potential fix or debugging steps. Title: %s\nDescription: %s",
                task.getTitle(), task.getDescription());

        return callAiAndLog(prompt, "EXPLAIN_BUG", "TASK", taskId, userId);
    }

    public String generateReleaseNotes(UUID sprintId, UUID userId) {
        Sprint sprint = sprintRepository.findByIdAndDeletedFalse(sprintId).orElseThrow();
        List<Task> completedTasks = taskRepository.findBySprintIdAndStatusNotAndDeletedFalse(sprintId, com.devsync.common.enums.TaskStatus.TODO); // Just grabbing everything not TODO for MVP
        
        String taskSummaries = completedTasks.stream()
                .map(t -> String.format("- %s", t.getTitle()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("Draft professional release notes for Sprint '%s'. Included features/fixes:\n%s",
                sprint.getName(), taskSummaries);

        return callAiAndLog(prompt, "RELEASE_NOTES", "SPRINT", sprintId, userId);
    }

    public String projectHealth(UUID projectId, UUID userId) {
        long totalTasks = taskRepository.countByProjectIdAndDeletedFalse(projectId);
        String prompt = String.format("Based on %d total tasks in project %s, provide a brief 3-sentence health assessment template that a manager can fill out.", totalTasks, projectId);
        return callAiAndLog(prompt, "PROJECT_HEALTH", "PROJECT", projectId, userId);
    }

    public String chat(String userMessage, UUID userId) {
        String prompt = "You are a helpful Agile Project Management assistant for DevSync AI. User asks: " + userMessage;
        return callAiAndLog(prompt, "CHAT", null, null, userId);
    }

    @Transactional
    protected String callAiAndLog(String prompt, String feature, String entityType, UUID entityId, UUID userId) {
        long startTime = System.currentTimeMillis();
        
        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
                
        long latency = System.currentTimeMillis() - startTime;

        AIRequestLog log = AIRequestLog.builder()
                .userId(userId)
                .feature(feature)
                .entityType(entityType)
                .entityId(entityId)
                .prompt(prompt)
                .response(response)
                .model("groq-llama3") // Hardcoded for logging MVP
                .tokensUsed(0) // Spring AI doesn't easily expose this in simple .content() call yet
                .latencyMs(latency)
                .build();
                
        aiRequestLogRepository.save(log);
        
        return response;
    }
}
