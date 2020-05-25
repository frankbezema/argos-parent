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
import com.rabobank.argos.service.adapter.in.rest.api.model.RestArtifactCollectorSpecification.TypeEnum;

import java.util.Set;

import static com.rabobank.argos.service.adapter.in.rest.api.model.RestValidationMessage.TypeEnum.DATA_INPUT;
import static com.rabobank.argos.service.adapter.in.rest.layout.ValidationHelper.throwLayoutValidationException;

public abstract class ContextInputValidator {

    static ContextInputValidator of(TypeEnum type) {
        if (type == TypeEnum.XLDEPLOY) {
            return new XLDeployContextInputValidator();
        }
        throw new IllegalArgumentException("context validator for collector type: " + type + "is not implemented");

    }

    private void checkForRequiredFieldsInContext(RestArtifactCollectorSpecification restArtifactCollectorSpecification, Set<String> requiredFields) {
        if (!restArtifactCollectorSpecification
                .getContext()
                .keySet()
                .containsAll(requiredFields)) {
            throwLayoutValidationException(DATA_INPUT, "context", "required fields : "
                    + requiredFields
                    +
                    " not present for collector type: " +
                    restArtifactCollectorSpecification.getType());
        }
    }

    void validateContextFields(RestArtifactCollectorSpecification restArtifactCollectorSpecification) {
        checkForRequiredFieldsInContext(restArtifactCollectorSpecification, requiredFields());
        checkFieldsForInputConsistencyRules(restArtifactCollectorSpecification);
    }

    protected abstract Set<String> requiredFields();

    protected abstract void checkFieldsForInputConsistencyRules(RestArtifactCollectorSpecification restArtifactCollectorSpecification);


}
