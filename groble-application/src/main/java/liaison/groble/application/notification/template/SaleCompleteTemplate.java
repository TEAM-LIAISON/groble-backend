package liaison.groble.application.notification.template;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SaleCompleteTemplate {
    @Value("${bizppurio.templates.sale-complete.code}")
    private String saleCompleteTemplateCode;
}
