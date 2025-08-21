package liaison.groble.mapping.auth;

import javax.annotation.processing.Generated;
import liaison.groble.api.model.auth.request.VerificationBusinessMakerAccountRequest;
import liaison.groble.api.model.auth.request.VerifyPersonalMakerAccountRequest;
import liaison.groble.application.auth.dto.VerifyBusinessMakerAccountDTO;
import liaison.groble.application.auth.dto.VerifyPersonalMakerAccountDTO;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-21T12:05:29+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)"
)
@Component
public class AccountVerificationMapperImpl implements AccountVerificationMapper {

    @Override
    public VerifyPersonalMakerAccountDTO toVerifyPersonalMakerAccountDTO(VerifyPersonalMakerAccountRequest request) {
        if ( request == null ) {
            return null;
        }

        VerifyPersonalMakerAccountDTO.VerifyPersonalMakerAccountDTOBuilder verifyPersonalMakerAccountDTO = VerifyPersonalMakerAccountDTO.builder();

        if ( request.getBankAccountOwner() != null ) {
            verifyPersonalMakerAccountDTO.bankAccountOwner( request.getBankAccountOwner() );
        }
        if ( request.getBankName() != null ) {
            verifyPersonalMakerAccountDTO.bankName( request.getBankName() );
        }
        if ( request.getBankAccountNumber() != null ) {
            verifyPersonalMakerAccountDTO.bankAccountNumber( request.getBankAccountNumber() );
        }
        if ( request.getCopyOfBankbookUrl() != null ) {
            verifyPersonalMakerAccountDTO.copyOfBankbookUrl( request.getCopyOfBankbookUrl() );
        }

        return verifyPersonalMakerAccountDTO.build();
    }

    @Override
    public VerifyBusinessMakerAccountDTO toVerifyBusinessMakerAccountDTO(VerificationBusinessMakerAccountRequest request) {
        if ( request == null ) {
            return null;
        }

        VerifyBusinessMakerAccountDTO.VerifyBusinessMakerAccountDTOBuilder verifyBusinessMakerAccountDTO = VerifyBusinessMakerAccountDTO.builder();

        if ( request.getBankAccountOwner() != null ) {
            verifyBusinessMakerAccountDTO.bankAccountOwner( request.getBankAccountOwner() );
        }
        if ( request.getBankName() != null ) {
            verifyBusinessMakerAccountDTO.bankName( request.getBankName() );
        }
        if ( request.getBankAccountNumber() != null ) {
            verifyBusinessMakerAccountDTO.bankAccountNumber( request.getBankAccountNumber() );
        }
        if ( request.getCopyOfBankbookUrl() != null ) {
            verifyBusinessMakerAccountDTO.copyOfBankbookUrl( request.getCopyOfBankbookUrl() );
        }
        if ( request.getBusinessType() != null ) {
            verifyBusinessMakerAccountDTO.businessType( map( request.getBusinessType() ) );
        }
        if ( request.getBusinessCategory() != null ) {
            verifyBusinessMakerAccountDTO.businessCategory( request.getBusinessCategory() );
        }
        if ( request.getBusinessSector() != null ) {
            verifyBusinessMakerAccountDTO.businessSector( request.getBusinessSector() );
        }
        if ( request.getBusinessName() != null ) {
            verifyBusinessMakerAccountDTO.businessName( request.getBusinessName() );
        }
        if ( request.getRepresentativeName() != null ) {
            verifyBusinessMakerAccountDTO.representativeName( request.getRepresentativeName() );
        }
        if ( request.getBusinessAddress() != null ) {
            verifyBusinessMakerAccountDTO.businessAddress( request.getBusinessAddress() );
        }
        if ( request.getBusinessLicenseFileUrl() != null ) {
            verifyBusinessMakerAccountDTO.businessLicenseFileUrl( request.getBusinessLicenseFileUrl() );
        }
        if ( request.getTaxInvoiceEmail() != null ) {
            verifyBusinessMakerAccountDTO.taxInvoiceEmail( request.getTaxInvoiceEmail() );
        }

        return verifyBusinessMakerAccountDTO.build();
    }
}
