package liaison.groble.persistence.file;

import liaison.groble.domain.file.entity.FileInfo;
import liaison.groble.domain.file.repository.FileRepository;

public class FileRepositoryImpl implements FileRepository {
  private final JpaFileRepository jpaFileRepository;

  public FileRepositoryImpl(JpaFileRepository jpaFileRepository) {
    this.jpaFileRepository = jpaFileRepository;
  }

  @Override
  public FileInfo save(FileInfo fileInfo) {
    return jpaFileRepository.save(fileInfo);
  }

  @Override
  public FileInfo findByFileName(String fileName) {
    return jpaFileRepository.findByFileName(fileName);
  }
}
