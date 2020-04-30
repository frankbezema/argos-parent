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

import com.fasterxml.jackson.databind.JsonMappingException;
import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestError;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestValidationError;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestValidationMessage;
import com.rabobank.argos.service.adapter.in.rest.layout.LayoutValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.rabobank.argos.service.adapter.in.rest.api.model.RestValidationMessage.TypeEnum.DATA_INPUT;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RestServiceExceptionHandler {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<RestValidationError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        List<RestValidationMessage> validationMessages = ex.getBindingResult().getAllErrors()
                .stream()
                .filter(FieldError.class::isInstance)
                .map(error -> new RestValidationMessage()
                        .field(((FieldError) error).getField())
                        .message(error.getDefaultMessage())
                        .type(DATA_INPUT)
                )
                .collect(Collectors.toList());

        sortValidationMessages(validationMessages);

        RestValidationError restValidationError = new RestValidationError().messages(validationMessages);
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(restValidationError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RestValidationError> handleConstraintViolationException(
            ConstraintViolationException ex) {
        List<RestValidationMessage> validationMessages = ex.getConstraintViolations()
                .stream()
                .map(error -> new RestValidationMessage()
                        .field(error.getPropertyPath().toString())
                        .message(error.getMessage())
                        .type(DATA_INPUT)
                )
                .collect(Collectors.toList());
        sortValidationMessages(validationMessages);
        RestValidationError restValidationError = new RestValidationError().messages(validationMessages);
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(restValidationError);
    }

    @ExceptionHandler(value = {JsonMappingException.class})
    public ResponseEntity<RestValidationError> handleJsonMappingException(JsonMappingException ex) {
        log.info("error with json {}", ex);
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(createValidationError("invalid json"));
    }

    @ExceptionHandler(value = {LayoutValidationException.class})
    public ResponseEntity<RestValidationError> handleLayoutValidationException(LayoutValidationException ex) {
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(createValidationError(ex));
    }


    @ExceptionHandler(value = {ResponseStatusException.class})
    public ResponseEntity handleResponseStatusException(ResponseStatusException ex) {
        if (BAD_REQUEST == ex.getStatus()) {
            return ResponseEntity.status(ex.getStatus()).contentType(APPLICATION_JSON).body(createValidationError(ex.getReason()));
        } else {
            return ResponseEntity.status(ex.getStatus()).contentType(APPLICATION_JSON).body(createRestErrorMessage(ex.getReason()));
        }
    }

    @ExceptionHandler(value = {ArgosError.class})
    public ResponseEntity handleArgosError(ArgosError ex) {
        if (ex.getLevel() == ArgosError.Level.WARNING) {
            log.debug("{}", ex.getMessage(), ex);
            return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(createValidationError(ex.getMessage()));
        } else {
            log.error("{}", ex.getMessage(), ex);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).contentType(APPLICATION_JSON).body(createRestErrorMessage(ex.getMessage()));
        }
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<RestError> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(FORBIDDEN).contentType(APPLICATION_JSON).body(createRestErrorMessage(ex.getMessage()));
    }

    private RestValidationError createValidationError(LayoutValidationException ex) {
        RestValidationError restValidationError = new RestValidationError();
        List<RestValidationMessage> validationMessages = ex.getValidationMessages();
        sortValidationMessages(validationMessages);
        restValidationError.setMessages(validationMessages);
        return restValidationError;
    }

    private void sortValidationMessages(List<RestValidationMessage> validationMessages) {
        Collections.sort(validationMessages, Comparator
                .comparing(RestValidationMessage::getField)
                .thenComparing(RestValidationMessage::getMessage));
    }

    private RestValidationError createValidationError(String reason) {
        return new RestValidationError()
                .messages(singletonList(new RestValidationMessage()
                        .message(reason)
                        .type(DATA_INPUT)));
    }

    private RestError createRestErrorMessage(String message) {
        return new RestError().message(message);
    }

}
