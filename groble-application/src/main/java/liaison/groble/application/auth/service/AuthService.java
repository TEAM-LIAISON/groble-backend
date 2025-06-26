package liaison.groble.application.auth.service;

import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDTO;
import liaison.groble.application.auth.dto.UserWithdrawalDTO;

public interface AuthService {

  SignInAuthResultDTO signIn(SignInDTO signInDto);

  void withdrawUser(Long userId, UserWithdrawalDTO userWithdrawalDto);
}
