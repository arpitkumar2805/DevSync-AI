package com.devsync.monolith.ai.service;

import com.devsync.monolith.ai.entity.AIRequestLog;
import com.devsync.monolith.ai.repository.AIRequestLogRepository;
import com.devsync.monolith.sprint.entity.Sprint;
import com.devsync.monolith.task.entity.Task;
import com.devsync.monolith.project.entity.Project;
import com.devsync.monolith.sprint.repository.SprintRepository;
import com.devsync.monolith.task.repository.TaskRepository;
import com.devsync.monolith.project.repository.ProjectRepository;
import com.devsync.monolith.auth.entity.User;
import com.devsync.monolith.auth.repository.UserRepository;
import com.devsync.common.enums.ProjectStatus;
import com.devsync.common.enums.SprintStatus;
import com.devsync.common.enums.TaskStatus;
import com.devsync.common.enums.TaskType;
import com.devsync.common.enums.Priority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final ChatClient.Builder chatClientBuilder;
    private final AIRequestLogRepository aiRequestLogRepository;
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
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
        List<Task> tasks = taskRepository.findByProjectIdAndDeletedFalse(projectId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        long totalTasks = tasks.size();
        long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS || t.getStatus() == TaskStatus.REVIEW).count();
        long todo = tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO || t.getStatus() == TaskStatus.BACKLOG).count();

        String prompt = String.format("Provide a realistic sprint progress/health report for this project. Total Tasks: %d, Completed: %d, In Progress: %d, To Do: %d. Keep it to 3 concise sentences. Focus on the actual data and sprint pace.",
                totalTasks, completed, inProgress, todo);
        return callAiAndLog(prompt, "PROJECT_HEALTH", "PROJECT", projectId, userId);
    }

    public String deadlineRisk(UUID projectId, UUID userId) {
        List<Task> tasks = taskRepository.findByProjectIdAndDeletedFalse(projectId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        long totalTasks = tasks.size();
        long criticalOrHigh = tasks.stream().filter(t -> t.getPriority() == Priority.CRITICAL || t.getPriority() == Priority.HIGH).count();
        long overdue = tasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now()) && t.getStatus() != TaskStatus.DONE).count();

        String prompt = String.format("Analyze the deadline risks for the project. Total tasks: %d, Critical/High Priority: %d, Overdue (and not completed): %d. Provide a brief 3-sentence risk summary highlighting actual bottleneck areas.",
                totalTasks, criticalOrHigh, overdue);
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

        // Retrieve all tasks for the project (up to 100 tasks)
        org.springframework.data.domain.Pageable limit = org.springframework.data.domain.PageRequest.of(0, 100);
        List<Task> tasks;
        if (projectId != null) {
            tasks = taskRepository.findByProjectIdAndDeletedFalse(projectId, limit).getContent();
        } else {
            tasks = taskRepository.findAll(limit).getContent();
        }
        log.info("[DEBUG STAGE 4] Documents loaded. Total tasks retrieved: {}", tasks.size());

        // Get user details
        String userContextStr = "Current User ID: " + userId;
        try {
            com.devsync.monolith.auth.entity.User currentUser = userRepository.findByIdAndDeletedFalse(userId).orElse(null);
            if (currentUser != null) {
                userContextStr = String.format("Current User: %s %s (ID: %s, Email: %s, Role: %s)",
                        currentUser.getFirstName(), currentUser.getLastName(), currentUser.getId(), currentUser.getEmail(), currentUser.getRoleName());
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve user details for chat context: {}", e.getMessage());
        }

        // Format tasks summary
        String allTasksSummary = tasks.stream()
                .map(t -> String.format("- Task Title: '%s' | Status: %s | Priority: %s | Assignee ID: %s | Story Points: %s | Due Date: %s | Description: %s",
                        t.getTitle(),
                        t.getStatus(),
                        t.getPriority(),
                        t.getAssigneeId() != null ? t.getAssigneeId().toString() : "Unassigned (None)",
                        t.getStoryPoints() != null ? t.getStoryPoints().toString() : "None",
                        t.getDueDate() != null ? t.getDueDate().toString() : "None",
                        t.getDescription() != null ? t.getDescription() : ""))
                .collect(Collectors.joining("\n"));

        String context = userContextStr + "\n\nTasks list in the project:\n" + (allTasksSummary.isEmpty() ? "No tasks found in this project." : allTasksSummary);

        // 3. Build Enriched Prompt
        String prompt = String.format(
                "You are a helpful Agile Project Management assistant for DevSync AI.\n" +
                "Here is the retrieved project context (relevant tasks/sprints/user):\n%s\n\n" +
                "Based on this context, answer the user's question: %s",
                context,
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

    @Transactional
    public String generateProjectFromPrompt(String prompt, UUID organizationId, UUID userId) {
        String systemInstruction = "Generate a valid JSON object matching the following structure exactly. Do not wrap in markdown tags like ```json or add any explanation. Respond with ONLY the JSON object.\n" +
                "JSON Schema:\n" +
                "{\n" +
                "  \"projectName\": \"Name of the project\",\n" +
                "  \"projectDescription\": \"Description of the project\",\n" +
                "  \"sprints\": [\n" +
                "    {\n" +
                "      \"name\": \"Sprint 1\",\n" +
                "      \"goal\": \"Sprint goal\",\n" +
                "      \"durationWeeks\": 2,\n" +
                "      \"tasks\": [\n" +
                "        {\n" +
                "          \"title\": \"Task Title\",\n" +
                "          \"description\": \"Task description\",\n" +
                "          \"type\": \"STORY\",\n" +
                "          \"priority\": \"HIGH\",\n" +
                "          \"storyPoints\": 5,\n" +
                "          \"daysOffsetFromStart\": 2,\n" +
                "          \"subtasks\": [\n" +
                "            {\n" +
                "              \"title\": \"Subtask Title\",\n" +
                "              \"description\": \"Subtask description\",\n" +
                "              \"priority\": \"MEDIUM\",\n" +
                "              \"storyPoints\": 2\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "Prompt: Generate a complete software development backlog for: " + prompt;

        String response = callAiAndLog(systemInstruction, "PROJECT_GENERATION", "PROJECT", null, userId);

        String cleaned = response.trim();
        if (cleaned.startsWith("```")) {
            int firstLineBreak = cleaned.indexOf("\n");
            int lastBackticks = cleaned.lastIndexOf("```");
            if (firstLineBreak != -1 && lastBackticks > firstLineBreak) {
                cleaned = cleaned.substring(firstLineBreak + 1, lastBackticks).trim();
            }
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(cleaned);

            String projectName = rootNode.get("projectName").asText();
            String projectDescription = rootNode.path("projectDescription").asText("Automatically generated project by AI.");

            Project project = Project.builder()
                    .organizationId(organizationId)
                    .name(projectName)
                    .description(projectDescription)
                    .status(ProjectStatus.ACTIVE)
                    .build();
            project = projectRepository.save(project);

            com.fasterxml.jackson.databind.JsonNode sprintsNode = rootNode.get("sprints");
            int sprintOffsetDays = 0;

            if (sprintsNode != null && sprintsNode.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode sprintNode : sprintsNode) {
                    String sprintName = sprintNode.get("name").asText();
                    String sprintGoal = sprintNode.path("goal").asText();
                    int durationWeeks = sprintNode.path("durationWeeks").asInt(2);

                    LocalDate startDate = LocalDate.now().plusDays(sprintOffsetDays);
                    LocalDate endDate = startDate.plusWeeks(durationWeeks);

                    Sprint sprint = Sprint.builder()
                            .projectId(project.getId())
                            .name(sprintName)
                            .goal(sprintGoal)
                            .startDate(startDate)
                            .endDate(endDate)
                            .status(SprintStatus.PLANNED)
                            .build();
                    sprint = sprintRepository.save(sprint);

                    com.fasterxml.jackson.databind.JsonNode tasksNode = sprintNode.get("tasks");
                    if (tasksNode != null && tasksNode.isArray()) {
                        for (com.fasterxml.jackson.databind.JsonNode taskNode : tasksNode) {
                            String taskTitle = taskNode.get("title").asText();
                            String taskDesc = taskNode.path("description").asText();
                            String taskTypeStr = taskNode.path("type").asText("STORY");
                            String taskPriorityStr = taskNode.path("priority").asText("MEDIUM");
                            int storyPoints = taskNode.path("storyPoints").asInt(3);
                            int daysOffset = taskNode.path("daysOffsetFromStart").asInt(0);

                            Task task = Task.builder()
                                    .projectId(project.getId())
                                    .sprintId(sprint.getId())
                                    .title(taskTitle)
                                    .description(taskDesc)
                                    .type(TaskType.valueOf(taskTypeStr))
                                    .priority(Priority.valueOf(taskPriorityStr))
                                    .status(TaskStatus.TODO)
                                    .storyPoints(storyPoints)
                                    .reporterId(userId)
                                    .dueDate(startDate.plusDays(daysOffset))
                                    .build();
                            task = taskRepository.save(task);

                            com.fasterxml.jackson.databind.JsonNode subtasksNode = taskNode.get("subtasks");
                            if (subtasksNode != null && subtasksNode.isArray()) {
                                for (com.fasterxml.jackson.databind.JsonNode subtaskNode : subtasksNode) {
                                    String subTitle = subtaskNode.get("title").asText();
                                    String subDesc = subtaskNode.path("description").asText();
                                    String subPriorityStr = subtaskNode.path("priority").asText("MEDIUM");
                                    int subStoryPoints = subtaskNode.path("storyPoints").asInt(1);

                                    Task subtask = Task.builder()
                                            .projectId(project.getId())
                                            .sprintId(sprint.getId())
                                            .parentId(task.getId())
                                            .title(subTitle)
                                            .description(subDesc)
                                            .type(TaskType.TASK)
                                            .priority(Priority.valueOf(subPriorityStr))
                                            .status(TaskStatus.TODO)
                                            .storyPoints(subStoryPoints)
                                            .reporterId(userId)
                                            .build();
                                    taskRepository.save(subtask);
                                }
                            }
                        }
                    }
                    sprintOffsetDays += durationWeeks * 7;
                }
            }

            return String.format("Successfully generated project '%s' with auto-inserted sprints and tasks in the database.", projectName);

        } catch (Exception e) {
            log.error("Failed to parse AI generated project JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate project: AI output was not in correct JSON format: " + cleaned, e);
        }
    }
}
