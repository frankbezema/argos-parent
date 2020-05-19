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
package com.rabobank.argos.service.adapter.in.rest.approval;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.domain.layout.ApprovalConfiguration;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.adapter.in.rest.api.handler.ApprovalApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestApprovalConfiguration;
import com.rabobank.argos.service.adapter.in.rest.layout.ApprovalConfigurationMapper;
import com.rabobank.argos.service.domain.layout.ApprovalConfigurationRepository;
import com.rabobank.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.rabobank.argos.service.domain.security.AccountSecurityContext;
import com.rabobank.argos.service.domain.security.LabelIdCheckParam;
import com.rabobank.argos.service.domain.security.PermissionCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.rabobank.argos.service.adapter.in.rest.supplychain.SupplyChainLabelIdExtractor.SUPPLY_CHAIN_LABEL_ID_EXTRACTOR;
import static java.util.Collections.emptyList;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApprovalService implements ApprovalApi {

    private final AccountSecurityContext accountSecurityContext;
    private final LayoutMetaBlockRepository layoutMetaBlockRepository;
    private final ApprovalConfigurationRepository approvalConfigurationRepository;
    private final ApprovalConfigurationMapper approvalConfigurationConverter;

    @Override
    @PermissionCheck(permissions = Permission.LINK_ADD)
    public ResponseEntity<List<RestApprovalConfiguration>> getApprovals(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId) {

        Account account = accountSecurityContext.getAuthenticatedAccount().orElseThrow(() -> new ArgosError("not logged in"));

        Optional<KeyPair> optionalKeyPair = Optional.ofNullable(account.getActiveKeyPair());
        Optional<LayoutMetaBlock> optionalLayoutMetaBlock = layoutMetaBlockRepository.findBySupplyChainId(supplyChainId);

        if (optionalKeyPair.isPresent() && optionalLayoutMetaBlock.isPresent()) {
            String activeAccountKeyId = optionalKeyPair.get().getKeyId();
            Layout layout = optionalLayoutMetaBlock.get().getLayout();
            return ok(approvalConfigurationRepository.findBySupplyChainId(supplyChainId).stream().filter(approvalConf -> canCollectFor(approvalConf, activeAccountKeyId, layout)
            ).map(approvalConfigurationConverter::convertToRestApprovalConfiguration).collect(Collectors.toList()));
        } else {
            return ok(emptyList());
        }
    }

    private boolean canCollectFor(ApprovalConfiguration approvalConf, String activeAccountKeyId, Layout layout) {
        return layout.getLayoutSegments().stream()
                .filter(layoutSegment -> layoutSegment.getName().equals(approvalConf.getSegmentName()))
                .map(LayoutSegment::getSteps).flatMap(Collection::stream)
                .filter(step -> step.getName().equals(approvalConf.getStepName()))
                .map(Step::getAuthorizedKeyIds).flatMap(Collection::stream)
                .anyMatch(authorizedKeyId -> authorizedKeyId.equals(activeAccountKeyId));

    }
}
