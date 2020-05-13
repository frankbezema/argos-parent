package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.service.adapter.in.rest.api.model.RestApprovalConfiguration;
import org.junit.jupiter.api.Test;

import static com.rabobank.argos.service.adapter.in.rest.ValidateHelper.expectedErrors;
import static com.rabobank.argos.service.adapter.in.rest.ValidateHelper.validate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class RestApprovalConfigurationTest {
    @Test
    void emptyRestLayoutMetaBlock() {
        assertThat(validate(new RestApprovalConfiguration()), contains(expectedErrors(
                "segmentName", "must not be null",
                "stepName", "must not be null")));
    }

}
