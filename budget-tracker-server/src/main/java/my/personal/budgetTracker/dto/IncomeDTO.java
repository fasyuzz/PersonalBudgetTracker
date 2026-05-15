package my.personal.budgetTracker.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncomeDTO {

    private Long id;
    private String name;
    private String icon;
    private String categoryName;
    private Long categoryId;
    private BigDecimal amount;
    private LocalDate date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
