package liaison.groble.api.model.admin.dashboard.response;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardTopContentsResponse {
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate startDate;
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate endDate;
  private Integer limit;
  private List<AdminDashboardTopContentResponse> contents;
}
