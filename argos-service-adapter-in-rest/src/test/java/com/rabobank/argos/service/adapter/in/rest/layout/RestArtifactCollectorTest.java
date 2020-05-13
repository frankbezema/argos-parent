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
