// package liaison.groble.api.server.payment;
//
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
//
// import liaison.groble.application.payment.service.PayplePaymentService;
//
// import lombok.RequiredArgsConstructor;
//
// @Controller
// @RequiredArgsConstructor
// public class PayplePaymentPageController {
//
//  private final PayplePaymentService payplePaymentService;
//
//  @GetMapping("/payment/payple")
//  public String payplePage(Model model) {
//    model.addAttribute("paypleJsUrl", payplePaymentService.getPaymentJsUrl());
//    return "payment/payple-payment"; // 확장자 `.html`은 생략
//  }
// }
