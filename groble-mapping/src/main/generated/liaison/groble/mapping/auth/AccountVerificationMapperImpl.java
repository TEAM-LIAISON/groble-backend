package liaison.groble.mapping.auth;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.auth.request.VerifyPersonalMakerAccountRequest;
import liaison.groble.application.auth.dto.VerifyPersonalMakerAccountDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-17T21:13:18+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class AccountVerificationMapperImpl implements AccountVerificationMapper {

  @Override
  public VerifyPersonalMakerAccountDTO toVerifyPersonalMakerAccountDTO(
      VerifyPersonalMakerAccountRequest request) {
    if (request == null) {
      return null;
    }

    VerifyPersonalMakerAccountDTO.VerifyPersonalMakerAccountDTOBuilder
        verifyPersonalMakerAccountDTO = VerifyPersonalMakerAccountDTO.builder();

    if (request.getBankAccountOwner() != null) {
      verifyPersonalMakerAccountDTO.bankAccountOwner(request.getBankAccountOwner());
    }
    if (request.getBankName() != null) {
      verifyPersonalMakerAccountDTO.bankName(request.getBankName());
    }
    if (request.getBankAccountNumber() != null) {
      verifyPersonalMakerAccountDTO.bankAccountNumber(request.getBankAccountNumber());
    }
    if (request.getCopyOfBankbookUrl() != null) {
      verifyPersonalMakerAccountDTO.copyOfBankbookUrl(request.getCopyOfBankbookUrl());
    }

    return verifyPersonalMakerAccountDTO.build();
  }
}
