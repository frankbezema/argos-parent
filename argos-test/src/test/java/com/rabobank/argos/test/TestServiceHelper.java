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
package com.rabobank.argos.test;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.argos4j.internal.ArgosServiceClient;
import com.rabobank.argos.argos4j.rest.api.client.PersonalAccountApi;
import com.rabobank.argos.argos4j.rest.api.client.ServiceAccountApi;
import com.rabobank.argos.argos4j.rest.api.model.RestKeyPair;
import com.rabobank.argos.argos4j.rest.api.model.RestLabel;
import com.rabobank.argos.argos4j.rest.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.argos4j.rest.api.model.RestPersonalAccount;
import com.rabobank.argos.argos4j.rest.api.model.RestServiceAccount;
import com.rabobank.argos.argos4j.rest.api.model.RestServiceAccountKeyPair;
import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.test.rest.api.ApiClient;
import com.rabobank.argos.test.rest.api.client.IntegrationTestServiceApi;
import com.rabobank.argos.test.rest.api.model.TestLayoutMetaBlock;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mapstruct.factory.Mappers;

import java.io.IOException;
import java.util.List;

import static com.rabobank.argos.argos4j.rest.api.model.RestPermission.LAYOUT_ADD;
import static com.rabobank.argos.argos4j.rest.api.model.RestPermission.LINK_ADD;
import static com.rabobank.argos.argos4j.rest.api.model.RestPermission.READ;
import static com.rabobank.argos.argos4j.rest.api.model.RestPermission.SERVICE_ACCOUNT_EDIT;
import static com.rabobank.argos.argos4j.rest.api.model.RestPermission.VERIFY;
import static com.rabobank.argos.test.ServiceStatusHelper.getHierarchyApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getLayoutApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getPersonalAccountApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getServiceAccountApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getToken;

class TestServiceHelper {

    private static final String DEFAULT_USER_2 = "Default User2";
    private static final String DEFAULT_USER1 = "Default User";
    private static Properties properties = Properties.getInstance();

    static void clearDatabase() {
        getTestApi().resetDatabase();
    }

    private static IntegrationTestServiceApi getTestApi() {
        return getApiClient().buildClient(IntegrationTestServiceApi.class);
    }

    private static ApiClient getApiClient() {
        return new ApiClient().setBasePath(properties.getIntegrationTestServiceBaseUrl() + "/integration-test");
    }

    static DefaultTestData createDefaultTestData() {
        getTestApi().resetDatabaseAll();
        DefaultTestData defaultTestData = new DefaultTestData();
        defaultTestData.setAdminToken(getToken("Luke Skywalker", "Skywalker", "luke@skywalker.imp"));
        createDefaultRootLabel(defaultTestData);
        createDefaultPersonalAccount(defaultTestData);
        createDefaultSaAccounts(defaultTestData);
        return defaultTestData;
    }

    private static void createDefaultRootLabel(DefaultTestData hierarchy) {
        hierarchy.setDefaultRootLabel(getHierarchyApi(hierarchy.getAdminToken()).createLabel(new RestLabel().name("default_root_label")));
    }

