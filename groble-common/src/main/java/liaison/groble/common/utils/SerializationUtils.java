package liaison.groble.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;

/**
 * 객체 직렬화 및 역직렬화를 위한 유틸리티 클래스.
 *
 * <p>주의: Java 기본 직렬화는 다음과 같은 제약이 있습니다:
 *
 * <ul>
 *   <li>직렬화할 객체는 반드시 {@link Serializable} 인터페이스를 구현해야 합니다.
 *   <li>보안에 민감한 데이터는 직렬화하기 전에 암호화하는 것이 좋습니다.
 *   <li>클래스 구조가 변경되면 역직렬화가 실패할 수 있습니다.
 *   <li>대용량 데이터의 경우 JSON 등 다른 직렬화 방식을 고려하세요.
 * </ul>
 */
@Slf4j
public class SerializationUtils {
  // 인스턴스화 방지
  private SerializationUtils() {
    throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
  }

  /**
   * 객체를 바이트 배열로 직렬화합니다.
   *
   * @param obj 직렬화할 객체 (반드시 {@link Serializable} 구현 필요)
   * @return 직렬화된 바이트 배열
   * @throws SerializationException 직렬화 과정에서 오류 발생 시
   */
  public static byte[] serialize(Object obj) {
    if (obj == null) {
      throw new IllegalArgumentException("직렬화할 객체는 null일 수 없습니다.");
    }

    if (!(obj instanceof Serializable)) {
      throw new IllegalArgumentException(
          String.format("직렬화할 객체는 Serializable을 구현해야 합니다. 실제 클래스: %s", obj.getClass().getName()));
    }

    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(obj);
      byte[] result = bos.toByteArray();
      log.debug("객체 직렬화 완료: 클래스={}, 크기={}바이트", obj.getClass().getSimpleName(), result.length);
      return result;
    } catch (IOException e) {
      log.error("객체 직렬화 중 오류 발생: {}", e.getMessage(), e);
      throw new SerializationException("객체 직렬화 중 오류 발생", e);
    }
  }

  /**
   * 바이트 배열을 객체로 역직렬화합니다.
   *
   * @param bytes 역직렬화할 바이트 배열
   * @return 역직렬화된 객체
   * @throws SerializationException 역직렬화 과정에서 오류 발생 시
   */
  public static Object deserialize(byte[] bytes) {
    validateDeserializationInput(bytes);

    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis)) {
      Object result = ois.readObject();
      log.debug("객체 역직렬화 완료: 클래스={}", result != null ? result.getClass().getSimpleName() : "null");
      return result;
    } catch (IOException | ClassNotFoundException e) {
      log.error("객체 역직렬화 중 오류 발생: {}", e.getMessage(), e);
      throw new SerializationException("객체 역직렬화 중 오류 발생", e);
    }
  }

  /**
   * 바이트 배열을 지정된 타입의 객체로 역직렬화합니다.
   *
   * @param bytes 역직렬화할 바이트 배열
   * @param targetClass 반환할 객체의 클래스
   * @param <T> 반환할 객체의 타입
   * @return 역직렬화된 특정 타입의 객체
   * @throws SerializationException 역직렬화 과정에서 오류 발생 시
   */
  public static <T> T deserialize(byte[] bytes, Class<T> targetClass) {
    validateDeserializationInput(bytes);

    if (targetClass == null) {
      throw new IllegalArgumentException("대상 클래스는 null일 수 없습니다.");
    }

    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis)) {

      Object result = ois.readObject();

      if (result == null) {
        return null;
      }

      if (!targetClass.isInstance(result)) {
        throw new ClassCastException(
            String.format(
                "역직렬화된 객체 타입이 예상과 다릅니다. 예상=%s, 실제=%s",
                targetClass.getName(), result.getClass().getName()));
      }

      log.debug("객체 역직렬화 완료: 클래스={}", targetClass.getSimpleName());
      return targetClass.cast(result);
    } catch (IOException | ClassNotFoundException e) {
      log.error("객체 역직렬화 중 오류 발생: {}", e.getMessage(), e);
      throw new SerializationException("객체 역직렬화 중 오류 발생", e);
    }
  }

  /**
   * 역직렬화 입력 데이터를 검증합니다.
   *
   * @param bytes 검증할 바이트 배열
   */
  private static void validateDeserializationInput(byte[] bytes) {
    if (bytes == null) {
      throw new IllegalArgumentException("역직렬화할 바이트 배열은 null일 수 없습니다.");
    }

    if (bytes.length == 0) {
      throw new IllegalArgumentException("역직렬화할 바이트 배열이 비어 있습니다.");
    }
  }

  /** 직렬화/역직렬화 관련 예외 클래스 */
  public static class SerializationException extends RuntimeException {
    /**
     * 직렬화/역직렬화 예외 생성
     *
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public SerializationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
