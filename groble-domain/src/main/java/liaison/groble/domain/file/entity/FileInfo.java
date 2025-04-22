package liaison.groble.domain.file.entity;

import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import liaison.groble.domain.common.utils.MapToJsonConverter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FileInfo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private String originalFilename;

  @Column(nullable = false)
  private String fileUrl;

  @Column(nullable = false)
  private String contentType;

  @Column(nullable = false)
  private Long fileSize;

  @Column(nullable = false)
  private String storagePath;

  // 파일 소유자 ID
  @Column(name = "user_id")
  private Long userId;

  // 메타데이터 저장
  @Convert(converter = MapToJsonConverter.class)
  @Column(columnDefinition = "json")
  private Map<String, String> metadata;
}
