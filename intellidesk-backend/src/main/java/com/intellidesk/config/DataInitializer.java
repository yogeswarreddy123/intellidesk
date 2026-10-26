package com.intellidesk.config;

import com.intellidesk.entity.*;
import com.intellidesk.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           CategoryRepository categoryRepository,
                           TicketRepository ticketRepository,
                           CommentRepository commentRepository,
                           KnowledgeArticleRepository articleRepository,
                           AuditLogRepository auditLogRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.categoryRepository = categoryRepository;
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            logger.info("Database already seeded. Skipping initial data setup.");
            return;
        }

        logger.info("Initializing IntelliDesk sample data...");

        // 1. Departments
        Map<String, Department> depts = new HashMap<>();
        String[][] deptData = {
                {"IT Support", "Tier 1 and Tier 2 general end-user support and desktop assistance"},
                {"Network", "Corporate Wi-Fi, switches, routers, firewalls, and VPN infrastructure"},
                {"Security", "Cybersecurity, threat detection, vulnerability analysis, and access reviews"},
                {"Database", "Production MySQL, PostgreSQL, and enterprise database servers"},
                {"Application Support", "Internal CRM, ERP, and enterprise software application support"},
                {"HR", "Human Resources, onboarding, and employee lifecycle management"},
                {"Finance", "Finance, payroll, billing, and accounting operations"}
        };

        for (String[] d : deptData) {
            Department dept = new Department(d[0], d[1]);
            depts.put(d[0], departmentRepository.save(dept));
        }

        // 2. Categories
        Map<String, Category> cats = new HashMap<>();
        String[][] catData = {
                {"Hardware", "Physical computing devices, laptops, monitors, accessories, and printers"},
                {"Software", "Operating system, licensed productivity software, and standard desktop utilities"},
                {"Network", "Corporate Wi-Fi, Ethernet, VPN connectivity, and DNS lookups"},
                {"Email", "Outlook, Exchange mailbox, calendars, distribution lists, and spam"},
                {"Access", "Account permissions, password resets, MFA tokens, and SSO privileges"},
                {"Security", "Malware alerts, phishing reports, certificates, and compliance checks"},
                {"Application", "Enterprise web portals, CRM, internal API services, and microservices"},
                {"Database", "SQL queries, database access, backup verification, and data migrations"}
        };

        for (String[] c : catData) {
            Category cat = new Category(c[0], c[1]);
            cats.put(c[0], categoryRepository.save(cat));
        }

        // 3. Users
        // Admin
        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@intellidesk.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setPhone("+1-555-0100");
        admin.setDepartment(depts.get("IT Support"));
        admin.setActive(true);
        admin = userRepository.save(admin);

        // IT Agent 1
        User agent1 = new User();
        agent1.setFirstName("Alex");
        agent1.setLastName("Vance");
        agent1.setEmail("agent@intellidesk.com");
        agent1.setPassword(passwordEncoder.encode("agent123"));
        agent1.setRole(Role.IT_AGENT);
        agent1.setPhone("+1-555-0101");
        agent1.setDepartment(depts.get("IT Support"));
        agent1.setActive(true);
        agent1 = userRepository.save(agent1);

        // IT Agent 2
        User agent2 = new User();
        agent2.setFirstName("David");
        agent2.setLastName("Chen");
        agent2.setEmail("agent2@intellidesk.com");
        agent2.setPassword(passwordEncoder.encode("agent123"));
        agent2.setRole(Role.IT_AGENT);
        agent2.setPhone("+1-555-0102");
        agent2.setDepartment(depts.get("Network"));
        agent2.setActive(true);
        agent2 = userRepository.save(agent2);

        // Employee 1
        User emp1 = new User();
        emp1.setFirstName("John");
        emp1.setLastName("Doe");
        emp1.setEmail("employee@intellidesk.com");
        emp1.setPassword(passwordEncoder.encode("employee123"));
        emp1.setRole(Role.EMPLOYEE);
        emp1.setPhone("+1-555-0103");
        emp1.setDepartment(depts.get("Finance"));
        emp1.setActive(true);
        emp1 = userRepository.save(emp1);

        // Employee 2
        User emp2 = new User();
        emp2.setFirstName("Sarah");
        emp2.setLastName("Connor");
        emp2.setEmail("sarah@intellidesk.com");
        emp2.setPassword(passwordEncoder.encode("employee123"));
        emp2.setRole(Role.EMPLOYEE);
        emp2.setPhone("+1-555-0104");
        emp2.setDepartment(depts.get("HR"));
        emp2.setActive(true);
        emp2 = userRepository.save(emp2);

        // 4. Knowledge Base Articles
        KnowledgeArticle[] articles = {
                new KnowledgeArticle(
                        "How to Reset Password",
                        "If you have forgotten your IntelliDesk or domain password, follow these simple steps:\n\n" +
                                "1. Navigate to https://account.company.com/reset\n" +
                                "2. Enter your corporate email address (@intellidesk.com).\n" +
                                "3. Confirm the push notification sent to your registered Microsoft Authenticator app.\n" +
                                "4. Choose a new password that contains at least 10 characters, 1 uppercase letter, 1 number, and 1 symbol.\n" +
                                "5. Wait 60 seconds for synchronization across all enterprise systems before signing in again.\n\n" +
                                "Note: If your account is locked due to repeated incorrect password attempts, please contact your local IT Service Desk.",
                        cats.get("Access"),
                        "password, reset, credentials, unlock, login, forgot",
                        admin
                ),
                new KnowledgeArticle(
                        "How to Connect to VPN",
                        "Connecting to the corporate network via GlobalProtect / OpenVPN:\n\n" +
                                "1. Launch the VPN client on your corporate laptop.\n" +
                                "2. In the Portal address field, type: `vpn.intellidesk.com`.\n" +
                                "3. Click Connect and enter your corporate Single Sign-On (SSO) credentials.\n" +
                                "4. Approve the 2-Factor Authentication prompt on your mobile phone.\n" +
                                "5. Ensure the shield icon turns solid Green indicating a secure tunnel.\n\n" +
                                "Troubleshooting: If connection hangs at 98%, disconnect your Wi-Fi, wait 10 seconds, reconnect to Wi-Fi, and re-launch the VPN client.",
                        cats.get("Network"),
                        "vpn, remote, connect, network, portal, tunnel",
                        admin
                ),
                new KnowledgeArticle(
                        "Wi-Fi Troubleshooting",
                        "Issues connecting to office Wi-Fi networks:\n\n" +
                                "1. Ensure Wi-Fi is toggled ON on your laptop.\n" +
                                "2. Connect to the SSID named `IntelliDesk-Corp` (do not use Guest Wi-Fi for work resources).\n" +
                                "3. If prompted, select 'Connect using a certificate' or enter your domain username & password.\n" +
                                "4. If you see 'No Internet, Secured', forget the network in Windows Settings > Network & Internet > Wi-Fi > Manage known networks, and reconnect.\n" +
                                "5. Restart your computer if the problem persists.",
                        cats.get("Network"),
                        "wifi, wi-fi, network, wireless, internet, connection",
                        agent1
                ),
                new KnowledgeArticle(
                        "Email Configuration & Outlook Sync",
                        "Steps to resolve Microsoft Outlook synchronization and mailbox errors:\n\n" +
                                "1. Check your Outlook status bar in the bottom right corner (it should say 'Connected to Microsoft Exchange').\n" +
                                "2. If it states 'Disconnected' or 'Need Password', click on the banner and re-authenticate.\n" +
                                "3. To force sync: Click the 'Send / Receive' tab > 'Send/Receive All Folders'.\n" +
                                "4. If Outlook fails to open: Press Win+R, type `outlook.exe /safe` and press Enter to test safe mode.\n" +
                                "5. For webmail access during desktop issues, use: https://outlook.office.com",
                        cats.get("Email"),
                        "email, outlook, exchange, sync, mailbox, messages",
                        agent1
                ),
                new KnowledgeArticle(
                        "Printer Troubleshooting Guide",
                        "How to resolve common printer connectivity and paper jam issues:\n\n" +
                                "1. Ensure the printer has paper and toner cartridges are properly seated.\n" +
                                "2. Clear any active print queues: Open Settings > Printers & Scanners > select printer > Open queue > Cancel all documents.\n" +
                                "3. For network printers: Ensure you are connected to the corporate LAN or VPN.\n" +
                                "4. Re-add printer: Delete the existing printer entry, click 'Add device', and search by the printer IP badge displayed on the device label.\n" +
                                "5. If paper is jammed, follow the labeled green levers inside door 1 and door 2 to pull out trapped paper smoothly.",
                        cats.get("Hardware"),
                        "printer, print, hardware, paper jam, offline, queue",
                        agent2
                ),
                new KnowledgeArticle(
                        "How to Request Software Access",
                        "Standard procedure for requesting specialized enterprise software licenses:\n\n" +
                                "1. Check the Self-Service Software Portal (Company Portal app in Windows Start Menu).\n" +
                                "2. Common apps (Slack, Zoom, Notepad++, VS Code, Chrome) can be installed without IT approval.\n" +
                                "3. For paid licenses (Tableau, Adobe Creative Cloud, JetBrains, AutoCAD), submit a ticket under Category: 'Access' or 'Software'.\n" +
                                "4. Include your Manager's email approval in the ticket description.\n" +
                                "5. Processing time is usually 24 to 48 business hours.",
                        cats.get("Software"),
                        "software, license, request, install, access, apps",
                        admin
                )
        };

        for (KnowledgeArticle article : articles) {
            articleRepository.save(article);
        }

        // 5. Sample Tickets
        Ticket t1 = new Ticket();
        t1.setTicketNumber("INC-10001");
        t1.setTitle("Unable to connect to office Wi-Fi on 3rd floor");
        t1.setDescription("My laptop drops Wi-Fi connection constantly when working near the conference rooms on the 3rd floor. Other networks appear fine.");
        t1.setCategory(cats.get("Network"));
        t1.setPriority(Priority.MEDIUM);
        t1.setStatus(TicketStatus.OPEN);
        t1.setDepartment(depts.get("Finance"));
        t1.setCreatedBy(emp1);
        t1 = ticketRepository.save(t1);

        Ticket t2 = new Ticket();
        t2.setTicketNumber("INC-10002");
        t2.setTitle("VPN connection error code 412");
        t2.setDescription("When trying to connect via GlobalProtect from home, I get error code 412 authentication failed despite entering correct password.");
        t2.setCategory(cats.get("Network"));
        t2.setPriority(Priority.HIGH);
        t2.setStatus(TicketStatus.IN_PROGRESS);
        t2.setDepartment(depts.get("Finance"));
        t2.setCreatedBy(emp1);
        t2.setAssignedTo(agent1);
        t2 = ticketRepository.save(t2);

        Ticket t3 = new Ticket();
        t3.setTicketNumber("INC-10003");
        t3.setTitle("Monitor flickers and turns black intermittently");
        t3.setDescription("The external Dell 27-inch monitor flickers every few minutes. I swapped the HDMI cable but the issue persists.");
        t3.setCategory(cats.get("Hardware"));
        t3.setPriority(Priority.LOW);
        t3.setStatus(TicketStatus.RESOLVED);
        t3.setDepartment(depts.get("HR"));
        t3.setCreatedBy(emp2);
        t3.setAssignedTo(agent2);
        t3.setResolvedAt(LocalDateTime.now().minusHours(2));
        t3 = ticketRepository.save(t3);

        Ticket t4 = new Ticket();
        t4.setTicketNumber("INC-10004");
        t4.setTitle("Production Billing Database latency spike");
        t4.setDescription("Transactions are taking over 15 seconds to execute. High connection pool saturation detected on DB cluster.");
        t4.setCategory(cats.get("Database"));
        t4.setPriority(Priority.CRITICAL);
        t4.setStatus(TicketStatus.IN_PROGRESS);
        t4.setDepartment(depts.get("Database"));
        t4.setCreatedBy(emp1);
        t4.setAssignedTo(agent1);
        t4 = ticketRepository.save(t4);

        Ticket t5 = new Ticket();
        t5.setTicketNumber("INC-10005");
        t5.setTitle("Request Adobe Acrobat Pro license for HR onboarding");
        t5.setDescription("Need Adobe Acrobat Pro license to prepare signed employee employment contracts and offer letters. Manager approval attached.");
        t5.setCategory(cats.get("Software"));
        t5.setPriority(Priority.MEDIUM);
        t5.setStatus(TicketStatus.CLOSED);
        t5.setDepartment(depts.get("HR"));
        t5.setCreatedBy(emp2);
        t5.setAssignedTo(agent2);
        t5.setResolvedAt(LocalDateTime.now().minusDays(1));
        t5.setClosedAt(LocalDateTime.now().minusHours(5));
        t5 = ticketRepository.save(t5);

        // 6. Sample Comments
        commentRepository.save(new Comment("I checked the access points on floor 3, inspecting AP-04.", t1, agent1));
        commentRepository.save(new Comment("Thank you Alex, please let me know when you test it.", t1, emp1));

        commentRepository.save(new Comment("Hi John, could you please confirm if your MFA token was approved within 30 seconds?", t2, agent1));
        commentRepository.save(new Comment("Yes, I approved the prompt, but the VPN app immediately closed with code 412.", t2, emp1));

        commentRepository.save(new Comment("Replaced the USB-C dock unit. Monitor is operating normally now.", t3, agent2));
        commentRepository.save(new Comment("Confirmed resolved, screen is stable now! Thank you David.", t3, emp2));

        // 7. Sample Audit Logs
        auditLogRepository.save(new AuditLog(admin, "SYSTEM_INIT", "SYSTEM", "0", "System initialized with initial sample data"));
        auditLogRepository.save(new AuditLog(emp1, "CREATE_TICKET", "TICKET", "INC-10001", "Created ticket INC-10001: Unable to connect to office Wi-Fi"));
        auditLogRepository.save(new AuditLog(emp1, "CREATE_TICKET", "TICKET", "INC-10002", "Created ticket INC-10002: VPN connection error code 412"));
        auditLogRepository.save(new AuditLog(agent1, "ASSIGN_TICKET", "TICKET", "INC-10002", "Assigned ticket INC-10002 to Alex Vance"));
        auditLogRepository.save(new AuditLog(agent2, "RESOLVE_TICKET", "TICKET", "INC-10003", "Resolved ticket INC-10003"));

        logger.info("IntelliDesk sample data successfully initialized!");
    }
}
