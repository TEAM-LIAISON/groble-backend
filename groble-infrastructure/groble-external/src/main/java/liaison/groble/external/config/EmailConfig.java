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

  @Value("${socks.proxy.host:10.0.1.238}")
  private String socksHost;

  @Value("${socks.proxy.port:1080}")
  private String socksPort;

  @Value("${socks.proxy.enabled:true}")
  private boolean socksEnabled;

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

    javaMailSender.setHost("smtp.gmail.com");
    javaMailSender.setUsername(id);
    javaMailSender.setPassword(password);
    javaMailSender.setPort(587);
    javaMailSender.setJavaMailProperties(getMailProperties());

    return javaMailSender;
  }

  private Properties getMailProperties() {
    Properties properties = new Properties();
    properties.setProperty("mail.transport.protocol", "smtp");
    properties.setProperty("mail.smtp.auth", "true");
    properties.setProperty("mail.smtp.starttls.enable", "true");
    properties.setProperty("mail.debug", "true");
    properties.setProperty("mail.smtp.ssl.trust", "smtp.gmail.com");

    // SOCKS 프록시 설정
    if (socksEnabled) {
      properties.setProperty("mail.smtp.socks.host", socksHost);
      properties.setProperty("mail.smtp.socks.port", socksPort);
    }

    // 타임아웃 설정
    properties.setProperty("mail.smtp.connectiontimeout", "10000");
    properties.setProperty("mail.smtp.timeout", "10000");
    properties.setProperty("mail.smtp.writetimeout", "5000");

    return properties;
  }
}
