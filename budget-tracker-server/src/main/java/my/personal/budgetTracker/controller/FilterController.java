package my.personal.budgetTracker.controller;

import java.util.List;
import java.time.LocalDate;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import my.personal.budgetTracker.dto.ExpenseDTO;
import my.personal.budgetTracker.dto.FilterDTO;
import my.personal.budgetTracker.dto.IncomeDTO;
import my.personal.budgetTracker.service.ExpenseService;
import my.personal.budgetTracker.service.IncomeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilterDTO filterDTO) {
        LocalDate startDate = filterDTO.getStartDate ()!= null ? filterDTO.getStartDate() : LocalDate.MIN;
        LocalDate endDate = filterDTO.getEndDate() != null ? filterDTO.getEndDate() : LocalDate.now();
        String keyword = filterDTO.getKeyword() != null ? filterDTO.getKeyword() : "";
        String sortField = filterDTO.getSortField() != null ? filterDTO.getSortField() : "date";
        Sort.Direction sortOrder = filterDTO.getSortOrder() != null ? Sort.Direction.fromString(filterDTO.getSortOrder()) : Sort.Direction.DESC;
        Sort sort = Sort.by(sortOrder, sortField);
        if ("income".equalsIgnoreCase(filterDTO.getType())) {
            List<IncomeDTO> filteredIncomes = incomeService.filterIncomes(startDate, endDate, keyword, sort);
            return ResponseEntity.ok(filteredIncomes);
        } else if ("expense".equalsIgnoreCase(filterDTO.getType())) {
            List<ExpenseDTO> filteredExpenses = expenseService.filterExpenses(startDate, endDate, keyword, sort);
            return ResponseEntity.ok(filteredExpenses);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid type. Must be 'income' or 'expense'.");
        }
    }
}
