package my.personal.budgetTracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import my.personal.budgetTracker.dto.CategoryDTO;
import my.personal.budgetTracker.entity.CategoryEntity;
import my.personal.budgetTracker.entity.ProfileEntity;
import my.personal.budgetTracker.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;

    //Save Category
    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        if (categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), profile.getId())) {
            throw new RuntimeException("Category with the same name already exists for this profile.");
        }
        CategoryEntity newCategoryEntity = toEntity(categoryDTO, profile);
        newCategoryEntity = categoryRepository.save(newCategoryEntity);
        return toDTO(newCategoryEntity);
    }

    public List<CategoryDTO> getCategoriesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categoryEntities = categoryRepository.findByProfileId(profile.getId());
        return categoryEntities.stream().map(this::toDTO).toList();
    }

    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categoryEntities = categoryRepository.findByTypeAndProfileId(type, profile.getId());
        return categoryEntities.stream().map(this::toDTO).toList();
    }

    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found for the current profile."));

        if (!existingCategory.getName().equals(categoryDTO.getName())
                && categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), profile.getId())) {
            throw new RuntimeException("Another category with the same name already exists for this profile.");
        }

        existingCategory.setName(categoryDTO.getName());
        existingCategory.setIcon(categoryDTO.getIcon());
        existingCategory.setType(categoryDTO.getType());

        existingCategory = categoryRepository.save(existingCategory);
        return toDTO(existingCategory);
    }

    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile) {
        return CategoryEntity.builder()
                .id(categoryDTO.getId())
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .type(categoryDTO.getType())
                .profile(profile)
                .build();
    }

    private CategoryDTO toDTO(CategoryEntity categoryEntity) {
        return CategoryDTO.builder()
                .id(categoryEntity.getId())
                .name(categoryEntity.getName())
                .icon(categoryEntity.getIcon())
                .type(categoryEntity.getType())
                .profileId(categoryEntity.getProfile() != null ? categoryEntity.getProfile().getId() : null)
                .createdAt(categoryEntity.getCreatedAt())
                .updatedAt(categoryEntity.getUpdatedAt())
                .build();
    }
}
