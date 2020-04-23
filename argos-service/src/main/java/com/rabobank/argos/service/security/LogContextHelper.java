package com.rabobank.argos.service.security;

import brave.propagation.ExtraFieldPropagation;
import com.rabobank.argos.service.domain.security.AccountUserDetailsAdapter;
import org.slf4j.MDC;

public class LogContextHelper {
    private LogContextHelper() {
    }

    public static void addAccountInfoToLogContext(AccountUserDetailsAdapter userDetails) {
        ExtraFieldPropagation.set("accountId", userDetails.getAccount().getAccountId());
        MDC.put("accountId", userDetails.getAccount().getAccountId());
        ExtraFieldPropagation.set("accountName", userDetails.getAccount().getAccountId());
        MDC.put("accountName", userDetails.getAccount().getName());
    }
}
