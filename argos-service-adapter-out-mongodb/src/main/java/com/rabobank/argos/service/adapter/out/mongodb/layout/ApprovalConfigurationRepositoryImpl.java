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
package com.rabobank.argos.service.adapter.out.mongodb.layout;

import com.mongodb.client.result.UpdateResult;
import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.hierarchy.Label;
import com.rabobank.argos.domain.layout.ApprovalConfiguration;
import com.rabobank.argos.service.domain.layout.ApprovalConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApprovalConfigurationRepositoryImpl implements ApprovalConfigurationRepository {
    static final String COLLECTION = "approvalConfigurations";
    static final String SUPPLYCHAIN_ID_FIELD = "supplyChainId";
    static final String SEGMENT_NAME_FIELD = "segmentName";
    static final String STEP_NAME_FIELD = "stepName";
    static final String APPROVAL_CONFIG_ID_FIELD = "approvalConfigurationId";
    private final MongoTemplate template;

    @Override
    public void save(ApprovalConfiguration approvalConfiguration) {
        template.save(approvalConfiguration, COLLECTION);
    }

    public Optional<ApprovalConfiguration> findBySupplyChainIdSegmentNameAndStepName(String supplyChainId, String segmentName, String stepName) {
        Criteria criteria = Criteria.where(SUPPLYCHAIN_ID_FIELD).is(supplyChainId);
        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(Criteria.where(SEGMENT_NAME_FIELD).is(segmentName));
        andCriteria.add(Criteria.where(STEP_NAME_FIELD).is(stepName));
        criteria.andOperator(andCriteria.toArray(new Criteria[0]));
        Query query = new Query(criteria);
        return Optional.ofNullable(template.findOne(query, ApprovalConfiguration.class, COLLECTION));
    }

    @Override
    public Optional<ApprovalConfiguration> findById(String approvalConfigurationId) {
        Query query = primaryKeyQuery(approvalConfigurationId);
        return Optional.ofNullable(template.findOne(query, ApprovalConfiguration.class, COLLECTION));
    }

    @Override
    public Optional<ApprovalConfiguration> update(ApprovalConfiguration approvalConfiguration) {
        Query query = primaryKeyQuery(approvalConfiguration.getApprovalConfigurationId());
        Document document = new Document();
        template.getConverter().write(approvalConfiguration, document);
        try {
            UpdateResult updateResult = template.updateFirst(query, Update.fromDocument(document), Label.class, COLLECTION);
            if (updateResult.getMatchedCount() > 0) {
                return Optional.of(approvalConfiguration);
            } else {
                return Optional.empty();
            }
        } catch (DuplicateKeyException e) {
            throw duplicateKeyException(approvalConfiguration, e);
        }
    }

    @Override
    public List<ApprovalConfiguration> findBySupplyChainId(String supplyChainId) {
        Criteria criteria = Criteria.where(SUPPLYCHAIN_ID_FIELD).is(supplyChainId);
        return template.find(new Query(criteria), ApprovalConfiguration.class, COLLECTION);
    }

    @Override
    public void delete(String approvalConfigurationId) {
        template.remove(primaryKeyQuery(approvalConfigurationId), ApprovalConfiguration.class, COLLECTION);
    }

    private Query primaryKeyQuery(String approvalConfigurationId) {
        Criteria criteria = Criteria.where(APPROVAL_CONFIG_ID_FIELD).is(approvalConfigurationId);
        return new Query(criteria);
    }

    private ArgosError duplicateKeyException(ApprovalConfiguration approvalConfiguration, DuplicateKeyException e) {
        return new ArgosError("approvalConfiguration with supplychain id: " + approvalConfiguration.getSupplyChainId() + " segmentName: " + approvalConfiguration.getSegmentName() + "and StepName:" + approvalConfiguration.getStepName() + "already exists", e, ArgosError.Level.WARNING);
    }

}
