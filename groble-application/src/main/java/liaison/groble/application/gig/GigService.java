package liaison.groble.application.gig;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.gig.dto.GigDraftDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GigService {

  @Transactional
  public void saveDraft(Long userId, GigDraftDto gigDraftDto) {}
}
