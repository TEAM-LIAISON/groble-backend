package liaison.groble.persistence.file;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.file.entity.FileInfo;
import liaison.groble.domain.file.repository.FileRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class FileRepositoryImpl implements FileRepository {
  private final JpaFileRepository jpaFileRepository;

  @Override
  public FileInfo save(FileInfo fileInfo) {
    return jpaFileRepository.save(fileInfo);
  }

  @Override
  public FileInfo findByFileName(String fileName) {
    return jpaFileRepository.findByFileName(fileName);
  }

  @Override
  public FileInfo findByFileUrl(String fileUrl) {
    return jpaFileRepository.findByFileUrl(fileUrl);
  }

  @Override
  public void delete(FileInfo fileInfo) {
    jpaFileRepository.delete(fileInfo);
  }
}
