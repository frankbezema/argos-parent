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
package com.rabobank.argos.argos4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.rabobank.argos.argos4j.rest.api.model.RestKeyPair;
import com.rabobank.argos.domain.key.KeyIdProviderImpl;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Argos4jTest {

    public static final char[] KEY_PASSPHRASE = "password".toCharArray();
    private Argos4j argos4j;
    private WireMockServer wireMockServer;

    @TempDir
    static File sharedTempDir;
    private String keyId;
    private String restKeyPairRest;

    @BeforeAll
    static void setUpBefore() throws IOException {
        FileUtils.write(new File(sharedTempDir, "text.txt"), "cool dit\r\nan other line", "UTF-8");
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() throws IOException, NoSuchAlgorithmException {
        Integer randomPort = findRandomPort();
        wireMockServer = new WireMockServer(randomPort);
        wireMockServer.start();

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        keyId = new KeyIdProviderImpl().computeKeyId(pair.getPublic());

        RestKeyPair restKeyPair = new RestKeyPair()
                .keyId(keyId).publicKey(pair.getPublic().getEncoded())
                .encryptedPrivateKey(EncryptionHelper
                        .addPassword(pair.getPrivate().getEncoded(), KEY_PASSPHRASE));

        restKeyPairRest = new ObjectMapper().writeValueAsString(restKeyPair);

        Argos4jSettings settings = Argos4jSettings.builder()
                .argosServerBaseUrl("http://localhost:" + randomPort + "/api")
                .stepName("build")
                .supplyChainName("supplyChainName")
                .layoutSegmentName("layoutSegmentName")
                .signingKeyId(keyId)
                .runId("runId")
                .build();
        argos4j = new Argos4j(settings);

    }

    private static Integer findRandomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @Test
    void storeMetablockLinkForDirectory() {
        wireMockServer.stubFor(get(urlEqualTo("/api/supplychain?name=supplyChainName")).willReturn(ok().withBody("[{\"name\":\"supplyChainName\",\"id\":\"supplyChainId\"}]")));
        wireMockServer.stubFor(post(urlEqualTo("/api/supplychain/supplyChainId/link")).willReturn(noContent()));
        wireMockServer.stubFor(get(urlEqualTo("/api/key/" + keyId)).willReturn(ok().withBody(restKeyPairRest)));
        argos4j.collectMaterials(sharedTempDir.getAbsoluteFile());
        argos4j.collectProducts(sharedTempDir.getAbsoluteFile());
        argos4j.store(KEY_PASSPHRASE);
        List<LoggedRequest> requests = wireMockServer.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertThat(requests, hasSize(3));
        assertThat(requests.get(2).getBodyAsString(), endsWith(",\"link\":{\"runId\":\"runId\",\"stepName\":\"build\",\"layoutSegmentName\":\"layoutSegmentName\",\"command\":[],\"materials\":[{\"uri\":\"text.txt\",\"hash\":\"cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91\"}],\"products\":[{\"uri\":\"text.txt\",\"hash\":\"cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91\"}]}}"));
    }

    @Test
    void storeMetablockLinkForDirectoryFailed() {
        wireMockServer.stubFor(get(urlEqualTo("/api/supplychain?name=supplyChainName")).willReturn(ok().withBody("[{\"name\":\"supplyChainName\",\"id\":\"supplyChainId\"}]")));
        wireMockServer.stubFor(get(urlEqualTo("/api/key/" + keyId)).willReturn(ok().withBody(restKeyPairRest)));
        wireMockServer.stubFor(post(urlEqualTo("/api/supplychain/supplyChainId/link")).willReturn(serverError()));
        Argos4jError error = assertThrows(Argos4jError.class, () -> argos4j.store(KEY_PASSPHRASE));
        assertThat(error.getMessage(), is("500 "));
    }

    @Test
    void storeMetaBlockLinkForDirectoryUnexpectedResponse() {
        wireMockServer.stubFor(get(urlEqualTo("/api/key/" + keyId)).willReturn(ok().withBody(restKeyPairRest)));
        wireMockServer.stubFor(get(urlEqualTo("/api/supplychain?name=supplyChainName")).willReturn(badRequest()));
        Argos4jError error = assertThrows(Argos4jError.class, () -> argos4j.store(KEY_PASSPHRASE));
        assertThat(error.getMessage(), is("400 "));
    }

    @Test
    void storeMetaBlockLinkForDirectoryUnknownKeyId() {
        wireMockServer.stubFor(get(urlEqualTo("/api/key/" + keyId)).willReturn(notFound()));
        Argos4jError error = assertThrows(Argos4jError.class, () -> argos4j.store(KEY_PASSPHRASE));
        assertThat(error.getMessage(), is("404 "));
    }
}
