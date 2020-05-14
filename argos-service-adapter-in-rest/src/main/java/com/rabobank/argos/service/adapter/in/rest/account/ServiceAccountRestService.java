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

import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.domain.account.ServiceAccount;
import com.rabobank.argos.domain.account.ServiceAccountKeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.handler.ServiceAccountApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestServiceAccount;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestServiceAccountKeyPair;
import com.rabobank.argos.service.domain.account.AccountService;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import com.rabobank.argos.service.domain.security.AccountSecurityContext;
import com.rabobank.argos.service.domain.security.LabelIdCheckParam;
import com.rabobank.argos.service.domain.security.PermissionCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static com.rabobank.argos.domain.permission.Permission.SERVICE_ACCOUNT_EDIT;
import static com.rabobank.argos.domain.permission.Permission.READ;
import static com.rabobank.argos.service.adapter.in.rest.account.ServiceAccountLabelIdExtractor.SERVICE_ACCOUNT_LABEL_ID_EXTRACTOR;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ServiceAccountRestService implements ServiceAccountApi {

    private final ServiceAccountMapper accountMapper;

    private final LabelRepository labelRepository;

    private final AccountKeyPairMapper keyPairMapper;

    private final AccountService accountService;

    private final AccountSecurityContext accountSecurityContext;

    @Override
    @PermissionCheck(permissions = SERVICE_ACCOUNT_EDIT)
    public ResponseEntity<RestServiceAccount> createServiceAccount(@LabelIdCheckParam(propertyPath = "parentLabelId") RestServiceAccount restServiceAccount) {
        verifyParentLabelExists(restServiceAccount.getParentLabelId());
        ServiceAccount serviceAccount = accountMapper.convertFromRestServiceAccount(restServiceAccount);
        accountService.save(serviceAccount);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{serviceAccountId}")
                .buildAndExpand(serviceAccount.getAccountId())
                .toUri();
        return ResponseEntity.created(location).body(accountMapper.convertToRestServiceAccount(serviceAccount));
    }

    @Override
    @PermissionCheck(permissions = SERVICE_ACCOUNT_EDIT)
    public ResponseEntity<RestServiceAccountKeyPair> createServiceAccountKeyById(@LabelIdCheckParam(dataExtractor = SERVICE_ACCOUNT_LABEL_ID_EXTRACTOR) String serviceAccountId, RestServiceAccountKeyPair restKeyPair) {
        ServiceAccount updatedAccount = accountService.activateNewKey(serviceAccountId, keyPairMapper.convertFromRestKeyPair(restKeyPair))
                .orElseThrow(() -> accountNotFound(serviceAccountId));
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{serviceAccountId}/key")
                .buildAndExpand(serviceAccountId)
                .toUri();
        return ResponseEntity.created(location).body(keyPairMapper.convertToRestKeyPair(((ServiceAccountKeyPair) updatedAccount.getActiveKeyPair())));
    }

    @Override
    @PermissionCheck(permissions = READ)
    public ResponseEntity<RestServiceAccountKeyPair> getServiceAccountKeyById(@LabelIdCheckParam(dataExtractor = SERVICE_ACCOUNT_LABEL_ID_EXTRACTOR) String serviceAccountId) {
        return accountService.findServiceAccountById(serviceAccountId)
                .flatMap(account -> Optional.ofNullable(account.getActiveKeyPair()))
                .map(account -> (ServiceAccountKeyPair) account)
                .map(keyPairMapper::convertToRestKeyPair)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> keyNotFound(serviceAccountId));
    }

    @Override
    @PermissionCheck(permissions = READ)
    public ResponseEntity<RestServiceAccount> getServiceAccountById(@LabelIdCheckParam(dataExtractor = SERVICE_ACCOUNT_LABEL_ID_EXTRACTOR) String serviceAccountId) {
        return accountService.findServiceAccountById(serviceAccountId)
                .map(accountMapper::convertToRestServiceAccount)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> accountNotFound(serviceAccountId));
    }

    @Override
    @PermissionCheck(permissions = SERVICE_ACCOUNT_EDIT)
    public ResponseEntity<RestServiceAccount> updateServiceAccountById(@LabelIdCheckParam(dataExtractor = SERVICE_ACCOUNT_LABEL_ID_EXTRACTOR) String serviceAccountId, RestServiceAccount restServiceAccount) {
        verifyParentLabelExists(restServiceAccount.getParentLabelId());
        ServiceAccount serviceAccount = accountMapper.convertFromRestServiceAccount(restServiceAccount);
        return accountService.update(serviceAccountId, serviceAccount)
                .map(accountMapper::convertToRestServiceAccount)
                .map(ResponseEntity::ok).orElseThrow(() -> accountNotFound(serviceAccountId));
    }

    @Override
    @PreAuthorize("hasRole('NONPERSONAL')")
    public ResponseEntity<RestServiceAccountKeyPair> getServiceAccountKey() {
        return accountSecurityContext.getAuthenticatedAccount()
                .map(Account::getActiveKeyPair).filter(Objects::nonNull)
                .map(keyPair -> (ServiceAccountKeyPair) keyPair)
                .map(keyPairMapper::convertToRestKeyPair)
                .map(ResponseEntity::ok).orElseThrow(this::keyNotFound);
    }

    private void verifyParentLabelExists(String parentLabelId) {
        if (!labelRepository.exists(parentLabelId)) {
            throw parentLabelNotFound(parentLabelId);
        }
    }

    private ResponseStatusException keyNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "no active service account key found");
    }

    private ResponseStatusException keyNotFound(String accountId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "no active service account key with id : " + accountId + " found");
    }

    private ResponseStatusException accountNotFound(String accountId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "no service account with id : " + accountId + " found");
    }

    private ResponseStatusException parentLabelNotFound(String parentLabelId) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "parent label id not found : " + parentLabelId);
    }
}
