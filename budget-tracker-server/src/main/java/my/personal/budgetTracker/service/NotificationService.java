package my.personal.budgetTracker.service;

import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.personal.budgetTracker.dto.ExpenseDTO;
import my.personal.budgetTracker.entity.ProfileEntity;
import my.personal.budgetTracker.repository.ProfileRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${app.frontEndUrl}")
    private String frontEndUrl;

    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Kuala_Lumpur") // Every day at 10 PM
    public void sendDailyIncomeExpenseReminder() throws MessagingException {
        log.info("Job Started: sendDailyIncomeExpenseReminder()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile : profiles) {
            try {
                String body = """
                            <html>
                            <body style="
                                margin:0;
                                padding:20px;
                                background-color:#f4f6f9;
                                font-family:Arial,sans-serif;
                            ">

                                <div style="
                                    max-width:650px;
                                    margin:auto;
                                    background-color:white;
                                    border-radius:10px;
                                    padding:35px;
                                    box-shadow:0 2px 8px rgba(0,0,0,0.1);
                                ">

                                    <h2 style="
                                        color:#2c3e50;
                                        margin-top:0;
                                    ">
                                        Daily Income & Expense Reminder
                                    </h2>

                                    <p style="
                                        font-size:16px;
                                        color:#444;
                                        line-height:1.6;
                                    ">
                                        Hello <strong>%s</strong>,
                                    </p>

                                    <p style="
                                        font-size:15px;
                                        color:#555;
                                        line-height:1.8;
                                    ">
                                        This is your daily reminder to review your
                                        income and expenses for today.
                                    </p>

                                    <div style="
                                        margin-top:30px;
                                        margin-bottom:30px;
                                    ">
                                        <a href="%s/dashboard"
                                        style="
                                                background-color:#3498db;
                                                color:white;
                                                padding:12px 22px;
                                                text-decoration:none;
                                                border-radius:6px;
                                                font-weight:bold;
                                                display:inline-block;
                                        ">
                                            View Dashboard
                                        </a>
                                    </div>

                                    <hr style="
                                        border:none;
                                        border-top:1px solid #e0e0e0;
                                        margin-top:30px;
                                        margin-bottom:20px;
                                    ">

                                    <p style="
                                        color:#777;
                                        font-size:14px;
                                        line-height:1.6;
                                    ">
                                        Best regards,<br>
                                        Personal Budget Tracker Team
                                    </p>

                                </div>

                            </body>
                            </html>
                            """
                        .formatted(
                                profile.getFullName(),
                                frontEndUrl
                        );
                emailService.sendHTMLEmail(profile.getEmail(), "Daily Income and Expense Reminder", body);
            } catch (RuntimeException e) {
                log.error("Failed to send reminder email to {}: {}", profile.getEmail(), e.getMessage());
            }
        }
        log.info("Job Finished: sendDailyIncomeExpenseReminder()");
    }

    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Kuala_Lumpur") // Every day at 11 PM
    public void sendDailyExpenseSummary() throws MessagingException {
        log.info("Job Started: sendDailyExpenseSummary()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile : profiles) {
            List<ExpenseDTO> todayExpenses = expenseService.getExpensesForUserOnDate(profile.getId(), LocalDate.now());
            // Process the expenses and send summary email
            if (!todayExpenses.isEmpty()) {
                StringBuilder table = new StringBuilder();
                table.append("<table style='width:100%;border-collapse:collapse;font-family:Arial,sans-serif;'>");
                table.append("<thead>")
                        .append("<tr>")
                        .append("<th colspan='4' style='background-color:#f2f2f2;border:1px solid #ddd;padding:8px;text-align:center;'>Today's Expenses</th>")
                        .append("</tr>")
                        .append("<tr>")
                        .append("<th style='border:1px solid #ddd;padding:8px;text-align:center;background-color:#3498db;color:white;width:50px;'>No.</th>")
                        .append("<th style='border:1px solid #ddd;padding:8px;text-align:left;background-color:#3498db;color:white;'>Category</th>")
                        .append("<th style='border:1px solid #ddd;padding:8px;text-align:right;background-color:#3498db;color:white;'>Amount</th>")
                        .append("<th style='border:1px solid #ddd;padding:8px;text-align:left;background-color:#3498db;color:white;'>Date</th>")
                        .append("</tr>")
                        .append("</thead>");
                int index = 1;
                for (ExpenseDTO expense : todayExpenses) {
                    table.append("<tr>")
                            .append("<td style='border:1px solid #ddd;padding:8px;text-align:left;'>").append(index++).append("</td>")
                            .append("<td style='border:1px solid #ddd;padding:8px;text-align:left;'>").append(expense.getCategoryName() != null ? expense.getCategoryName() : "N/A").append("</td>")
                            .append("<td style='border:1px solid #ddd;padding:8px;text-align:right;'>RM").append(expense.getAmount()).append("</td>")
                            .append("<td style='border:1px solid #ddd;padding:8px;text-align:left;'>").append(expense.getDate()).append("</td>")
                            .append("</tr>");
                }
                table.append("</table>");
                BigDecimal totalExpenses = todayExpenses.stream()
                        .map(ExpenseDTO::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                String body = """
                                Hello %s,<br><br>

                                Here is your expense summary for today:<br><br>

                                %s

                                <br><br>
                                Total Expenses: RM%s<br><br>

                                <br><br>
                                Best regards,<br>
                                Your Personal Budget Tracker Team
                                """
                        .formatted(
                                profile.getFullName(),
                                table.toString(),
                                totalExpenses
                        );
                try {
                    emailService.sendHTMLEmail(profile.getEmail(), "Daily Expense Summary", body);
                } catch (RuntimeException e) {
                    log.error("Failed to send expense summary email to {}: {}", profile.getEmail(), e.getMessage());
                }
            }
        }
        log.info("Job Finished: sendDailyExpenseSummary()");
    }
}
