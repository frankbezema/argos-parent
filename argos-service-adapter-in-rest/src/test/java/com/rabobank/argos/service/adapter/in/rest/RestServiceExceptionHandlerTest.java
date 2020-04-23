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
package com.rabobank.argos.service.adapter.in.rest;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestError;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestValidationError;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestValidationMessage;
import com.rabobank.argos.service.adapter.in.rest.layout.LayoutValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.rabobank.argos.service.adapter.in.rest.api.model.RestValidationMessage.TypeEnum.MODEL_CONSISTENCY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestServiceExceptionHandlerTest {

    private RestServiceExceptionHandler handler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;


    @Mock
    private ConstraintViolationException constraintViolationException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private ConstraintViolation constraintViolation;

    @Mock
    private Path path;

    @Mock
    private FieldError fieldError;

    @Mock
    private ResponseStatusException responseStatusException;

    @Mock
    private LayoutValidationException layoutValidationException;

    @Mock
    private ArgosError argosError;

    @BeforeEach
    void setUp() {
        handler = new RestServiceExceptionHandler();
    }

    @Test
    void handleMethodArgumentNotValidException() {
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));
        when(fieldError.getField()).thenReturn("field");
        when(fieldError.getDefaultMessage()).thenReturn("message");
        ResponseEntity<RestValidationError> response = handler.handleMethodArgumentNotValidException(methodArgumentNotValidException);
        assertThat(response.getStatusCodeValue(), is(400));
        assertThat(response.getBody().getMessages().get(0).getField(), is("field"));
        assertThat(response.getBody().getMessages().get(0).getMessage(), is("message"));
        assertThat(response.getBody().getMessages().get(0).getType(), is(RestValidationMessage.TypeEnum.DATA_INPUT));
    }

    @Test
    void handleLayoutValidationException() {
        when(layoutValidationException.getValidationMessages())
                .thenReturn(new ArrayList(List.of(new RestValidationMessage().field("key").message("message").type(MODEL_CONSISTENCY))));
        ResponseEntity<RestValidationError> response = handler.handleLayoutValidationException(layoutValidationException);
        assertThat(response.getStatusCodeValue(), is(400));
        assertThat(response.getBody().getMessages().get(0).getField(), is("key"));
        assertThat(response.getBody().getMessages().get(0).getMessage(), is("message"));
        assertThat(response.getBody().getMessages().get(0).getType(), is(MODEL_CONSISTENCY));
    }

    @Test
    void handleConstraintViolationException() {
        when(constraintViolationException.getConstraintViolations()).thenReturn(Set.of(constraintViolation));
        when(constraintViolation.getPropertyPath()).thenReturn(path);
        when(constraintViolation.getMessage()).thenReturn("message");
        when(path.toString()).thenReturn("field");
        ResponseEntity<RestValidationError> response = handler.handleConstraintViolationException(constraintViolationException);
        assertThat(response.getStatusCodeValue(), is(400));
        assertThat(response.getBody().getMessages().get(0).getField(), is("field"));
        assertThat(response.getBody().getMessages().get(0).getMessage(), is("message"));
        assertThat(response.getBody().getMessages().get(0).getType(), is(RestValidationMessage.TypeEnum.DATA_INPUT));
    }


    @Test
    void handleJsonMappingException() {
        ResponseEntity<RestValidationError> response = handler.handleJsonMappingException();
        assertThat(response.getStatusCodeValue(), is(400));
        assertThat(response.getBody().getMessages().get(0).getMessage(), is("invalid json"));
        assertThat(response.getBody().getMessages().get(0).getType(), is(RestValidationMessage.TypeEnum.DATA_INPUT));
    }

    @Test
    void handleResponseStatusException() {
        when(responseStatusException.getStatus()).thenReturn(HttpStatus.NOT_FOUND);
        when(responseStatusException.getReason()).thenReturn("reason");
        ResponseEntity<RestError> response = (ResponseEntity<RestError>) handler.handleResponseStatusException(responseStatusException);
        assertThat(response.getStatusCodeValue(), is(404));
        assertThat(response.getBody().getMessage(), is("reason"));
    }

    @Test
    void handleArgosErrorERROR() {
        when(argosError.getLevel()).thenReturn(ArgosError.Level.ERROR);
        when(argosError.getMessage()).thenReturn("message");
        ResponseEntity<RestError> response = (ResponseEntity<RestError>) handler.handleArgosError(argosError);
        assertThat(response.getStatusCodeValue(), is(500));
        assertThat(response.getBody().getMessage(), is("message"));
    }

    @Test
    void handleArgosErrorWARNING() {
        when(argosError.getLevel()).thenReturn(ArgosError.Level.WARNING);
        when(argosError.getMessage()).thenReturn("message");
        ResponseEntity<RestValidationError> response = (ResponseEntity<RestValidationError>) handler.handleArgosError(argosError);
        assertThat(response.getStatusCodeValue(), is(400));
        assertThat(response.getBody().getMessages().get(0).getMessage(), is("message"));
        assertThat(response.getBody().getMessages().get(0).getType(), is(RestValidationMessage.TypeEnum.DATA_INPUT));
    }
}
