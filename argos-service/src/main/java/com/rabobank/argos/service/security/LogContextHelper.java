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

import com.rabobank.argos.service.domain.security.AccountUserDetailsAdapter;
import org.slf4j.MDC;

import java.util.UUID;

public class LogContextHelper {

    void addAccountInfoToLogContext(AccountUserDetailsAdapter userDetails) {
        MDC.put("accountId", userDetails.getAccount().getAccountId());
        MDC.put("accountName", userDetails.getAccount().getName());
    }

    void addTraceIdToLogContext() {
        MDC.put("traceId", UUID.randomUUID().toString());
    }
}
