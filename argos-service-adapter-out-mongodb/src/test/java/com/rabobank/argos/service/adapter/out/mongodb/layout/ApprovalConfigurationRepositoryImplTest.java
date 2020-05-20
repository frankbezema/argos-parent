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
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

import static com.rabobank.argos.service.adapter.out.mongodb.layout.ApprovalConfigurationRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalConfigurationRepositoryImplTest {
    @Mock
    private MongoTemplate template;

    private ApprovalConfigurationRepositoryImpl approvalConfigurationRepository;

    @Mock
    private ApprovalConfiguration approvalConfiguration;
    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @BeforeEach
    void setup() {
        approvalConfigurationRepository = new ApprovalConfigurationRepositoryImpl(template);
    }

    @Test
    void findBySupplyChainIdSegmentNameAndStepName() {
        when(template.findOne(any(), eq(ApprovalConfiguration.class), eq(COLLECTION))).thenReturn(approvalConfiguration);
        assertThat(approvalConfigurationRepository.findBySupplyChainIdSegmentNameAndStepName("supplyChain", "segmentName", "stepName"), is(Optional.of(approvalConfiguration)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(ApprovalConfiguration.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChain\", \"$and\" : [{ \"segmentName\" : \"segmentName\"}, { \"stepName\" : \"stepName\"}]}, Fields: {}, Sort: {}"));
    }


    @Test
    void findBySupplyChainId() {
        when(template.find(any(Query.class), eq(ApprovalConfiguration.class), eq(COLLECTION))).thenReturn(List.of(approvalConfiguration));
        List<ApprovalConfiguration> approvalConfigurations = approvalConfigurationRepository.findBySupplyChainId("supplyChain");
        assertThat(approvalConfigurations, hasSize(1));
        verify(template).find(queryArgumentCaptor.capture(), eq(ApprovalConfiguration.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"supplyChainId\" : \"supplyChain\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void saveAll() {
        approvalConfigurationRepository.saveAll("supplyChain", List.of(approvalConfiguration));
        verify(template).remove(queryArgumentCaptor.capture(), eq(COLLECTION));
        verify(template).insert(List.of(approvalConfiguration), COLLECTION);
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"supplyChainId\" : \"supplyChain\"}, Fields: {}, Sort: {}"));
    }

}