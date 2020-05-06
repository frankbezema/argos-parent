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

import com.rabobank.argos.domain.layout.ApprovalConfiguration;
import com.rabobank.argos.service.domain.layout.ApprovalConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApprovalConfigurationRepositoryImpl implements ApprovalConfigurationRepository {
    protected static final String COLLECTION = "approvalConfigurations";
    protected static final String SUPPLYCHAIN_ID_FIELD = "supplyChainId";
    protected static final String SEGMENT_NAME_FIELD = "segmentName";
    protected static final String STEP_NAME_FIELD = "stepName";
    protected static final String APPROVAL_CONFIG_ID_FIELD = "approvalConfigurationId";
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
        criteria.andOperator(andCriteria.toArray(new Criteria[andCriteria.size()]));
        Query query = new Query(criteria);
        return Optional.ofNullable(template.findOne(query, ApprovalConfiguration.class, COLLECTION));
    }
}
