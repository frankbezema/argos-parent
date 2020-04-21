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

import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayout;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.service.domain.layout.LayoutMetaBlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LayoutRestServiceTest {

    private static final String SUPPLY_CHAIN_ID = "supplyChainId";

    @Mock
    private LayoutMetaBlockMapper converter;

    @Mock
    private LayoutMetaBlockRepository repository;

    @Mock
    private RestLayoutMetaBlock restLayoutMetaBlock;

    @Mock
    private RestLayout restLayout;

    @Mock
    private Layout layout;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private HttpServletRequest httpServletRequest;

    private LayoutRestService service;

    @Mock
    private LayoutValidatorService validator;

    @BeforeEach
    void setUp() {
        service = new LayoutRestService(converter, repository, validator);
    }

    @Test
    void createOrUpdateLayout() {
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(converter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock)).thenReturn(layoutMetaBlock);
        when(converter.convertToRestLayoutMetaBlock(layoutMetaBlock)).thenReturn(restLayoutMetaBlock);

        ResponseEntity<RestLayoutMetaBlock> responseEntity = service.createOrUpdateLayout(SUPPLY_CHAIN_ID, restLayoutMetaBlock);

        assertThat(responseEntity.getStatusCodeValue(), is(201));
        assertThat(responseEntity.getBody(), sameInstance(restLayoutMetaBlock));
        assertThat(responseEntity.getHeaders().getLocation().getPath(), is(""));

        verify(repository).createOrUpdate(layoutMetaBlock);
        verify(validator).validate(layoutMetaBlock);

    }

    @Test
    void validateLayoutValid() {
        when(converter.convertFromRestLayout(restLayout)).thenReturn(layout);
        ResponseEntity responseEntity = service.validateLayout(SUPPLY_CHAIN_ID, restLayout);
        assertThat(responseEntity.getStatusCodeValue(), is(204));
        verify(validator).validateLayout(layout);
    }

    @Test
    void getLayout() {
        when(converter.convertToRestLayoutMetaBlock(layoutMetaBlock)).thenReturn(restLayoutMetaBlock);
        when(repository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
        ResponseEntity<RestLayoutMetaBlock> responseEntity = service.getLayout(SUPPLY_CHAIN_ID);
        assertThat(responseEntity.getStatusCodeValue(), is(200));
        assertThat(responseEntity.getBody(), sameInstance(restLayoutMetaBlock));
    }

    @Test
    void getLayoutNotFound() {
        when(repository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> {
            service.getLayout(SUPPLY_CHAIN_ID);
        });
        assertThat(responseStatusException.getStatus(), is(HttpStatus.NOT_FOUND));
        assertThat(responseStatusException.getReason(), is("layout not found"));
    }

}
