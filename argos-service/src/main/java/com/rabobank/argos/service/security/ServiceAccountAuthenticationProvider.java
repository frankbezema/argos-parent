/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.service.security;

import com.rabobank.argos.domain.account.ServiceAccountKeyPair;
import com.rabobank.argos.service.domain.security.AccountUserDetailsAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@RequiredArgsConstructor
public class ServiceAccountAuthenticationProvider implements AuthenticationProvider {

    private static final String NOT_AUTHENTICATED = "not authenticated";
    private final ServiceAccountUserDetailsService serviceAccountUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final LogContextHelper logContextHelper;

    @Override
    public Authentication authenticate(Authentication notAuthenticatedServiceAccount) {
        ServiceAccountAuthenticationToken serviceAccountAuthenticationToken = (ServiceAccountAuthenticationToken) notAuthenticatedServiceAccount;
        try {
            AccountUserDetailsAdapter userDetails = (AccountUserDetailsAdapter) serviceAccountUserDetailsService
                    .loadUserById(serviceAccountAuthenticationToken.getServiceAccountCredentials().getKeyId());
            log.debug("successfully found service account by key id {}", userDetails.getUsername());
            String password = serviceAccountAuthenticationToken.getServiceAccountCredentials().getPassword();
            ServiceAccountKeyPair serviceAccountKeyPair = (ServiceAccountKeyPair) userDetails.getAccount().getActiveKeyPair();
            if (passwordEncoder.matches(password, serviceAccountKeyPair.getEncryptedHashedKeyPassphrase())) {
                log.debug("successfully authenticated service account {}", userDetails.getUsername());
                logContextHelper.addAccountInfoToLogContext(userDetails);
                return new ServiceAccountAuthenticationToken(serviceAccountAuthenticationToken.getServiceAccountCredentials(),
                        userDetails,
                        userDetails.getAuthorities());
            } else {
                log.warn("invalid access attempt {}", serviceAccountAuthenticationToken);
                throw new BadCredentialsException(NOT_AUTHENTICATED);
            }
        } catch (Exception ex) {
            log.warn("invalid access attempt {}", serviceAccountAuthenticationToken);
            throw new BadCredentialsException(NOT_AUTHENTICATED);
        }
    }


    @Override
    public boolean supports(Class<?> authenticationTokenClass) {
        return authenticationTokenClass.equals(ServiceAccountAuthenticationToken.class);
    }
}
