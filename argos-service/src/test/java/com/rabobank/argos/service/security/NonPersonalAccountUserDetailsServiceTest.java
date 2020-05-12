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

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.account.ServiceAccount;
import com.rabobank.argos.service.domain.account.ServiceAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceAccountUserDetailsServiceTest {
    private static final String USER_NAME = "test";
    private static final String KEY_ID = "keyId";
    @Mock
    private ServiceAccountRepository serviceAccountRepository;
    private ServiceAccount serviceAccount = ServiceAccount.builder().name(USER_NAME).build();
    private ServiceAccountUserDetailsService serviceAccountUserDetailsService;

    @BeforeEach
    void setup() {
        serviceAccountUserDetailsService = new ServiceAccountUserDetailsService(serviceAccountRepository);
    }

    @Test
    void loadUserByIdWithValidIdShouldReturnUserdetails() {
        when(serviceAccountRepository.findByActiveKeyId(anyString())).thenReturn(Optional.of(serviceAccount));
        UserDetails userDetails = serviceAccountUserDetailsService.loadUserById(KEY_ID);
        assertThat(userDetails.getUsername(), is(USER_NAME));
    }

    @Test
    void loadUserByIdWithInValidIdShouldReturnError() {
        when(serviceAccountRepository.findByActiveKeyId(anyString())).thenReturn(Optional.empty());
        Exception exception = assertThrows(ArgosError.class, () -> serviceAccountUserDetailsService.loadUserById("keyId"));
        assertThat(exception.getMessage(), is("Non personal account with keyid keyId not found"));
    }

}