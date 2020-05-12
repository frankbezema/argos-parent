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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.domain.account.ServiceAccount;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestServiceAccount;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.is;

class ServiceAccountMapperTest {

    private ServiceAccountMapper converter;
    private ObjectMapper mapper;
    private String linkJson;

    @BeforeEach
    void setUp() throws IOException {
        converter = Mappers.getMapper(ServiceAccountMapper.class);
        mapper = new ObjectMapper();
        linkJson = IOUtils.toString(ServiceAccountMapperTest.class.getResourceAsStream("/service-account.json"), StandardCharsets.UTF_8);
    }

    @Test
    void convertFromRestLinkMetaBlock() throws JsonProcessingException, JSONException {
        ServiceAccount serviceAccount = converter.convertFromRestServiceAccount(mapper.readValue(linkJson, RestServiceAccount.class));
        RestServiceAccount restServiceAccount = converter.convertToRestServiceAccount(serviceAccount);
        assertThat(restServiceAccount.getId(), is(serviceAccount.getAccountId()));
        assertThat(restServiceAccount.getId(), hasLength(36));
        restServiceAccount.setId("accountId");
        JSONAssert.assertEquals(linkJson, mapper.writeValueAsString(restServiceAccount), true);
    }
}
