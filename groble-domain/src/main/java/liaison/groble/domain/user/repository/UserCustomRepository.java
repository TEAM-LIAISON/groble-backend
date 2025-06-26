package liaison.groble.domain.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.user.dto.FlatAdminUserSummaryInfoDTO;

public interface UserCustomRepository {
  Page<FlatAdminUserSummaryInfoDTO> findUsersByPageable(Pageable pageable);

  boolean existsByIntegratedAccountEmailAndPhoneNumber(String integratedAccountEmail);
}
