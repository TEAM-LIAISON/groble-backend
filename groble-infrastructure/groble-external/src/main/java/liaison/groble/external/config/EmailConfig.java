package liaison.groble.external.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfig {

  @Value("${spring.mail.username}")
  private String id;

  @Value("${spring.mail.password}")
  private String password;

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

    javaMailSender.setHost("smtp.gmail.com"); // 일반적인 Gmail 서버를 사용
    javaMailSender.setUsername(id);
    javaMailSender.setPassword(password);
    javaMailSender.setPort(587); // TLS 포트 설정
    javaMailSender.setJavaMailProperties(getMailProperties());

    return javaMailSender;
  }

  private Properties getMailProperties() {
    Properties properties = new Properties();
    properties.setProperty("mail.transport.protocol", "smtp");
    properties.setProperty("mail.smtp.auth", "true");
    properties.setProperty("mail.smtp.starttls.enable", "true"); // TLS 설정
    properties.setProperty("mail.debug", "true");
    properties.setProperty("mail.smtp.ssl.trust", "smtp.gmail.com");
    return properties;
  }
}
