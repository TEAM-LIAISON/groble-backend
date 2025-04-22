package liaison.groble.api.server.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
@Tag(name = "스토리지 저장 API", description = "이미지 및 파일 저장 API")
public class StorageController {}
