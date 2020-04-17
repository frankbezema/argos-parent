package com.rabobank.argos.service.adapter.in.rest.layout;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class LayoutValidationError extends RuntimeException {
    private final Map<String, List<String>> validationMessages;
}
