package liaison.groble.api.server.admin;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.request.AdminMakerVerifyRequest;
import liaison.groble.api.model.admin.request.AdminMemoRequest;
import liaison.groble.api.model.admin.response.maker.AdminMakerDetailInfoResponse;
import liaison.groble.api.model.admin.response.maker.AdminMemoResponse;
import liaison.groble.api.model.maker.response.MakerIntroSectionResponse;
import liaison.groble.api.server.admin.docs.AdminMakerSwaggerDocs;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.application.admin.dto.AdminMakerDetailInfoDTO;
import liaison.groble.application.admin.dto.AdminMemoDTO;
import liaison.groble.application.admin.service.AdminMakerService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.admin.AdminMakerMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiPaths.Admin.MAKER_BASE)
@Tag(name = AdminMakerSwaggerDocs.TAG_NAME, description = AdminMakerSwaggerDocs.TAG_DESCRIPTION)
public class AdminMakerController extends BaseController {

  private final AdminMakerService adminMakerService;
  private final AdminMakerMapper adminMakerMapper;

  public AdminMakerController(
      ResponseHelper responseHelper,
      AdminMakerService adminMakerService,
      AdminMakerMapper adminMakerMapper) {
    super(responseHelper);
    this.adminMakerService = adminMakerService;
    this.adminMakerMapper = adminMakerMapper;
  }

  @Operation(
      summary = AdminMakerSwaggerDocs.GET_MAKER_DETAIL_SUMMARY,
      description = AdminMakerSwaggerDocs.GET_MAKER_DETAIL_DESCRIPTION)
  @ApiResponse(
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = MakerIntroSectionResponse.class)))
  @Logging(
      item = "AdminMaker",
      action = "getMakerDetailInfo",
      includeParam = true,
      includeResult = true)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.MAKER_DETAIL)
  public ResponseEntity<GrobleResponse<AdminMakerDetailInfoResponse>> getMakerDetailInfo(
      @Auth Accessor accessor, @Valid @PathVariable("nickname") String nickname) {

    AdminMakerDetailInfoDTO adminMakerServiceMakerDetailInfo =
        adminMakerService.getMakerDetailInfo(accessor.getUserId(), nickname);
    AdminMakerDetailInfoResponse response =
        adminMakerMapper.toAdminMakerDetailInfoResponse(adminMakerServiceMakerDetailInfo);

    return success(response, ResponseMessages.Admin.MAKER_DETAIL_INFO_RETRIEVED);
  }

  @Operation(
      summary = AdminMakerSwaggerDocs.VERIFY_MAKER_SUMMARY,
      description = AdminMakerSwaggerDocs.VERIFY_MAKER_DESCRIPTION)
  @RequireRole("ROLE_ADMIN")
  @PostMapping(ApiPaths.Admin.MAKER_VERIFY)
  public ResponseEntity<GrobleResponse<Void>> verifyMakerAccount(
      @Auth Accessor accessor, @Valid @RequestBody AdminMakerVerifyRequest request) {

    return switch (request.getStatus()) {
      case APPROVED -> {
        adminMakerService.approveMaker(request.getNickname());
        yield successVoid(ResponseMessages.Admin.MAKER_VERIFY_APPROVED);
      }
      case REJECTED -> {
        adminMakerService.rejectMaker(request.getNickname());
        yield successVoid(ResponseMessages.Admin.MAKER_VERIFY_REJECTED);
      }
    };
  }

  @Operation(
      summary = AdminMakerSwaggerDocs.SAVE_ADMIN_MEMO_SUMMARY,
      description = AdminMakerSwaggerDocs.SAVE_ADMIN_MEMO_DESCRIPTION)
  @RequireRole("ROLE_ADMIN")
  @PostMapping(ApiPaths.Admin.MAKER_MEMO)
  public ResponseEntity<GrobleResponse<AdminMemoResponse>> saveAdminMemo(
      @Auth Accessor accessor,
      @Valid @PathVariable("nickname") String nickname,
      @Valid @RequestBody AdminMemoRequest adminMemoRequest) {
    AdminMemoDTO memoDTO = adminMakerMapper.toAdminMemoDTO(adminMemoRequest);

    AdminMemoDTO savedAdminMemoDTO =
        adminMakerService.saveAdminMemo(accessor.getUserId(), nickname, memoDTO);
    AdminMemoResponse savedAdminMemo = adminMakerMapper.toAdminMemoResponse(savedAdminMemoDTO);
    return success(savedAdminMemo, ResponseMessages.Admin.MAKER_MEMO_SAVED);
  }
}
