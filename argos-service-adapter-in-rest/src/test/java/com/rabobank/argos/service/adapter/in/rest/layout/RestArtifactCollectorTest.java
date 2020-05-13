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

import com.rabobank.argos.service.adapter.in.rest.api.model.RestArtifactCollector;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static com.rabobank.argos.service.adapter.in.rest.ValidateHelper.expectedErrors;
import static com.rabobank.argos.service.adapter.in.rest.ValidateHelper.validate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class RestArtifactCollectorTest {

    @Test
    void incorrectName() throws URISyntaxException {
        assertThat(validate(new RestArtifactCollector()
                .name("Name")
                .uri(new URI("http://uri.com"))
                .type(RestArtifactCollector.TypeEnum.XLDEPLOY)
        ), contains(expectedErrors(
                "name", "must match \"^([a-z]{1}[a-z0-9_]*)?$\"")));

    }

    @Test
    void emptyName() throws URISyntaxException {
        assertThat(validate(new RestArtifactCollector()
                .uri(new URI("http://uri.com"))
                .type(RestArtifactCollector.TypeEnum.XLDEPLOY)
        ), contains(expectedErrors(
                "name", "must not be null")));
    }

    @Test
    void emptyUri() throws URISyntaxException {
        assertThat(validate(new RestArtifactCollector()
                .name("name")
                .type(RestArtifactCollector.TypeEnum.XLDEPLOY)
        ), contains(expectedErrors(
                "uri", "must not be null")));
    }

    @Test
    void emptyType() throws URISyntaxException {
        assertThat(validate(new RestArtifactCollector()
                .name("name")
                .uri(new URI("http://uri.com"))
        ), contains(expectedErrors(
                "type", "must not be null")));
    }
}
