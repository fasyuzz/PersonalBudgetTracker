package my.personal.budgetTracker.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Stream.concat;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import my.personal.budgetTracker.dto.ExpenseDTO;
import my.personal.budgetTracker.dto.IncomeDTO;
import my.personal.budgetTracker.dto.RecentTransactionDTO;
import my.personal.budgetTracker.entity.ProfileEntity;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProfileService profileService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    public Map<String, Object> getDashboardData() {
        ProfileEntity profile = profileService.getCurrentProfile();
        Map<String, Object> dashboardData = new LinkedHashMap<>();
        List<IncomeDTO> latestIncomes = incomeService.getLatest5IncomesForCurrentUser();
        List<ExpenseDTO> latestExpenses = expenseService.getLatest5ExpensesForCurrentUser();
        List<RecentTransactionDTO> latestTransactions = concat(latestIncomes.stream().map(i
                -> RecentTransactionDTO.builder()
                        .id(i.getId())
                        .profileId(profile.getId())
                        .icon(i.getIcon())
                        .name(i.getName())
                        .amount(i.getAmount())
                        .date(i.getDate())
                        .createdAt(i.getCreatedAt())
                        .updatedAt(i.getUpdatedAt())
                        .type("income")
                        .build()
        ), latestExpenses.stream().map(e
                -> RecentTransactionDTO.builder()
                        .id(e.getId())
                        .profileId(profile.getId())
                        .icon(e.getIcon())
                        .name(e.getName())
                        .amount(e.getAmount())
                        .date(e.getDate())
                        .createdAt(e.getCreatedAt())
                        .updatedAt(e.getUpdatedAt())
                        .type("expense")
                        .build()
        )).sorted((a, b) -> {
            int cmp = b.getDate().compareTo(a.getDate());
            if (cmp == 0 && b.getCreatedAt() != null && a.getCreatedAt() != null) {
                cmp = b.getCreatedAt().compareTo(a.getCreatedAt());
            }
            return cmp;
        }).collect(Collectors.toList());
        dashboardData.put("totalBalance",
                incomeService.getTotalIncomesForCurrentUser()
                        .subtract(expenseService.getTotalExpensesForCurrentUser()));
        dashboardData.put("totalIncomes",
                incomeService.getTotalIncomesForCurrentUser());
        dashboardData.put("totalExpenses",
                expenseService.getTotalExpensesForCurrentUser());
        dashboardData.put("latest5Expenses", latestExpenses);
        dashboardData.put("latest5Incomes", latestIncomes);
        dashboardData.put("latestTransactions", latestTransactions);
        return dashboardData;
    }
}
