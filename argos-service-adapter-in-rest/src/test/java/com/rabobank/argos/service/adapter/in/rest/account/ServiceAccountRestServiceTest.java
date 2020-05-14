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
package com.rabobank.argos.service.adapter.in.rest.account;

import com.rabobank.argos.domain.account.ServiceAccount;
import com.rabobank.argos.domain.account.ServiceAccountKeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestServiceAccount;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestServiceAccountKeyPair;
import com.rabobank.argos.service.domain.account.AccountService;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import com.rabobank.argos.service.domain.security.AccountSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceAccountRestServiceTest {

    private static final String PARENT_LABEL_ID = "parentLabelId";
    private static final String ACCOUNT_ID = "accountId";

    @Mock
    private ServiceAccountMapper accountMapper;

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private AccountKeyPairMapper keyPairMapper;

    @Mock
    private RestServiceAccount restServiceAccount;

    private ServiceAccountRestService service;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ServiceAccount serviceAccount;

    @Mock
    private RestServiceAccountKeyPair restKeyPair;

    @Mock
    private ServiceAccountKeyPair keyPair;

    @Mock
    private AccountService accountService;

    @Mock
    private AccountSecurityContext accountSecurityContext;

    @BeforeEach
    void setUp() {
        service = new ServiceAccountRestService(accountMapper, labelRepository, keyPairMapper, accountService, accountSecurityContext);
    }

    @Test
    void createServiceAccount() {
        when(restServiceAccount.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(true);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(accountMapper.convertFromRestServiceAccount(restServiceAccount)).thenReturn(serviceAccount);
        when(accountMapper.convertToRestServiceAccount(serviceAccount)).thenReturn(restServiceAccount);
        ResponseEntity<RestServiceAccount> response = service.createServiceAccount(restServiceAccount);
        assertThat(response.getStatusCodeValue(), is(201));
        assertThat(response.getBody(), sameInstance(restServiceAccount));
        assertThat(response.getHeaders().getLocation(), notNullValue());
        verify(accountService).save(serviceAccount);
    }

    @Test
    void createServiceAccountParentLabelIdDoesNotExist() {
        when(restServiceAccount.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.createServiceAccount(restServiceAccount));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"parent label id not found : parentLabelId\""));
    }

    @Test
    void createServiceAccountKeyById() {
        when(accountService.activateNewKey(ACCOUNT_ID, keyPair)).thenReturn(Optional.of(serviceAccount));
        when(serviceAccount.getActiveKeyPair()).thenReturn(keyPair);
        when(keyPairMapper.convertToRestKeyPair(keyPair)).thenReturn(restKeyPair);
        when(keyPairMapper.convertFromRestKeyPair(restKeyPair)).thenReturn(keyPair);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<RestServiceAccountKeyPair> response = service.createServiceAccountKeyById(ACCOUNT_ID, restKeyPair);
        assertThat(response.getStatusCodeValue(), is(201));
        assertThat(response.getBody(), sameInstance(restKeyPair));
        assertThat(response.getHeaders().getLocation(), notNullValue());
        verify(accountService).activateNewKey(ACCOUNT_ID, keyPair);
    }

    @Test
    void createServiceAccountKeyByIdAccountNotFound() {
        when(keyPairMapper.convertFromRestKeyPair(restKeyPair)).thenReturn(keyPair);
        when(accountService.activateNewKey(ACCOUNT_ID, keyPair)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.createServiceAccountKeyById(ACCOUNT_ID, restKeyPair));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no service account with id : accountId found\""));
    }

    @Test
    void getServiceAccountKeyById() {
        when(accountService.findServiceAccountById(ACCOUNT_ID)).thenReturn(Optional.of(serviceAccount));
        when(serviceAccount.getActiveKeyPair()).thenReturn(keyPair);
        when(keyPairMapper.convertToRestKeyPair(keyPair)).thenReturn(restKeyPair);
        ResponseEntity<RestServiceAccountKeyPair> response = service.getServiceAccountKeyById(ACCOUNT_ID);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restKeyPair));
    }

    @Test
    void getServiceAccountKeyByIdAccountNotFound() {
        when(accountService.findServiceAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getServiceAccountKeyById(ACCOUNT_ID));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no active service account key with id : accountId found\""));
    }

    @Test
    void getServiceAccountKeyByIdNoActiveKey() {
        when(accountService.findServiceAccountById(ACCOUNT_ID)).thenReturn(Optional.of(serviceAccount));
        when(serviceAccount.getActiveKeyPair()).thenReturn(null);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getServiceAccountKeyById(ACCOUNT_ID));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no active service account key with id : accountId found\""));
    }

    @Test
    void getServiceAccountById() {
        when(accountService.findServiceAccountById(ACCOUNT_ID)).thenReturn(Optional.of(serviceAccount));
        when(accountMapper.convertToRestServiceAccount(serviceAccount)).thenReturn(restServiceAccount);
        ResponseEntity<RestServiceAccount> response = service.getServiceAccountById(ACCOUNT_ID);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restServiceAccount));
    }

    @Test
    void getServiceAccountByIdAccountNotFound() {
        when(accountService.findServiceAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getServiceAccountById(ACCOUNT_ID));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no service account with id : accountId found\""));
    }

    @Test
    void updateServiceAccountById() {
        when(restServiceAccount.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(true);
        when(accountMapper.convertFromRestServiceAccount(restServiceAccount)).thenReturn(serviceAccount);
        when(accountService.update(ACCOUNT_ID, serviceAccount)).thenReturn(Optional.of(serviceAccount));
        when(accountMapper.convertToRestServiceAccount(serviceAccount)).thenReturn(restServiceAccount);
        ResponseEntity<RestServiceAccount> response = service.updateServiceAccountById(ACCOUNT_ID, restServiceAccount);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restServiceAccount));
    }

    @Test
    void updateServiceAccountByIdAccountNotFound() {
        when(restServiceAccount.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(true);
        when(accountMapper.convertFromRestServiceAccount(restServiceAccount)).thenReturn(serviceAccount);
        when(accountService.update(ACCOUNT_ID, serviceAccount)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.updateServiceAccountById(ACCOUNT_ID, restServiceAccount));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no service account with id : accountId found\""));
    }

    @Test
    void updateServiceAccountByIdParentLabelIdNotFound() {
        when(restServiceAccount.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.updateServiceAccountById(ACCOUNT_ID, restServiceAccount));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"parent label id not found : parentLabelId\""));
    }

    @Test
    void getServiceAccountKey() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(serviceAccount));
        when(serviceAccount.getActiveKeyPair()).thenReturn(keyPair);
        when(keyPairMapper.convertToRestKeyPair(keyPair)).thenReturn(restKeyPair);
        ResponseEntity<RestServiceAccountKeyPair> response = service.getServiceAccountKey();
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restKeyPair));
    }

    @Test
    void getServiceAccountKeyNotFound() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getServiceAccountKey());
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no active service account key found\""));
    }

    @Test
    void getServiceAccountKeyNoActiveKey() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(serviceAccount));
        when(serviceAccount.getActiveKeyPair()).thenReturn(null);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getServiceAccountKey());
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no active service account key found\""));
    }
}