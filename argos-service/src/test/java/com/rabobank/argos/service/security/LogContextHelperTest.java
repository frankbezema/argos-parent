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

import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.service.domain.security.AccountUserDetailsAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogContextHelperTest {
    private static final String ACCOUNT_ID = "accountId";
    private static final String ACCOUNT_NAME = "accountName";
    @Mock
    private AccountUserDetailsAdapter accountUserDetailsAdapter;
    @Mock
    private Account account;


    private LogContextHelper logContextHelper;

    @BeforeEach
    void setup() {
        logContextHelper = new LogContextHelper();


    }

    @Test
    void addAccountInfoToLogContext() {
        when(accountUserDetailsAdapter.getAccount()).thenReturn(account);
        when(account.getAccountId()).thenReturn(ACCOUNT_ID);
        when(account.getName()).thenReturn(ACCOUNT_NAME);
        logContextHelper.addAccountInfoToLogContext(accountUserDetailsAdapter);
        verify(account, times(1)).getName();
        verify(account, times(1)).getAccountId();
        assertThat(MDC.get(ACCOUNT_ID), is(ACCOUNT_ID));
        assertThat(MDC.get(ACCOUNT_NAME), is(ACCOUNT_NAME));
    }

    @Test
    void addTraceIdToLogContext() {
        logContextHelper.addTraceIdToLogContext();
        assertThat(MDC.get("traceId"), matchesPattern("[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}"));
    }

    @AfterEach
    void removeFromMDC() {
        MDC.clear();
    }
}