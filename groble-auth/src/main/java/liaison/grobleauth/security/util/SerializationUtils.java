package liaison.grobleauth.security.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/** 객체 직렬화 및 역직렬화를 위한 유틸리티 클래스 */
public class SerializationUtils {
  /**
   * 객체를 바이트 배열로 직렬화
   *
   * @param obj 직렬화할 객체
   * @return 직렬화된 바이트 배열
   */
  public static byte[] serialize(Object obj) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(obj);
      return bos.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("객체 직렬화 중 오류 발생", e);
    }
  }

  /**
   * 바이트 배열을 객체로 역직렬화
   *
   * @param bytes 역직렬화할 바이트 배열
   * @return 역직렬화된 객체
   */
  public static Object deserialize(byte[] bytes) {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis)) {
      return ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("객체 역직렬화 중 오류 발생", e);
    }
  }
}
