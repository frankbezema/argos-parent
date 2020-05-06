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
package com.rabobank.argos.service.adapter.in.rest.layout;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.domain.layout.ApprovalConfiguration;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestApprovalConfiguration;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

class ApprovalConfigurationMapperTest {

    private ApprovalConfigurationMapper approvalConfigMapper;
    private ObjectMapper mapper;
    private String approvalConfigJson;

    @BeforeEach
    void setup() throws IOException {
        approvalConfigMapper = Mappers.getMapper(ApprovalConfigurationMapper.class);
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        approvalConfigJson = IOUtils.toString(getClass().getResourceAsStream("/approval-config.json"), UTF_8);

    }

    @Test
    void shouldConvertCorrectLy() throws JsonProcessingException, JSONException {
        ApprovalConfiguration approvalConfiguration = approvalConfigMapper.convertFromRestApprovalConfiguration(mapper.readValue(approvalConfigJson, RestApprovalConfiguration.class));
        RestApprovalConfiguration restApprovalConfiguration = approvalConfigMapper.convertToRestApprovalConfiguration(approvalConfiguration);
        JSONAssert.assertEquals(approvalConfigJson, mapper.writeValueAsString(restApprovalConfiguration), true);
    }

}