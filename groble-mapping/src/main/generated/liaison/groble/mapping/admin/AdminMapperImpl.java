package liaison.groble.mapping.admin;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.admin.response.AdminOrderCancelRequestResponse;
import liaison.groble.application.admin.dto.AdminOrderCancelRequestDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-07T15:25:00+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class AdminMapperImpl implements AdminMapper {

  @Override
  public AdminOrderCancelRequestResponse toAdminOrderCancelRequestResponse(
      AdminOrderCancelRequestDTO adminOrderCancelRequestDTO) {
    if (adminOrderCancelRequestDTO == null) {
      return null;
    }

    AdminOrderCancelRequestResponse.AdminOrderCancelRequestResponseBuilder
        adminOrderCancelRequestResponse = AdminOrderCancelRequestResponse.builder();

    return adminOrderCancelRequestResponse.build();
  }
}
