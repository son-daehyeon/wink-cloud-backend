package com.github.son_daehyeon.common.security.authentication;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

@Configuration
public class UserAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) {

        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {

        return authentication.equals(UserAuthentication.class);
    }
}
