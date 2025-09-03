package liaison.groble.mapping.admin;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.admin.settlement.request.SettlementApprovalRequest;
import liaison.groble.api.model.admin.settlement.response.SettlementApprovalResponse;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalRequestDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-04T00:33:04+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class AdminSettlementMapperImpl implements AdminSettlementMapper {

  @Override
  public SettlementApprovalRequestDTO toRequestDTO(SettlementApprovalRequest request) {
    if (request == null) {
      return null;
    }

    SettlementApprovalRequestDTO.SettlementApprovalRequestDTOBuilder settlementApprovalRequestDTO =
        SettlementApprovalRequestDTO.builder();

    List<Long> list = request.getSettlementIds();
    if (list != null) {
      settlementApprovalRequestDTO.settlementIds(new ArrayList<Long>(list));
    }
    if (request.getAdminUserId() != null) {
      settlementApprovalRequestDTO.adminUserId(request.getAdminUserId());
    }
    if (request.getApprovalReason() != null) {
      settlementApprovalRequestDTO.approvalReason(request.getApprovalReason());
    }
    settlementApprovalRequestDTO.executePaypleSettlement(request.isExecutePaypleSettlement());

    return settlementApprovalRequestDTO.build();
  }

  @Override
  public SettlementApprovalResponse toResponse(SettlementApprovalDTO dto) {
    if (dto == null) {
      return null;
    }

    SettlementApprovalResponse.SettlementApprovalResponseBuilder settlementApprovalResponse =
        SettlementApprovalResponse.builder();

    if (dto.getPaypleResult() != null) {
      settlementApprovalResponse.paypleResult(toPaypleResult(dto.getPaypleResult()));
    }
    List<SettlementApprovalResponse.FailedSettlement> list =
        failedSettlementDTOListToFailedSettlementList(dto.getFailedSettlements());
    if (list != null) {
      settlementApprovalResponse.failedSettlements(list);
    }
    settlementApprovalResponse.success(dto.isSuccess());
    settlementApprovalResponse.approvedSettlementCount(dto.getApprovedSettlementCount());
    settlementApprovalResponse.approvedItemCount(dto.getApprovedItemCount());
    if (dto.getTotalApprovedAmount() != null) {
      settlementApprovalResponse.totalApprovedAmount(dto.getTotalApprovedAmount());
    }
    if (dto.getApprovedAt() != null) {
      settlementApprovalResponse.approvedAt(dto.getApprovedAt());
    }
    settlementApprovalResponse.excludedRefundedItemCount(dto.getExcludedRefundedItemCount());

    return settlementApprovalResponse.build();
  }

  @Override
  public SettlementApprovalResponse.PaypleSettlementResult toPaypleResult(
      SettlementApprovalDTO.PaypleSettlementResultDTO dto) {
    if (dto == null) {
      return null;
    }

    SettlementApprovalResponse.PaypleSettlementResult.PaypleSettlementResultBuilder
        paypleSettlementResult = SettlementApprovalResponse.PaypleSettlementResult.builder();

    paypleSettlementResult.success(dto.isSuccess());
    if (dto.getResponseCode() != null) {
      paypleSettlementResult.responseCode(dto.getResponseCode());
    }
    if (dto.getResponseMessage() != null) {
      paypleSettlementResult.responseMessage(dto.getResponseMessage());
    }
    if (dto.getAccessToken() != null) {
      paypleSettlementResult.accessToken(dto.getAccessToken());
    }
    if (dto.getExpiresIn() != null) {
      paypleSettlementResult.expiresIn(dto.getExpiresIn());
    }

    return paypleSettlementResult.build();
  }

  @Override
  public SettlementApprovalResponse.FailedSettlement toFailedSettlement(
      SettlementApprovalDTO.FailedSettlementDTO dto) {
    if (dto == null) {
      return null;
    }

    SettlementApprovalResponse.FailedSettlement.FailedSettlementBuilder failedSettlement =
        SettlementApprovalResponse.FailedSettlement.builder();

    if (dto.getSettlementId() != null) {
      failedSettlement.settlementId(dto.getSettlementId());
    }
    if (dto.getFailureReason() != null) {
      failedSettlement.failureReason(dto.getFailureReason());
    }

    return failedSettlement.build();
  }

  protected List<SettlementApprovalResponse.FailedSettlement>
      failedSettlementDTOListToFailedSettlementList(
          List<SettlementApprovalDTO.FailedSettlementDTO> list) {
    if (list == null) {
      return null;
    }

    List<SettlementApprovalResponse.FailedSettlement> list1 =
        new ArrayList<SettlementApprovalResponse.FailedSettlement>(list.size());
    for (SettlementApprovalDTO.FailedSettlementDTO failedSettlementDTO : list) {
      list1.add(toFailedSettlement(failedSettlementDTO));
    }

    return list1;
  }
}
