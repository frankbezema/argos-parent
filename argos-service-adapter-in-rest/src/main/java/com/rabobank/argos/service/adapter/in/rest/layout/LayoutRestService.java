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
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.adapter.in.rest.api.handler.LayoutApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayout;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.rabobank.argos.service.domain.security.LabelIdCheckParam;
import com.rabobank.argos.service.domain.security.PermissionCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static com.rabobank.argos.service.adapter.in.rest.supplychain.SupplyChainLabelIdExtractor.SUPPLY_CHAIN_LABEL_ID_EXTRACTOR;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class LayoutRestService implements LayoutApi {

    private final LayoutMetaBlockMapper converter;

    private final LayoutMetaBlockRepository repository;

    private final LayoutValidatorService validator;

    @Override
    @PermissionCheck(permissions = Permission.LAYOUT_ADD)
    public ResponseEntity validateLayout(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, RestLayout restLayout) {
        Layout layout = converter.convertFromRestLayout(restLayout);
        validator.validateLayout(layout);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @PermissionCheck(permissions = Permission.LAYOUT_ADD)
    public ResponseEntity<RestLayoutMetaBlock> createOrUpdateLayout(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, RestLayoutMetaBlock restLayoutMetaBlock) {
        log.info("createLayout for supplyChainId {}", supplyChainId);
        LayoutMetaBlock layoutMetaBlock = converter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock);
        layoutMetaBlock.setSupplyChainId(supplyChainId);
        validator.validate(layoutMetaBlock);
        repository.createOrUpdate(layoutMetaBlock);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();

        return ResponseEntity.created(location).body(converter.convertToRestLayoutMetaBlock(layoutMetaBlock));
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<RestLayoutMetaBlock> getLayout(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId) {
        return repository.findBySupplyChainId(supplyChainId)
                .map(converter::convertToRestLayoutMetaBlock)
                .map(ResponseEntity::ok).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "layout not found"));
    }

}
