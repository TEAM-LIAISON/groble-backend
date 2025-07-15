package liaison.groble.api.server.global;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.content.response.dynamic.DynamicContentListResponse;
import liaison.groble.application.content.dto.DynamicContentDTO;
import liaison.groble.application.content.service.ContentService;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.content.DynamicContentMapper;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "[💿 동적 데이터] 그로블 동적 데이터 목록 조회 API", description = "그로블 SEO 동적 데이터 목록 조회 기능입니다.")
public class GrobleController {

  // API 경로 상수화
  private static final String DYNAMIC_CONTENTS_PATH = "/groble/contents";

  // 응답 메시지 상수화
  private static final String DYNAMIC_CONTENTS_RESPONSE_MESSAGE = "동적 콘텐츠 목록 조회 성공";

  // Service
  private final ContentService contentService;

  // Mapper
  private final DynamicContentMapper dynamicContentMapper;

  // Helper
  private final ResponseHelper responseHelper;

  @GetMapping(DYNAMIC_CONTENTS_PATH)
  public ResponseEntity<GrobleResponse<DynamicContentListResponse>> getDynamicContentList() {
    List<DynamicContentDTO> dynamicContentDTOS = contentService.getDynamicContents();

    DynamicContentListResponse response =
        dynamicContentMapper.toDynamicContentListResponse(dynamicContentDTOS);

    return responseHelper.success(response, DYNAMIC_CONTENTS_RESPONSE_MESSAGE, HttpStatus.OK);
  }
}
