package liaison.groble.common.utils;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BirthDateUtilTest {

  @Test
  @DisplayName("YYYYMMDD를 YYMMDD로 정상 변환")
  void convertToSixDigit_Success() {
    // Given
    String birthDate = "19900101";

    // When
    String result = BirthDateUtil.convertToSixDigit(birthDate);

    // Then
    assertThat(result).isEqualTo("900101");
  }

  @Test
  @DisplayName("이미 6자리인 경우 그대로 반환")
  void convertToSixDigit_AlreadySixDigit() {
    // Given
    String birthDate = "900101";

    // When
    String result = BirthDateUtil.convertToSixDigit(birthDate);

    // Then
    assertThat(result).isEqualTo("900101");
  }

  @Test
  @DisplayName("공백이 포함된 생년월일 처리")
  void convertToSixDigit_WithWhitespace() {
    // Given
    String birthDate = " 19900101 ";

    // When
    String result = BirthDateUtil.convertToSixDigit(birthDate);

    // Then
    assertThat(result).isEqualTo("900101");
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "1990010", "199001011", "abcd1234", "1990-01-01"})
  @DisplayName("잘못된 형식의 생년월일은 예외 발생")
  void convertToSixDigit_InvalidFormat(String invalidBirthDate) {
    // When & Then
    assertThatThrownBy(() -> BirthDateUtil.convertToSixDigit(invalidBirthDate))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("null 입력 시 예외 발생")
  void convertToSixDigit_NullInput() {
    // When & Then
    assertThatThrownBy(() -> BirthDateUtil.convertToSixDigit(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("생년월일은 필수 입력값입니다.");
  }

  @ParameterizedTest
  @ValueSource(strings = {"19900101", "900101", "20001231", "001231"})
  @DisplayName("유효한 형식 검증 - 성공")
  void isValidFormat_Success(String validBirthDate) {
    // When & Then
    assertThat(BirthDateUtil.isValidFormat(validBirthDate)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "1990010", "199001011", "abcd1234", "1990-01-01"})
  @DisplayName("유효하지 않은 형식 검증 - 실패")
  void isValidFormat_Fail(String invalidBirthDate) {
    // When & Then
    assertThat(BirthDateUtil.isValidFormat(invalidBirthDate)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"19900101", "20001231", "900101", "001231"})
  @DisplayName("논리적 검증 - 성공 케이스")
  void isLogicallyValid_Success(String validBirthDate) {
    // When & Then
    assertThat(BirthDateUtil.isLogicallyValid(validBirthDate)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"19900001", "19901301", "19900132", "900001", "901301", "900132"})
  @DisplayName("논리적 검증 - 실패 케이스 (잘못된 월/일)")
  void isLogicallyValid_Fail(String invalidBirthDate) {
    // When & Then
    assertThat(BirthDateUtil.isLogicallyValid(invalidBirthDate)).isFalse();
  }

  @Test
  @DisplayName("2000년대 생년월일 변환")
  void convertToSixDigit_2000s() {
    // Given
    String birthDate = "20010315";

    // When
    String result = BirthDateUtil.convertToSixDigit(birthDate);

    // Then
    assertThat(result).isEqualTo("010315");
  }

  @Test
  @DisplayName("1900년대 생년월일 변환")
  void convertToSixDigit_1900s() {
    // Given
    String birthDate = "19851225";

    // When
    String result = BirthDateUtil.convertToSixDigit(birthDate);

    // Then
    assertThat(result).isEqualTo("851225");
  }
}
