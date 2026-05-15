package my.personal.budgetTracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import my.personal.budgetTracker.entity.CategoryEntity;
import my.personal.budgetTracker.entity.ExpenseEntity;
import my.personal.budgetTracker.entity.ProfileEntity;
import my.personal.budgetTracker.repository.CategoryRepository;
import my.personal.budgetTracker.repository.ExpenseRepository;
import my.personal.budgetTracker.dto.ExpenseDTO;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;

    public ExpenseDTO addExpense(ExpenseDTO expenseDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(expenseDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        ExpenseEntity newExpense = toEntity(expenseDTO, profile, category);
        newExpense = expenseRepository.save(newExpense);
        return toDTO(newExpense);
    }

    public List<ExpenseDTO> getCurrentMonthExpenses() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> currentMonthExpenses = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return currentMonthExpenses.stream().map(this::toDTO).toList();
    }

    public void deleteExpense(Long expenseId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity deletedExpense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        if (!deletedExpense.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized to delete this expense");
        }
        expenseRepository.delete(deletedExpense);
    }

    public List<ExpenseDTO> getLatest5ExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> latestExpenses = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return latestExpenses.stream().map(this::toDTO).toList();
    }

    public BigDecimal getTotalExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal totalExpenses = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
    }

    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> filteredExpenses = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return filteredExpenses.stream().map(this::toDTO).toList();
    }

    private ExpenseEntity toEntity(ExpenseDTO expenseDTO, ProfileEntity profile, CategoryEntity category) {
        return ExpenseEntity.builder()
                .name(expenseDTO.getName())
                .icon(expenseDTO.getIcon())
                .amount(expenseDTO.getAmount())
                .date(expenseDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private ExpenseDTO toDTO(ExpenseEntity expenseEntity) {
        return ExpenseDTO.builder()
                .id(expenseEntity.getId())
                .name(expenseEntity.getName())
                .icon(expenseEntity.getIcon())
                .date(expenseEntity.getDate())
                .amount(expenseEntity.getAmount())
                .categoryId(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getId() : null)
                .categoryName(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getName() : "N/A")
                .createdAt(expenseEntity.getCreatedAt())
                .updatedAt(expenseEntity.getUpdatedAt())
                .build();
    }
}
