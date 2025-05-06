package liaison.groble.api.server.user;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.user.request.UserTypeRequest;
import liaison.groble.api.model.user.response.BuyerMyPageSummaryResponse;
import liaison.groble.api.model.user.response.MyPageSummaryResponseBase;
import liaison.groble.api.model.user.response.SellerMyPageSummaryResponse;
import liaison.groble.api.model.user.response.UserMyPageDetailResponse;
import liaison.groble.api.server.user.mapper.UserDtoMapper;
import liaison.groble.application.user.dto.UserMyPageDetailDto;
import liaison.groble.application.user.dto.UserMyPageSummaryDto;
import liaison.groble.application.user.service.UserService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 사용자 정보 관련 API 컨트롤러 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "사용자 정보 API", description = "닉네임, 비밀번호 설정 및 계정 전환 API")
public class UserController {

  private final UserService userService;
  private final UserDtoMapper userDtoMapper;

  public UserController(UserService userService, UserDtoMapper userDtoMapper) {
    this.userService = userService;
    this.userDtoMapper = userDtoMapper;
  }

  /** 사용자 역할 전환 API - 판매자/구매자 모드 전환 */
  @Operation(summary = "가입 유형 전환", description = "판매자 또는 구매자로 가입 유형을 전환합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "역할 전환 성공"),
    @ApiResponse(responseCode = "400", description = "역할 전환 실패 - 해당 역할이 할당되어 있지 않음"),
    @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
    @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @PostMapping("/switch-role")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @RequireRole({"ROLE_USER"})
  public void switchUserType(@Auth Accessor accessor, @Valid @RequestBody UserTypeRequest request) {
    boolean success = userService.switchUserType(accessor.getUserId(), request.getUserType());
  }

  /** 마이페이지 요약 정보 조회 */
  /** 마이페이지 요약 정보 조회 */
  @Operation(
      summary = "마이페이지 요약 정보 조회",
      description = "마이페이지 첫 화면에서 요약 정보를 조회합니다. 사용자 유형(구매자/판매자)에 따라 응답 구조가 달라집니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "요청 성공",
        content = {
          @Content(
              mediaType = "application/json",
              schema =
                  @Schema(
                      oneOf = {BuyerMyPageSummaryResponse.class, SellerMyPageSummaryResponse.class},
                      discriminatorProperty = "userType.code"),
              examples = {
                @ExampleObject(
                    name = "구매자 응답",
                    summary = "구매자 마이페이지 요약 정보",
                    value =
                        """
                      {
                        "status": "SUCCESS",
                        "code": 200,
                        "message": "요청이 성공적으로 처리되었습니다.",
                        "data": {
                          "nickname": "권동민",
                          "profileImageUrl": null,
                          "userType": {
                            "code": "BUYER",
                            "description": "구매자"
                          },
                          "canSwitchToSeller": false
                        },
                        "timestamp": "2025-05-06 04:26:26"
                      }
                      """),
                @ExampleObject(
                    name = "판매자 응답",
                    summary = "판매자 마이페이지 요약 정보",
                    value =
                        """
                      {
                        "status": "SUCCESS",
                        "code": 200,
                        "message": "요청이 성공적으로 처리되었습니다.",
                        "data": {
                          "nickname": "김판매",
                          "profileImageUrl": "https://example.com/profile.jpg",
                          "userType": {
                            "code": "SELLER",
                            "description": "판매자"
                          },
                          "verificationStatus": {
                            "code": "APPROVED",
                            "description": "승인됨"
                          }
                        },
                        "timestamp": "2025-05-06 04:26:26"
                      }
                      """)
              })
        }),
    @ApiResponse(responseCode = "401", description = "인증 실패"),
    @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
  })
  @GetMapping("/me/summary")
  public ResponseEntity<GrobleResponse<MyPageSummaryResponseBase>> getUserMyPageSummary(
      @Auth Accessor accessor) {
    UserMyPageSummaryDto summaryDto = userService.getUserMyPageSummary(accessor.getUserId());
    MyPageSummaryResponseBase response = userDtoMapper.toApiMyPageSummaryResponse(summaryDto);

    return ResponseEntity.ok(GrobleResponse.success(response));
  }

  /** 마이페이지 상세 정보 조회 */
  @Operation(summary = "마이페이지 상세 정보 조회", description = "마이페이지에서 사용자 상세 정보를 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "마이페이지 상세 정보 조회 성공",
        content = @Content(schema = @Schema(implementation = UserMyPageDetailResponse.class))),
    @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
    @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
  })
  @GetMapping("/me/detail")
  public UserMyPageDetailResponse getUserMyPageDetail(@Auth Accessor accessor) {
    UserMyPageDetailDto detailDto = userService.getUserMyPageDetail(accessor.getUserId());
    return userDtoMapper.toApiMyPageDetailResponse(detailDto);
  }
}
