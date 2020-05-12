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
package com.rabobank.argos.service.adapter.out.mongodb.account;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.account.ServiceAccount;
import com.rabobank.argos.service.domain.account.ServiceAccountRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ServiceAccountRepositoryImpl implements ServiceAccountRepository {

    static final String COLLECTION = "serviceAccounts";
    static final String ACCOUNT_ID_FIELD = "accountId";
    static final String ACCOUNT_NAME_FIELD = "name";
    static final String ACTIVE_KEY_ID_FIELD = "activeKeyPair.keyId";
    static final String PARENT_LABEL_ID_FIELD = "parentLabelId";
    private final MongoTemplate template;

    @Override
    public void save(ServiceAccount account) {
        try {
            template.save(account, COLLECTION);
        } catch (DuplicateKeyException e) {
            throw duplicateKeyException(account, e);
        }
    }

    @Override
    public Optional<ServiceAccount> findById(String id) {
        return Optional.ofNullable(template.findOne(getPrimaryKeyQuery(id), ServiceAccount.class, COLLECTION));
    }

    @Override
    public Optional<ServiceAccount> findByActiveKeyId(String activeKeyId) {
        return Optional.ofNullable(template.findOne(getActiveKeyQuery(activeKeyId), ServiceAccount.class, COLLECTION));
    }

    @Override
    public void update(ServiceAccount account) {
        Query query = getPrimaryKeyQuery(account.getAccountId());
        Document document = new Document();
        template.getConverter().write(account, document);
        try {
            template.updateFirst(query, Update.fromDocument(document), ServiceAccount.class, COLLECTION);
        } catch (DuplicateKeyException e) {
            throw duplicateKeyException(account, e);
        }
    }

    @Override
    public boolean activeKeyExists(String activeKeyId) {
        return template.exists(getActiveKeyQuery(activeKeyId), ServiceAccount.class, COLLECTION);
    }

    @Override
    public Optional<String> findParentLabelIdByAccountId(String accountId) {
        Query query = getPrimaryKeyQuery(accountId);
        query.fields().include(PARENT_LABEL_ID_FIELD);
        return Optional.ofNullable(template.findOne(query, ServiceAccount.class, COLLECTION)).map(ServiceAccount::getParentLabelId);
    }

    private Query getActiveKeyQuery(String activekeyId) {
        return new Query(Criteria.where(ACTIVE_KEY_ID_FIELD).is(activekeyId));
    }

    private Query getPrimaryKeyQuery(String id) {
        return new Query(Criteria.where(ACCOUNT_ID_FIELD).is(id));
    }

    private ArgosError duplicateKeyException(ServiceAccount account, DuplicateKeyException e) {
        return new ArgosError("service account with name: " + account.getName() + " and parentLabelId: " + account.getParentLabelId() + " already exists",
                e, ArgosError.Level.WARNING);
    }

}
