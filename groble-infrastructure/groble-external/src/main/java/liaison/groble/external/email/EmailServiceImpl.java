// package liaison.groble.external.email;
//
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.mail.javamail.JavaMailSender;
//
// public class EmailServiceImpl implements EmailService {
//    private final JavaMailSender emailSender;
//
//    @Value("${app.backend-url}")
//    private String backendUrl;
//
//    @Value("${spring.mail.username}")
//    private String fromEmail;
//
//    public void sendVerificationEmail(String to, String token) throws MessagingException {
//        MimeMessage message = emailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//        helper.setFrom(fromEmail);
//        helper.setTo(to);
//        helper.setSubject("이메일 인증을 완료해주세요");
//
//        // 이메일을 Base64로 인코딩하여 파라미터로 전달 (개인정보 보호를 위해)
//        String encodedEmail = java.util.Base64.getEncoder().encodeToString(to.getBytes());
//
//        String verificationUrl =
//                backendUrl + "/api/v1/auth/verify?token=" + token + "&email=" + encodedEmail;
//
//        String htmlContent =
//                "<div style='margin:20px;'>"
//                        + "<h2>안녕하세요, Groble 서비스에 가입해주셔서 감사합니다.</h2>"
//                        + "<p>아래 버튼을 클릭하여 이메일 인증을 완료해주세요.</p>"
//                        + "<a href='"
//                        + verificationUrl
//                        + "' "
//                        + "style='background-color:#4CAF50;color:white;padding:10px
// 20px;text-decoration:none;border-radius:5px;'>"
//                        + "이메일 인증하기</a>"
//                        + "<p>인증 후 회원가입 페이지로 돌아가 나머지 정보를 입력해주세요.</p>"
//                        + "<p>감사합니다.</p>"
//                        + "</div>";
//
//        helper.setText(htmlContent, true);
//
//        emailSender.send(message);
//        log.info("인증 이메일 발송 완료: {}", to);
//    }
// }
