package liaison.groble.application.admin.dashboard.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardTopContentsDTO {
  private LocalDate startDate;
  private LocalDate endDate;
  private Integer limit;
  private List<AdminDashboardTopContentDTO> contents;
}
