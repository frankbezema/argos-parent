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

import com.rabobank.argos.service.adapter.in.rest.api.model.RestArtifactCollectorSpecification;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rabobank.argos.service.adapter.in.rest.api.model.RestValidationMessage.TypeEnum.DATA_INPUT;
import static com.rabobank.argos.service.adapter.in.rest.layout.ValidationHelper.throwLayoutValidationException;

public class XLDeployContextInputValidator extends ContextInputValidator {


    private static final String APPLICATION_NAME = "applicationName";
    //(no `/`, `\`, `:`, `[`, `]`, `|`, `,` or `*`)
    private static final Pattern invalidCharacters = Pattern.compile("[/\\\\:\\[\\]|,*\\]]");
    private static final int MAX_LENGTH = 255;

    XLDeployContextInputValidator() {
    }

    @Override
    protected Set<String> requiredFields() {
        return Set.of(APPLICATION_NAME);
    }

    @Override
    protected void checkFieldsForInputConsistencyRules(RestArtifactCollectorSpecification restArtifactCollectorSpecification) {
        String applicationNameValue = restArtifactCollectorSpecification.getContext().get(APPLICATION_NAME);
        Matcher m = invalidCharacters.matcher(applicationNameValue);
        if (m.find()) {
            throwLayoutValidationException(DATA_INPUT, APPLICATION_NAME,
                    "(no `/`, `\\`, `:`, `[`, `]`, `|`, `,` or `*`) characters are allowed");
        }
        if (applicationNameValue.length() > MAX_LENGTH) {
            throwLayoutValidationException(DATA_INPUT, APPLICATION_NAME,
                    "applicationName is to long "
                            + applicationNameValue.length() +
                            " only "
                            + MAX_LENGTH + " is allowed");

        }
    }
}
