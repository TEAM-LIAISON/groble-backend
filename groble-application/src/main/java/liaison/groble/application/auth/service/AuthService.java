package liaison.groble.application.auth.service;

public interface AuthService {

  TokenResponse signup(SignupRequest request)
      throws EmailAlreadyExistsException, EmailNotVerifiedException;
}
