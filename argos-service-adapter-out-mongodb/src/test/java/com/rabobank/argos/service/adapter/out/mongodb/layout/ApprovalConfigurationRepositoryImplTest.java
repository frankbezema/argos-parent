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
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Optional;

import static com.rabobank.argos.service.adapter.out.mongodb.layout.ApprovalConfigurationRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Mock
    private MongoConverter converter;

    @Mock
    private UpdateResult updateResult;

    @Mock
    private DuplicateKeyException duplicateKeyException;


    @Captor
    private ArgumentCaptor<Update> updateArgumentCaptor;


    @BeforeEach
    void setup() {
        approvalConfigurationRepository = new ApprovalConfigurationRepositoryImpl(template);

    }

    @Test
    void save() {
        approvalConfigurationRepository.save(approvalConfiguration);
        verify(template).save(approvalConfiguration, COLLECTION);

    }

    @Test
    void findBySupplyChainIdSegmentNameAndStepName() {
        when(template.findOne(any(), eq(ApprovalConfiguration.class), eq(COLLECTION))).thenReturn(approvalConfiguration);
        assertThat(approvalConfigurationRepository.findBySupplyChainIdSegmentNameAndStepName("supplyChain", "segmentName", "stepName"), is(Optional.of(approvalConfiguration)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(ApprovalConfiguration.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChain\", \"$and\" : [{ \"segmentName\" : \"segmentName\"}, { \"stepName\" : \"stepName\"}]}, Fields: {}, Sort: {}"));
    }

    @Test
    void findById() {
        when(template.findOne(any(), eq(ApprovalConfiguration.class), eq(COLLECTION))).thenReturn(approvalConfiguration);
        assertThat(approvalConfigurationRepository.findById("id"), is(Optional.of(approvalConfiguration)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(ApprovalConfiguration.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"approvalConfigurationId\" : \"id\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void updateFound() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(Label.class), eq(COLLECTION))).thenReturn(updateResult);
        when(updateResult.getMatchedCount()).thenReturn(1L);
        when(approvalConfiguration.getApprovalConfigurationId()).thenReturn("id");
        Optional<ApprovalConfiguration> update = approvalConfigurationRepository.update(approvalConfiguration);
        assertThat(update, Matchers.is(Optional.of(approvalConfiguration)));
        verify(template).updateFirst(queryArgumentCaptor.capture(), updateArgumentCaptor.capture(), eq(Label.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"approvalConfigurationId\" : \"id\"}, Fields: {}, Sort: {}"));
        verify(converter).write(eq(approvalConfiguration), any());
        assertThat(updateArgumentCaptor.getValue().toString(), Matchers.is("{}"));
    }

    @Test
    void updateNotFound() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(Label.class), eq(COLLECTION))).thenReturn(updateResult);
        when(updateResult.getMatchedCount()).thenReturn(0L);
        when(approvalConfiguration.getApprovalConfigurationId()).thenReturn("id");
        Optional<ApprovalConfiguration> update = approvalConfigurationRepository.update(approvalConfiguration);
        assertThat(update.isEmpty(), Matchers.is(true));
        verify(template).updateFirst(queryArgumentCaptor.capture(), updateArgumentCaptor.capture(), eq(Label.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"approvalConfigurationId\" : \"id\"}, Fields: {}, Sort: {}"));
        verify(converter).write(eq(approvalConfiguration), any());
        assertThat(updateArgumentCaptor.getValue().toString(), Matchers.is("{}"));
    }

    @Test
    void updateDuplicateKeyException() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(Label.class), eq(COLLECTION))).thenThrow(duplicateKeyException);
        ArgosError argosError = assertThrows(ArgosError.class, () -> approvalConfigurationRepository.update(approvalConfiguration));
        assertThat(argosError.getCause(), sameInstance(duplicateKeyException));
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


    @Test
    void deleteById() {
        approvalConfigurationRepository.delete("id");
        verify(template).remove(queryArgumentCaptor.capture(), eq(ApprovalConfiguration.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"approvalConfigurationId\" : \"id\"}, Fields: {}, Sort: {}"));
    }

}