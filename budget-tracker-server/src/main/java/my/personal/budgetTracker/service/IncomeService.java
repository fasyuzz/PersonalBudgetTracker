package my.personal.budgetTracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import my.personal.budgetTracker.dto.IncomeDTO;
import my.personal.budgetTracker.entity.CategoryEntity;
import my.personal.budgetTracker.entity.IncomeEntity;
import my.personal.budgetTracker.entity.ProfileEntity;
import my.personal.budgetTracker.repository.CategoryRepository;
import my.personal.budgetTracker.repository.IncomeRepository;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;

    public IncomeDTO addIncome(IncomeDTO incomeDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(incomeDTO.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));
        IncomeEntity newIncome = toEntity(incomeDTO, profile, category);
        newIncome = incomeRepository.save(newIncome);
        return toDTO(newIncome);
    }

    public List<IncomeDTO> getCurrentMonthIncomes() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<IncomeEntity> currentMonthIncomes = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return currentMonthIncomes.stream().map(this::toDTO).toList();
    }

    public void deleteIncome(Long incomeId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity deletedIncome = incomeRepository.findById(incomeId)
            .orElseThrow(() -> new RuntimeException("Income not found"));
        if (!deletedIncome.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized to delete this income");
        }
        incomeRepository.delete(deletedIncome);
    }

    public List<IncomeDTO> getLatest5IncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> latestIncomes = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return latestIncomes.stream().map(this::toDTO).toList();
    }

    public BigDecimal getTotalIncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal totalIncomes = incomeRepository.findTotalIncomeByProfileId(profile.getId());
        return totalIncomes != null ? totalIncomes : BigDecimal.ZERO;
    }

    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> filteredIncomes = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return filteredIncomes.stream().map(this::toDTO).toList();
    }

    private IncomeEntity toEntity(IncomeDTO incomeDTO, ProfileEntity profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .name(incomeDTO.getName())
                .icon(incomeDTO.getIcon())
                .amount(incomeDTO.getAmount())
                .date(incomeDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private IncomeDTO toDTO(IncomeEntity incomeEntity) {
        return IncomeDTO.builder()
                .id(incomeEntity.getId())
                .name(incomeEntity.getName())
                .icon(incomeEntity.getIcon())
                .date(incomeEntity.getDate())
                .amount(incomeEntity.getAmount())
                .categoryId(incomeEntity.getCategory()!=null ? incomeEntity.getCategory().getId() : null)
                .categoryName(incomeEntity.getCategory()!=null ? incomeEntity.getCategory().getName() : "N/A")
                .createdAt(incomeEntity.getCreatedAt())
                .updatedAt(incomeEntity.getUpdatedAt())
                .build();
    }

}
