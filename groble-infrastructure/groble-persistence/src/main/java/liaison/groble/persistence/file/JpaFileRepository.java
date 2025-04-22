package liaison.groble.persistence.file;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.file.entity.FileInfo;

public interface JpaFileRepository extends JpaRepository<FileInfo, Long> {
  FileInfo findByFileName(String fileName);
}
