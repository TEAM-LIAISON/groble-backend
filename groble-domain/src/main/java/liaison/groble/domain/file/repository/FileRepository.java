package liaison.groble.domain.file.repository;

import liaison.groble.domain.file.entity.FileInfo;

public interface FileRepository {
  FileInfo save(FileInfo fileInfo);

  FileInfo findByFileName(String fileName);

  FileInfo findByFileUrl(String fileUrl);

  void delete(FileInfo fileInfo);
}
