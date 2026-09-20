package com.devsync.aiservice.ai.service;

import com.devsync.aiservice.ai.entity.AIRequestLog;
import com.devsync.aiservice.ai.repository.AIRequestLogRepository;
import com.devsync.aiservice.task.entity.Sprint;
import com.devsync.aiservice.task.entity.Task;
import com.devsync.aiservice.task.repository.SprintRepository;
import com.devsync.aiservice.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final ChatClient.Builder chatClientBuilder;
    private final AIRequestLogRepository aiRequestLogRepository;
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final org.springframework.ai.embedding.EmbeddingModel embeddingModel;

    @org.springframework.beans.factory.annotation.Value("${spring.ai.openai.api-key:}")
    private String geminiApiKey;

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

    public String deadlineRisk(UUID projectId, UUID userId) {
        long totalTasks = taskRepository.countByProjectIdAndDeletedFalse(projectId);
        String prompt = String.format("Analyze the deadline risks for the project '%s' which has %d tasks in total. Provide a brief 3-sentence summary highlighting critical bottlenecks.",
                projectId, totalTasks);
        return callAiAndLog(prompt, "DEADLINE_RISK", "PROJECT", projectId, userId);
    }

    public String generateDocs(UUID projectId, UUID userId) {
        long totalTasks = taskRepository.countByProjectIdAndDeletedFalse(projectId);
        String prompt = String.format("Generate release documentation outline for the project '%s' based on its current progress of %d tasks.",
                projectId, totalTasks);
        return callAiAndLog(prompt, "GENERATE_DOCS", "PROJECT", projectId, userId);
    }

    public String taskPriority(UUID projectId, UUID userId) {
        List<Task> tasks = taskRepository.findByProjectIdAndDeletedFalse(projectId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        String taskSummaries = tasks.stream()
                .map(t -> String.format("- %s (Priority: %s)", t.getTitle(), t.getPriority()))
                .collect(Collectors.joining("\n"));
        String prompt = String.format("Analyze these project tasks and prioritize them logically from highest impact to lowest impact:\n%s",
                taskSummaries);
        return callAiAndLog(prompt, "TASK_PRIORITY", "PROJECT", projectId, userId);
    }

    public String chat(String userMessage, UUID projectId, UUID userId) {
        log.info("[DEBUG STAGE 3] Workspace loaded. project context: {}", projectId);
        
        // Check for bypass logic
        String trimmedMsg = userMessage.trim().toLowerCase();
        if (trimmedMsg.equals("hi") || trimmedMsg.equals("say hello")) {
            log.info("[DEBUG STAGE RAG BYPASS] User sent '{}', bypassing RAG pipeline.", userMessage);
            log.info("[DEBUG STAGE 9] Groq request started.");
            String response = callAiAndLog("Say hello.", "CHAT", null, null, userId);
            log.info("[DEBUG STAGE 10] Groq response received.");
            return response;
        }

        // 1. Retrieve Context Tasks from Relational Database
        org.springframework.data.domain.Pageable limit = org.springframework.data.domain.PageRequest.of(0, 50);
        List<Task> tasks;
        if (projectId != null) {
            tasks = taskRepository.findByProjectIdAndDeletedFalse(projectId, limit).getContent();
        } else {
            tasks = taskRepository.findAll(limit).getContent();
        }
        log.info("[DEBUG STAGE 4] Documents loaded. Total tasks retrieved: {}", tasks.size());

        // 2. Generate Embeddings & Index into Vector Store
        List<org.springframework.ai.document.Document> documents = tasks.stream()
                .map(t -> new org.springframework.ai.document.Document(
                        String.format("Task Title: %s. Description: %s. Status: %s. Priority: %s.",
                                t.getTitle(), t.getDescription(), t.getStatus(), t.getPriority()),
                        java.util.Map.of("id", t.getId().toString(), "type", "task")
                ))
                .collect(Collectors.toList());

        String context = "";
        if (!documents.isEmpty()) {
            try {
                if (geminiApiKey != null && geminiApiKey.trim().startsWith("gsk_")) {
                    throw new UnsupportedOperationException("Embeddings not supported for Groq");
                }
                log.info("[DEBUG STAGE 5] Embeddings started.");
                org.springframework.ai.vectorstore.SimpleVectorStore vectorStore = 
                        new org.springframework.ai.vectorstore.SimpleVectorStore(embeddingModel);
                
                vectorStore.add(documents);
                log.info("[DEBUG STAGE 6] Embeddings completed.");

                log.info("[DEBUG STAGE 7] Vector search started.");
                List<org.springframework.ai.document.Document> similarDocs = vectorStore.similaritySearch(
                        org.springframework.ai.vectorstore.SearchRequest.query(userMessage).withTopK(5)
                );
                log.info("[DEBUG STAGE 8] Vector search completed.");

                context = similarDocs.stream()
                        .map(org.springframework.ai.document.Document::getContent)
                        .collect(Collectors.joining("\n\n"));
            } catch (Exception e) {
                log.error("[RAG FLOW] Embeddings generation/vector search failed: {}", e.getMessage(), e);
                // Fallback to simple title/description search context to prevent infinite loading or total crash
                context = tasks.stream()
                        .filter(t -> t.getTitle().toLowerCase().contains(userMessage.toLowerCase()) 
                                || (t.getDescription() != null && t.getDescription().toLowerCase().contains(userMessage.toLowerCase())))
                        .limit(5)
                        .map(t -> String.format("Task Title: %s. Description: %s. Status: %s. Priority: %s.",
                                t.getTitle(), t.getDescription(), t.getStatus(), t.getPriority()))
                        .collect(Collectors.joining("\n\n"));
            }
        } else {
            log.info("[DEBUG STAGE 5] Embeddings skipped (no documents).");
            log.info("[DEBUG STAGE 6] Embeddings completed.");
            log.info("[DEBUG STAGE 7] Vector search skipped.");
            log.info("[DEBUG STAGE 8] Vector search completed.");
        }

        // 3. Build Enriched Prompt
        String prompt = String.format(
                "You are a helpful Agile Project Management assistant for DevSync AI.\n" +
                "Here is the retrieved project context (relevant tasks/sprints):\n%s\n\n" +
                "Based on this context, answer the user's question: %s",
                context.isEmpty() ? "No relevant project tasks found in context." : context,
                userMessage
        );

        // 4. Send to Groq
        log.info("[DEBUG STAGE 9] Groq request started.");
        String response = callAiAndLog(prompt, "CHAT", null, null, userId);
        log.info("[DEBUG STAGE 10] Groq response received.");
        return response;
    }

    protected String callAiAndLog(String prompt, String feature, String entityType, UUID entityId, UUID userId) {
        log.info("[AI SERVICE] Starting AI request for feature: {}, user: {}", feature, userId);
        
        boolean keyExists = (geminiApiKey != null && !geminiApiKey.trim().isEmpty() && !geminiApiKey.equals("dummy-key-value"));
        log.info("[DEBUG INFO] GROQ_API_KEY loaded: {}", keyExists);
        log.info("[DEBUG INFO] Groq model in use: llama-3.1-8b-instant");

        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || geminiApiKey.equals("dummy-key-value")) {
            log.error("[AI SERVICE] Groq API key is missing. Please configure GROQ_API_KEY.");
            throw new IllegalArgumentException("Groq API key is missing. Please configure GROQ_API_KEY.");
        }

        log.debug("[AI SERVICE] Prompt content: {}", prompt);
        long startTime = System.currentTimeMillis();
        
        String response;
        try {
            log.info("[AI SERVICE] Building ChatClient...");
            ChatClient chatClient = chatClientBuilder.build();
            log.info("[AI SERVICE] Sending request to LLM (Groq API)...");
            response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            log.info("[AI SERVICE] Received response from LLM (Groq API) in {} ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("[AI SERVICE] Failed during LLM call: {}", e.getMessage(), e);
            throw e;
        }
        
        long latency = System.currentTimeMillis() - startTime;

        try {
            log.info("[AI SERVICE] Logging request to database...");
            saveRequestLog(userId, feature, entityType, entityId, prompt, response, latency);
            log.info("[AI SERVICE] Database log saved successfully.");
        } catch (Exception e) {
            log.warn("[AI SERVICE] Failed to save AI request log to database: {}", e.getMessage());
        }
        
        return response;
    }

    @Transactional
    public void saveRequestLog(UUID userId, String feature, String entityType, UUID entityId, String prompt, String response, long latency) {
        AIRequestLog logEntity = AIRequestLog.builder()
                .userId(userId)
                .feature(feature)
                .entityType(entityType)
                .entityId(entityId)
                .prompt(prompt)
                .response(response)
                .model("llama-3.1-8b-instant")
                .tokensUsed(0) 
                .latencyMs(latency)
                .build();
        aiRequestLogRepository.save(logEntity);
    }
}
