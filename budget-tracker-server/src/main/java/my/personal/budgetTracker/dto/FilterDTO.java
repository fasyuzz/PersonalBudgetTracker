package my.personal.budgetTracker.dto;

import java.time.LocalDate;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterDTO {

    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private String sortField; //date/amount/name
    private String sortOrder; //asc/desc
}
