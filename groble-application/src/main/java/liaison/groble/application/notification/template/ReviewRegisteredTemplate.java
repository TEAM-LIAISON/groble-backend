package liaison.groble.application.notification.template;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReviewRegisteredTemplate {
    @Value("${bizppurio.templates.review-register.code}")
    private String reviewRegisteredTemplateCode;
}
