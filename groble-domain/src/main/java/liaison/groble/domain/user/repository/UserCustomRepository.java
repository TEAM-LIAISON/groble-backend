package liaison.groble.domain.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.user.dto.FlatAdminUserSummaryInfoDTO;

public interface UserCustomRepository {
  Page<FlatAdminUserSummaryInfoDTO> findUsersByPageable(Pageable pageable);

  Optional<FlatAdminUserSummaryInfoDTO> findUserByNickname(String nickname);

  boolean existsByIntegratedAccountEmailAndPhoneNumber(String integratedAccountEmail);
}
