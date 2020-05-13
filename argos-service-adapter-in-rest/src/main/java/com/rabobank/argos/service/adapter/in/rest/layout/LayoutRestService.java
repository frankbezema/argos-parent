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

import com.rabobank.argos.domain.layout.ApprovalConfiguration;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.adapter.in.rest.api.handler.LayoutApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestApprovalConfiguration;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayout;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestValidationMessage;
import com.rabobank.argos.service.domain.layout.ApprovalConfigurationRepository;
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

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rabobank.argos.service.adapter.in.rest.api.model.RestValidationMessage.TypeEnum.MODEL_CONSISTENCY;
import static com.rabobank.argos.service.adapter.in.rest.supplychain.SupplyChainLabelIdExtractor.SUPPLY_CHAIN_LABEL_ID_EXTRACTOR;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class LayoutRestService implements LayoutApi {

    private static final String SEGMENT_NAME = "segmentName";
    private final LayoutMetaBlockMapper layoutMetaBlockConverter;
    private final LayoutMetaBlockRepository layoutMetaBlockRepository;
    private final LayoutValidatorService validator;
    private final ApprovalConfigurationRepository approvalConfigurationRepository;
    private final ApprovalConfigurationMapper approvalConfigurationConverter;


    @Override
    @PermissionCheck(permissions = Permission.LAYOUT_ADD)
    public ResponseEntity<Void> validateLayout(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, RestLayout restLayout) {
        Layout layout = layoutMetaBlockConverter.convertFromRestLayout(restLayout);
        validator.validateLayout(layout);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @PermissionCheck(permissions = Permission.LAYOUT_ADD)
    public ResponseEntity<RestLayoutMetaBlock> createOrUpdateLayout(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, RestLayoutMetaBlock restLayoutMetaBlock) {
        log.info("createLayout for supplyChainId {}", supplyChainId);
        LayoutMetaBlock layoutMetaBlock = layoutMetaBlockConverter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock);
        layoutMetaBlock.setSupplyChainId(supplyChainId);
        validator.validate(layoutMetaBlock);
        layoutMetaBlockRepository.createOrUpdate(layoutMetaBlock);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(layoutMetaBlockConverter.convertToRestLayoutMetaBlock(layoutMetaBlock));
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<RestLayoutMetaBlock> getLayout(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId) {
        return layoutMetaBlockRepository.findBySupplyChainId(supplyChainId)
                .map(layoutMetaBlockConverter::convertToRestLayoutMetaBlock)
                .map(ResponseEntity::ok).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "layout not found"));
    }

    @Override
    @PermissionCheck(permissions = Permission.LAYOUT_ADD)
    public ResponseEntity<List<RestApprovalConfiguration>> createApprovalConfigurations(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, @Valid List<RestApprovalConfiguration> restApprovalConfigurations) {
        List<ApprovalConfiguration> approvalConfigurations = restApprovalConfigurations.stream()
                .map(restApprovalConfiguration -> convertAndValidate(supplyChainId, restApprovalConfiguration))
                .collect(Collectors.toList());
        approvalConfigurationRepository.saveAll(supplyChainId, approvalConfigurations);
        return ResponseEntity.ok(approvalConfigurations.stream()
                .map(approvalConfigurationConverter::convertToRestApprovalConfiguration)
                .collect(Collectors.toList()));

    }

    private ApprovalConfiguration convertAndValidate(String supplyChainId, RestApprovalConfiguration restApprovalConfiguration) {
        ApprovalConfiguration approvalConfiguration = approvalConfigurationConverter.convertFromRestApprovalConfiguration(restApprovalConfiguration);
        approvalConfiguration.setSupplyChainId(supplyChainId);
        verifyStepNameAndSegmentNameExistInLayout(approvalConfiguration);
        return approvalConfiguration;
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<RestApprovalConfiguration> getApprovalConfiguration(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, String approvalConfigurationId) {
        return approvalConfigurationRepository.findById(approvalConfigurationId)
                .map(approvalConfigurationConverter::convertToRestApprovalConfiguration)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "approval configuration not found"));

    }

    @Override
    @PermissionCheck(permissions = Permission.LAYOUT_ADD)
    public ResponseEntity<RestApprovalConfiguration> updateApprovalConfiguration(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, String approvalConfigurationId, @Valid RestApprovalConfiguration restApprovalConfiguration) {
        ApprovalConfiguration approvalConfiguration = approvalConfigurationConverter.convertFromRestApprovalConfiguration(restApprovalConfiguration);
        approvalConfiguration.setSupplyChainId(supplyChainId);
        approvalConfiguration.setApprovalConfigurationId(approvalConfigurationId);
        verifyStepNameAndSegmentNameExistInLayout(approvalConfiguration);
        return approvalConfigurationRepository.update(approvalConfiguration)
                .map(approvalConfigurationConverter::convertToRestApprovalConfiguration)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "approval configuration not found"));

    }

    @Override
    @PermissionCheck(permissions = Permission.LAYOUT_ADD)
    public ResponseEntity<Void> deleteApprovalConfiguration(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, String approvalConfigurationId) {
        approvalConfigurationRepository.delete(approvalConfigurationId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<List<RestApprovalConfiguration>> getApprovalConfigurations(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId) {
        return ResponseEntity.ok(approvalConfigurationRepository
                .findBySupplyChainId(supplyChainId)
                .stream()
                .map(approvalConfigurationConverter::convertToRestApprovalConfiguration)
                .collect(Collectors.toList()));
    }


    private void verifyStepNameAndSegmentNameExistInLayout(ApprovalConfiguration approvalConfiguration) {
        Map<String, Set<String>> segmentStepNameCombinations = getSegmentsAndSteps(approvalConfiguration);
        if (segmentNameIsNotPresentInLayout(approvalConfiguration, segmentStepNameCombinations)) {
            throwLayoutValidationException(
                    SEGMENT_NAME,
                    "segment with name : " + approvalConfiguration.getSegmentName() + " does not exist in layout"
            );
        } else if (stepNameIsNotPresentInSegment(approvalConfiguration, segmentStepNameCombinations)) {
            throwLayoutValidationException(
                    "stepName",
                    "step with name: " + approvalConfiguration.getStepName() + " in segment: " + approvalConfiguration.getSegmentName() + " does not exist in layout"
            );
        }
    }

    private Map<String, Set<String>> getSegmentsAndSteps(ApprovalConfiguration approvalConfiguration) {
        return layoutMetaBlockRepository.findBySupplyChainId(approvalConfiguration.getSupplyChainId())
                .map(layoutMetaBlock -> layoutMetaBlock
                        .getLayout().getLayoutSegments()
                        .stream()
                        .collect(Collectors
                                .toMap(LayoutSegment::getName,
                                        segment -> segment.getSteps()
                                                .stream().map(Step::getName)
                                                .collect(Collectors.toSet())))
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "layout not found"));
    }

    private static boolean stepNameIsNotPresentInSegment(ApprovalConfiguration approvalConfiguration, Map<String, Set<String>> segmentStepNameCombinations) {
        return !segmentStepNameCombinations
                .get(approvalConfiguration.getSegmentName())
                .contains(approvalConfiguration.getStepName());
    }

    private static boolean segmentNameIsNotPresentInLayout(ApprovalConfiguration approvalConfiguration, Map<String, Set<String>> segmentStepNameCombinations) {
        return !segmentStepNameCombinations.containsKey(approvalConfiguration.getSegmentName());
    }

    private void throwLayoutValidationException(String field, String message) {
        throw LayoutValidationException
                .builder()
                .validationMessages(List
                        .of(new RestValidationMessage()
                                .type(MODEL_CONSISTENCY)
                                .field(field)
                                .message(message)
                        ))
                .build();
    }

}
