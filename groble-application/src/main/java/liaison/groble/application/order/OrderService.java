package liaison.groble.application.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.order.dto.OrderCreateDto;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.content.repository.ContentRepository;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.OrderItem;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.purchase.entity.Purchaser;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
  private final UserReader userReader;
  private final ContentReader contentReader;
  private final ContentRepository contentRepository;
  private final OrderRepository orderRepository;

  // 환경별 프론트엔드 도메인 설정
  @Value("${app.frontend-url}")
  private String frontendDomain; // 환경별로 설정 가능하도록 변경

  @Transactional
  public String createOrder(Long userId, OrderCreateDto orderCreateDto) {
    User user = userReader.getUserById(userId);

    Content content = contentReader.getContentById(orderCreateDto.getContentId());
    OrderItem.OptionType optionType = null;
    if (content.getContentType().equals(ContentType.COACHING)) {
      optionType = OrderItem.OptionType.COACHING_OPTION;
    } else if (content.getContentType().equals(ContentType.DOCUMENT)) {
      optionType = OrderItem.OptionType.DOCUMENT_OPTION;
    }

    // Get the selected option
    ContentOption selectedOption =
        content.getOptions().stream()
            .filter(option -> option.getId().equals(orderCreateDto.getContentOptionId()))
            .findFirst()
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "해당 옵션을 가진 콘텐츠를 찾을 수 없습니다. ID: " + orderCreateDto.getContentOptionId()));

    Purchaser purchaser =
        Purchaser.builder()
            .name(user.getUserProfile().getNickname())
            .email(user.getEmail())
            .phone(user.getUserProfile().getPhoneNumber())
            .build();

    Order order =
        Order.createOrderWithOption(
            user,
            content,
            optionType,
            selectedOption.getId(),
            selectedOption.getName(),
            selectedOption.getPrice(),
            purchaser);

    Order savedOrder = orderRepository.save(order);
    String savedMerchantUId = savedOrder.getMerchantUid();

    // merchantUId에서 두 번째 언더바 이후의 숫자 추출
    String parsedMerchantUId = null;
    if (savedMerchantUId != null && savedMerchantUId.split("_").length >= 3) {
      parsedMerchantUId = savedMerchantUId.split("_")[2];
    }

    // 리다이렉트 URL 생성
    String redirectUrl = null;
    if (parsedMerchantUId != null) {
      redirectUrl = frontendDomain + "/contents/" + parsedMerchantUId;
    }

    return redirectUrl;
  }
}
