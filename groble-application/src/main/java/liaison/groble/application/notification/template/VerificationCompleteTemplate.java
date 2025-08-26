package liaison.groble.application.notification.template;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VerificationCompleteTemplate {
    @Value("${bizppurio.templates.verification-complete.code}")
    private String verificationCompleteTemplateCode;
}