    private static void createDefaultPersonalAccount(DefaultTestData defaultTestData) {
        String defaultUser1Token = getToken(DEFAULT_USER1, "User", "default@nl.nl");
        PersonalAccountApi personalAccountApi = getPersonalAccountApi(defaultTestData.getAdminToken());
        RestPersonalAccount defaultUser1 = personalAccountApi.searchPersonalAccounts(null, null, DEFAULT_USER1).iterator().next();
        personalAccountApi.updateLocalPermissionsForLabel(defaultUser1.getId(), defaultTestData.getDefaultRootLabel().getId(), List.of(LAYOUT_ADD, READ, VERIFY, SERVICE_ACCOUNT_EDIT, LINK_ADD));

        TestDateKeyPair keyPair = readKeyPair(1);
        getPersonalAccountApi(defaultUser1Token).createKey(new RestKeyPair()
                .encryptedPrivateKey(keyPair.getEncryptedPrivateKey())
                .publicKey(keyPair.getPublicKey())
                .keyId(keyPair.getKeyId()));

        defaultTestData.getPersonalAccounts().put("default-pa1", DefaultTestData.PersonalAccount.builder()
                .passphrase(keyPair.getPassphrase())
                .keyId(keyPair.getKeyId())
                .accountId(defaultUser1.getId())
                .token(defaultUser1Token)
                .publicKey(keyPair.getPublicKey())
                .build());

        String defaultUser2Token = getToken(DEFAULT_USER_2, "User2", "default2@nl.nl");
        RestPersonalAccount defaultUser2 = personalAccountApi.searchPersonalAccounts(null, null, DEFAULT_USER_2).iterator().next();
        defaultTestData.getPersonalAccounts().put("default-pa2", DefaultTestData.PersonalAccount.builder()
                .accountId(defaultUser2.getId())
                .token(defaultUser2Token)
                .build());
    }

    private static void createDefaultSaAccounts(DefaultTestData defaultTestData) {
        createSaWithActiveKey(defaultTestData, readKeyPair(1), "default-sa1");
        createSaWithActiveKey(defaultTestData, readKeyPair(2), "default-sa2");
        createSaWithActiveKey(defaultTestData, readKeyPair(3), "default-sa3");
        createSaWithActiveKey(defaultTestData, readKeyPair(4), "default-sa4");
        createSaWithActiveKey(defaultTestData, readKeyPair(5), "default-sa5");
    }

    private static void createSaWithActiveKey(DefaultTestData defaultTestData, TestDateKeyPair keyPair, String name) {
        ServiceAccountApi serviceAccountApi = getServiceAccountApi(defaultTestData.getPersonalAccounts().get("default-pa1").getToken());
        RestServiceAccount sa = serviceAccountApi.createServiceAccount(new RestServiceAccount().parentLabelId(defaultTestData.getDefaultRootLabel().getId()).name(name));

        String hashedKeyPassphrase = ArgosServiceClient.calculatePassphrase(keyPair.getKeyId(), keyPair.getPassphrase());

        serviceAccountApi.createServiceAccountKeyById(sa.getId(),
                new RestServiceAccountKeyPair().keyId(keyPair.getKeyId())
                        .hashedKeyPassphrase(hashedKeyPassphrase)
                        .encryptedPrivateKey(keyPair.getEncryptedPrivateKey())
                        .publicKey(keyPair.getPublicKey()));
        defaultTestData.getServiceAccount().put(name,
                DefaultTestData.ServiceAccount.builder()
                        .passphrase(keyPair.getPassphrase())
                        .keyId(keyPair.getKeyId())
                        .hashedKeyPassphrase(hashedKeyPassphrase)
                        .publicKey(keyPair.getPublicKey())
                        .build());
    }

    private static TestDateKeyPair readKeyPair(int index) {
        try {
            return new ObjectMapper().readValue(TestServiceHelper.class.getResourceAsStream("/testmessages/key/default-test-keypair" + index + ".json"), TestDateKeyPair.class);
        } catch (IOException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class TestDateKeyPair {
        private String keyId;
        private byte[] publicKey;
        private String passphrase;
        private byte[] encryptedPrivateKey;
    }

    static void signAndStoreLayout(String token, String supplyChainId, RestLayoutMetaBlock restLayout, String keyId, String password) {
        RestMapper mapper = Mappers.getMapper(RestMapper.class);
        TestLayoutMetaBlock testLayout = mapper.mapRestLayout(restLayout);
        TestLayoutMetaBlock signed = getTestApi().signLayout(password, keyId, testLayout);
        getLayoutApi(token).createOrUpdateLayout(supplyChainId, mapper.mapTestLayout(signed));
    }

}
