package com.school360.service;

import com.school360.model.*;
import com.school360.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AILearningService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EnrollmentRequestRepository enrollmentRequestRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SystemLogRepository logRepository;

    // Dynamic Learned Knowledge Base (Query Pattern -> Response / Data Template)
    private final Map<String, String> customKnowledgeBase = new ConcurrentHashMap<>();
    private final List<Map<String, String>> conversationLogs = Collections.synchronizedList(new ArrayList<>());

    @PostConstruct
    public void initBaselineTrainingData() {
        // Train AI with core baseline knowledge pairs
        customKnowledgeBase.put("what is school360", "School360 is a unified academic SaaS protocol designed for complete digital campus management. It handles admissions, digital QR ID badges, automated class timetables, support tickets, library catalogs, and real-time attendance.");
        customKnowledgeBase.put("how to login", "You can log in to School360 using your assigned username and password, or by clicking 'Scan ID' in the navigation bar to use your encrypted Digital Student ID QR Code.");
        customKnowledgeBase.put("how to enroll", "Navigate to the Course Enrollment tab on your Student Dashboard, select your preferred course, and submit an application. An Enrollment Officer will review and approve your admission code.");
        customKnowledgeBase.put("how to raise ticket", "Click on Support Center from your dashboard, create a new ticket with your query, and attach any relevant screenshots. Our Support Agents respond within 24 hours.");
        customKnowledgeBase.put("library rules", "Books can be borrowed for up to 14 days using your Student QR ID card. Contact Librarian Aathmika for reservations or renewals.");
        customKnowledgeBase.put("grading system", "School360 uses a standard GPA scale: A+ (90-100%), A (80-89%), B (70-79%), C (60-69%), F (<60%). Grades are updated live by subject teachers.");
    }

    /**
     * Dynamically train the AI Chatbot with new Q&A data pairs
     */
    public String trainKnowledge(String question, String answer) {
        if (question == null || question.trim().isEmpty() || answer == null || answer.trim().isEmpty()) {
            return "Invalid training input. Question and answer must not be empty.";
        }
        String key = question.toLowerCase().trim();
        customKnowledgeBase.put(key, answer.trim());
        logRepository.save(new SystemLog("AI Chatbot trained with new dataset for question: '" + question + "'", "INFO"));
        return "Successfully trained AI model with response for: '" + question + "'";
    }

    /**
     * Retrieve all trained custom knowledge entries
     */
    public Map<String, String> getTrainedKnowledge() {
        return new LinkedHashMap<>(customKnowledgeBase);
    }

    /**
     * Main AI Process & Response Pipeline with Real-Time Database Training Integration
     */
    public Map<String, Object> generateAIResponse(String userQuery, String userRole, String username, String pageContext) {
        String cleanQuery = (userQuery != null) ? userQuery.trim() : "";
        String lowerQuery = cleanQuery.toLowerCase();
        String role = (userRole != null) ? userRole.toUpperCase() : "GUEST";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", new Date());
        result.put("query", cleanQuery);
        result.put("userRole", role);

        String answer;

        // 1. Check Exact or Partial Match in Trained Custom Knowledge Base
        String trainedMatch = findTrainedMatch(lowerQuery);
        if (trainedMatch != null) {
            answer = "🧠 **[Trained Knowledge Match]**\n" + trainedMatch;
            result.put("source", "TRAINED_MODEL");
            result.put("response", answer);
            logInteraction(username, role, cleanQuery, answer);
            return result;
        }

        // 2. Real-Time System Database Queries (Live Data Extraction)
        if (isQueryAbout(lowerQuery, "student count", "how many student", "total student", "number of student")) {
            long count = studentRepository.count();
            answer = "📊 **Live Database Insight:** There are currently **" + count + " active students** registered in the School360 system.";
            result.put("source", "LIVE_DB_STUDENTS");
        } 
        else if (isQueryAbout(lowerQuery, "course list", "available course", "subjects", "what courses")) {
            List<Course> courses = courseRepository.findAll();
            if (courses.isEmpty()) {
                answer = "📚 **Live Database Insight:** No courses are currently listed in the directory.";
            } else {
                StringBuilder sb = new StringBuilder("📚 **Live Academic Courses Directory (" + courses.size() + " Total):**\n\n");
                for (Course c : courses) {
                    sb.append("• **").append(c.getCode()).append("**: ").append(c.getName());
                    if (c.getDescription() != null && !c.getDescription().isEmpty()) {
                        sb.append(" - ").append(c.getDescription());
                    }
                    sb.append("\n");
                }
                answer = sb.toString();
            }
            result.put("source", "LIVE_DB_COURSES");
        }
        else if (isQueryAbout(lowerQuery, "announcement", "news", "notice", "updates")) {
            List<Announcement> news = announcementRepository.findAll();
            if (news.isEmpty()) {
                answer = "📢 **Live Updates:** There are currently no broadcast announcements published.";
            } else {
                StringBuilder sb = new StringBuilder("📢 **Latest School360 Campus Announcements:**\n\n");
                int limit = Math.min(news.size(), 3);
                for (int i = 0; i < limit; i++) {
                    Announcement a = news.get(i);
                    sb.append("📌 **").append(a.getTitle()).append("**\n")
                      .append(a.getContent()).append("\n\n");
                }
                answer = sb.toString().trim();
            }
            result.put("source", "LIVE_DB_ANNOUNCEMENTS");
        }
        else if (isQueryAbout(lowerQuery, "timetable", "schedule", "class time", "next class", "lecture")) {
            List<Schedule> schedules = scheduleRepository.findAll();
            if (schedules.isEmpty()) {
                answer = "🗓️ **Live Timetable:** No active class schedules found for today.";
            } else {
                Map<Long, String> teacherNames = userRepository.findAll().stream()
                    .collect(Collectors.toMap(User::getId, u -> u.getFullName() != null ? u.getFullName() : "Faculty Teacher", (v1, v2) -> v1));
                StringBuilder sb = new StringBuilder("🗓️ **Live Class Schedule (" + schedules.size() + " Sessions):**\n\n");
                for (Schedule s : schedules) {
                    String teacherName = s.getTeacherId() != null ? teacherNames.getOrDefault(s.getTeacherId(), "Faculty Teacher") : "Faculty Teacher";
                    sb.append("• **").append(s.getModuleName()).append("** (").append(s.getRoom() != null ? s.getRoom() : "Room").append(")\n")
                      .append("  🕒 ").append(s.getStartTime()).append(" - ").append(s.getEndTime())
                      .append(" | Teacher: ").append(teacherName).append("\n");
                }
                answer = sb.toString();
            }
            result.put("source", "LIVE_DB_SCHEDULES");
        }
        else if (isQueryAbout(lowerQuery, "user count", "how many user", "system user", "who works")) {
            long totalUsers = userRepository.count();
            answer = "👥 **Live System Metrics:** There are **" + totalUsers + " registered accounts** across all administrative and student roles in School360.";
            result.put("source", "LIVE_DB_USERS");
        }
        else if (isQueryAbout(lowerQuery, "ticket", "support status", "open issues", "helpdesk")) {
            List<Ticket> tickets = ticketRepository.findAll();
            long openCount = tickets.stream().filter(t -> "OPEN".equalsIgnoreCase(t.getStatus())).count();
            long pendingCount = tickets.stream().filter(t -> "PENDING".equalsIgnoreCase(t.getStatus())).count();
            long resolvedCount = tickets.stream().filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus())).count();
            answer = "🎫 **Live Helpdesk Stats:**\n" +
                     "• Total Tickets: **" + tickets.size() + "**\n" +
                     "• Open: **" + openCount + "** | Pending: **" + pendingCount + "** | Resolved: **" + resolvedCount + "**\n\n" +
                     "You can file a new support ticket directly from the **Support Center** tab.";
            result.put("source", "LIVE_DB_TICKETS");
        }
        else if (isQueryAbout(lowerQuery, "enrollment request", "admission request", "pending admission")) {
            List<EnrollmentRequest> reqs = enrollmentRequestRepository.findAll();
            long pending = reqs.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getEoStatus())).count();
            answer = "🎓 **Enrollment Center Insights:** There are **" + pending + " pending admission requests** waiting for Enrollment Officer sign-off out of " + reqs.size() + " total submissions.";
            result.put("source", "LIVE_DB_ENROLLMENT");
        }
        else if (isQueryAbout(lowerQuery, "librarian", "library", "aathmika", "book")) {
            answer = "📖 **School360 Central Library Service:**\n" +
                     "• Manager: **Librarian Aathmika** (`aathu11`)\n" +
                     "• Location: Academic Block Building B, Level 2\n" +
                     "• Hours: Monday - Friday (08:00 AM - 05:00 PM)\n" +
                     "• Note: Scan your Digital Student QR ID to instantly check out books and digital journals.";
            result.put("source", "LIBRARY_DATA");
        }
        // 3. Mathematical & Logic Calculation Engine
        else if (isMathExpression(lowerQuery)) {
            answer = evaluateMathExpression(lowerQuery);
            result.put("source", "AI_MATH_ENGINE");
        }
        // 4. Role-Based Specialized Assistance
        else if (isQueryAbout(lowerQuery, "role", "my permission", "what can i do")) {
            answer = getRoleGuidance(role);
            result.put("source", "ROLE_GUIDANCE");
        }
        // 5. Intelligent NLP Natural Language QA Fallback Engine
        else {
            answer = generateGeneralKnowledgeAnswer(lowerQuery, role);
            result.put("source", "AI_GENERAL_KNOWLEDGE");
        }

        result.put("response", answer);
        logInteraction(username, role, cleanQuery, answer);
        return result;
    }

    private boolean isQueryAbout(String query, String... keywords) {
        for (String kw : keywords) {
            if (query.contains(kw)) return true;
        }
        return false;
    }

    private String findTrainedMatch(String query) {
        for (Map.Entry<String, String> entry : customKnowledgeBase.entrySet()) {
            if (query.equals(entry.getKey()) || query.contains(entry.getKey()) || entry.getKey().contains(query)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private boolean isMathExpression(String query) {
        return query.matches(".*\\d+\\s*[\\+\\-\\*/%]\\s*\\d+.*") || query.startsWith("what is ") && query.matches(".*\\d+.*");
    }

    private String evaluateMathExpression(String query) {
        try {
            Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)\\s*([\\+\\-\\*/%])\\s*(\\d+(\\.\\d+)?)");
            Matcher matcher = pattern.matcher(query);
            if (matcher.find()) {
                double num1 = Double.parseDouble(matcher.group(1));
                String op = matcher.group(3);
                double num2 = Double.parseDouble(matcher.group(4));
                double res = 0;
                switch (op) {
                    case "+": res = num1 + num2; break;
                    case "-": res = num1 - num2; break;
                    case "*": res = num1 * num2; break;
                    case "/": res = (num2 != 0) ? num1 / num2 : Double.NaN; break;
                    case "%": res = num1 % num2; break;
                }
                if (Double.isNaN(res)) {
                    return "🧮 **Math Engine:** Error: Division by zero is undefined.";
                }
                return "🧮 **AI Math Engine Calculation:**\n`" + num1 + " " + op + " " + num2 + " = " + (res % 1 == 0 ? (long)res : res) + "`";
            }
        } catch (Exception ignored) {}
        return "🧮 I can perform mathematical calculations! Please format like: `25 * 4` or `150 / 3`.";
    }

    private String getRoleGuidance(String role) {
        switch (role) {
            case "STUDENT":
                return "🎓 **Student Role Overview:** You can view assigned modules, check lecture schedules, monitor attendance, track grades, access library services, and open helpdesk support tickets.";
            case "TEACHER":
                return "👨‍🏫 **Teacher Role Overview:** You can manage course modules, schedule/reschedule lectures, mark live student attendance, record assessment grades, and send class alerts.";
            case "ADMIN":
                return "⚙️ **Admin Role Overview:** Full platform governance. Manage system users, inspect analytics charts, handle ticket escalations, execute database maintenance, and monitor system security logs.";
            case "SUPPORT":
                return "🎧 **Support Agent Role Overview:** Manage student helpdesk tickets, request verification screenshots, update issue status, and escalate unresolved cases to administrators.";
            case "ENROLLMENT":
                return "📋 **Enrollment Officer Overview:** Review incoming student admission forms, approve class assignments, generate official admission numbers, and unlock academic modules.";
            case "LIBRARIAN":
                return "📚 **Librarian Role Overview:** Scan student QR IDs for book issuance, manage catalog inventory, register new titles, and track overdue material returns.";
            default:
                return "👤 **Guest Visitor:** Explore School360 features, review admissions criteria, or log in to access your designated role portal.";
        }
    }

    private String generateGeneralKnowledgeAnswer(String query, String role) {
        // Broad General Knowledge & Academic Assistant Capabilities
        if (query.contains("hello") || query.contains("hi") || query.contains("hey")) {
            return "👋 Hello! I am your **School360 Intelligent AI Assistant**. I am continuously learning from campus data to answer any questions about admissions, schedules, courses, support tickets, library material, or general academic topics. How can I assist you today?";
        }
        if (query.contains("thank")) {
            return "😊 You're very welcome! I'm here 24/7 whenever you need assistance with School360.";
        }
        if (query.contains("who are you") || query.contains("what can you do")) {
            return "🤖 **About Me:** I am the real-time trained AI Assistant for School360. I connect directly to the school's live database to provide real-time information on students, courses, timetables, and tickets, as well as answer general knowledge and academic questions!";
        }
        if (query.contains("python") || query.contains("coding") || query.contains("programming") || query.contains("java") || query.contains("html")) {
            return "💻 **Computer Science & Software Assist:**\nSchool360 is powered by modern Java Spring Boot REST services, SQL databases, and responsive HTML5/JS interfaces. If you need coding assistance or explanation of algorithms, feel free to ask!";
        }
        if (query.contains("science") || query.contains("math") || query.contains("physics") || query.contains("chemistry")) {
            return "🔬 **Academic & STEM Support:** I can help explain core concepts in Mathematics, Computer Science, Physics, Chemistry, and General Studies. Feel free to enter any problem or concept you'd like to explore.";
        }
        if (query.contains("password") || query.contains("reset") || query.contains("forgot")) {
            return "🔐 **Security & Password Recovery:**\nTo reset your credentials, navigate to the Login modal and click 'Forgot Password', or visit the Receptionist desk with your physical student/staff ID badge for an administrative password reset token.";
        }
        if (query.contains("fee") || query.contains("payment") || query.contains("tuition")) {
            return "💳 **Tuition & Billing:** Tuition fee receipts and payment status can be tracked under your Student Finance section. Contact the Administrative Officer for payment installment arrangements.";
        }

        // Intelligently formatted general fallback
        return "🤖 **AI Intelligence Assist:**\n" +
               "I've analyzed your question regarding *\"" + query + "\"*.\n\n" +
               "Here is how I can help:\n" +
               "1. If this is a school query, try asking about **\"available courses\"**, **\"class schedule\"**, **\"student count\"**, **\"announcements\"**, or **\"support status\"** for real-time live database updates.\n" +
               "2. If you want to train me on custom answers for this question, administrators can submit training Q&As via the AI Training Portal!\n" +
               "3. Feel free to rephrase or ask any other academic, STEM, or system operational question!";
    }

    private void logInteraction(String username, String role, String query, String answer) {
        Map<String, String> log = new HashMap<>();
        log.put("timestamp", new Date().toString());
        log.put("user", (username != null) ? username : "GUEST");
        log.put("role", role);
        log.put("query", query);
        log.put("answer", answer);
        conversationLogs.add(log);
        if (conversationLogs.size() > 500) {
            conversationLogs.remove(0);
        }
    }
}
