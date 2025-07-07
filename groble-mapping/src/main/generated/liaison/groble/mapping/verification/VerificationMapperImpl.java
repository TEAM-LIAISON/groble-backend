package liaison.groble.mapping.verification;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.auth.request.EmailVerificationRequest;
import liaison.groble.api.model.auth.request.VerifyEmailCodeRequest;
import liaison.groble.application.auth.dto.EmailVerificationDTO;
import liaison.groble.application.auth.dto.VerifyEmailCodeDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-07T21:45:28+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class VerificationMapperImpl implements VerificationMapper {

  @Override
  public EmailVerificationDTO toEmailVerificationDTO(EmailVerificationRequest request) {
    if (request == null) {
      return null;
    }

    EmailVerificationDTO.EmailVerificationDTOBuilder emailVerificationDTO =
        EmailVerificationDTO.builder();

    if (request.getEmail() != null) {
      emailVerificationDTO.email(request.getEmail());
    }

    return emailVerificationDTO.build();
  }

  @Override
  public VerifyEmailCodeDTO toVerifyEmailCodeDTO(VerifyEmailCodeRequest request) {
    if (request == null) {
      return null;
    }

    VerifyEmailCodeDTO.VerifyEmailCodeDTOBuilder verifyEmailCodeDTO = VerifyEmailCodeDTO.builder();

    if (request.getEmail() != null) {
      verifyEmailCodeDTO.email(request.getEmail());
    }
    if (request.getVerificationCode() != null) {
      verifyEmailCodeDTO.verificationCode(request.getVerificationCode());
    }

    return verifyEmailCodeDTO.build();
  }
}
