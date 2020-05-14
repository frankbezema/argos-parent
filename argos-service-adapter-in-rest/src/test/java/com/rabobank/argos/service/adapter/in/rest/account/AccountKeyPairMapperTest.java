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
import com.rabobank.argos.domain.account.ServiceAccountKeyPair;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestServiceAccountKeyPair;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountKeyPairMapperTest {

    private AccountKeyPairMapper converter;
    private ObjectMapper mapper;
    private String keyPairJson;
    private byte[] bytePublicKey;
    private String base64EncodedPublicKey;

    @Mock
    private PasswordEncoder passwordEncoder;


    @BeforeEach
    void setUp() throws IOException {
        converter = Mappers.getMapper(AccountKeyPairMapper.class);
        ReflectionTestUtils.setField(converter, "passwordEncoder", passwordEncoder);
        mapper = new ObjectMapper();
        keyPairJson = IOUtils.toString(getClass().getResourceAsStream("/keypair.json"), UTF_8);
        base64EncodedPublicKey = IOUtils.toString(getClass().getResourceAsStream("/testkey.pub"), UTF_8);
        bytePublicKey = Base64.getDecoder().decode(base64EncodedPublicKey);
    }

    @Test
    void serviceAccountKeyPair() throws IOException, JSONException {
        when(passwordEncoder.encode("hashedKeyPassphrase")).thenReturn("encodedHashedKeyPassphrase");
        String saPairJson = IOUtils.toString(getClass().getResourceAsStream("/sa-keypair.json"), UTF_8);
        RestServiceAccountKeyPair restSaKeyPair = mapper.readValue(saPairJson, RestServiceAccountKeyPair.class);
        ServiceAccountKeyPair saKeyPair = converter.convertFromRestKeyPair(restSaKeyPair);
        assertThat(saKeyPair.getEncryptedHashedKeyPassphrase(), is("encodedHashedKeyPassphrase"));
        RestServiceAccountKeyPair restServiceAccountKeyPair = converter.convertToRestKeyPair(saKeyPair);
        assertThat(restServiceAccountKeyPair.getHashedKeyPassphrase(), nullValue());
        restServiceAccountKeyPair.setHashedKeyPassphrase("hashedKeyPassphrase");
        JSONAssert.assertEquals(saPairJson, mapper.writeValueAsString(restServiceAccountKeyPair), true);

    }

    @Test
    void convertFromRestKeyPair() throws JsonProcessingException, JSONException {
        KeyPair keyPair = converter.convertFromRestKeyPair(mapper.readValue(keyPairJson, RestKeyPair.class));
        JSONAssert.assertEquals(keyPairJson, mapper.writeValueAsString(converter.convertToRestKeyPair(keyPair)), true);
    }

    @Test
    void convertByteArrayToPublicKey() {
        PublicKey publicKey = converter.convertByteArrayToPublicKey(bytePublicKey);
        assertEquals(base64EncodedPublicKey, Base64.getEncoder().encodeToString(converter.convertPublicKeyToByteArray(publicKey)));
    }
}
