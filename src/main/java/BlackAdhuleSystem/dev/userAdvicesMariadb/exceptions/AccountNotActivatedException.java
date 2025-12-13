package BlackAdhuleSystem.dev.userAdvicesMariadb.exceptions;

import org.springframework.security.core.AuthenticationException;

public class AccountNotActivatedException extends AuthenticationException {
    public AccountNotActivatedException(String msg) {
        super(msg);
    }
}
