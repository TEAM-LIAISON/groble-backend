package liaison.groble.mapping.verification;

import org.mapstruct.Mapper;

import liaison.groble.api.model.auth.request.EmailVerificationRequest;
import liaison.groble.api.model.auth.request.VerifyEmailCodeRequest;
import liaison.groble.application.auth.dto.EmailVerificationDTO;
import liaison.groble.application.auth.dto.VerifyEmailCodeDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface VerificationMapper {
  // ====== 📥 Request → DTO 변환 ======
  /** [이메일 인증(코드 요청)] EmailVerificationRequest → EmailVerificationDTO */
  EmailVerificationDTO toEmailVerificationDTO(EmailVerificationRequest request);

  /** [이메일 인증(코드 검증)] VerifyEmailCodeRequest → VerifyEmailCodeDTO */
  VerifyEmailCodeDTO toVerifyEmailCodeDTO(VerifyEmailCodeRequest request);
}
