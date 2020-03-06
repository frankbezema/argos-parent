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
package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.domain.permission.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;

@Component
public class AccountSecurityContextImpl implements AccountSecurityContext {

    @Override
    public Optional<Account> getAuthenticatedAccount() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .map(authentication -> (AccountUserDetailsAdapter) authentication)
                .map(AccountUserDetailsAdapter::getAccount);
    }

    @Override
    public Set<Permission> getGlobalPermission() {
        AccountUserDetailsAdapter authentication = (AccountUserDetailsAdapter) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        if (authentication != null) {
            return authentication.getGlobalPermissions();
        } else {
            return emptySet();
        }

    }
}
