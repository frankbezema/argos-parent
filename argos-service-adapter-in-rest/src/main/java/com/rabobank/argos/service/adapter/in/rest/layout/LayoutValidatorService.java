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

import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.key.KeyIdProvider;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.PublicKey;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.layout.rule.MatchRule;
import com.rabobank.argos.service.adapter.in.rest.SignatureValidatorService;
import com.rabobank.argos.service.domain.account.AccountService;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class LayoutValidatorService {

    private final SupplyChainRepository supplyChainRepository;

    private final SignatureValidatorService signatureValidatorService;

    private final AccountService accountService;

    public void validate(LayoutMetaBlock layoutMetaBlock) {
        LayoutValidationReport report = new LayoutValidationReport();
        validateSegmentNamesUnique(report, layoutMetaBlock.getLayout());
        validateStepNamesUnique(report, layoutMetaBlock.getLayout());
        validateMatchRuleDestinations(report, layoutMetaBlock.getLayout());
        validateExpectedProductsHaveSameSegmentName(report, layoutMetaBlock.getLayout());
        validateSupplyChain(report, layoutMetaBlock);
        validateAutorizationKeyIds(report, layoutMetaBlock.getLayout());
        validatePublicKeys(report, layoutMetaBlock.getLayout());
        validateSignatures(report, layoutMetaBlock);

        if (!report.isValid()) {
            throwValidationException(report);
        }
    }

    public void validateLayout(Layout layout) {
        LayoutValidationReport report = new LayoutValidationReport();
        validateSegmentNamesUnique(report, layout);
        validateStepNamesUnique(report, layout);
        validateMatchRuleDestinations(report, layout);
        validateExpectedProductsHaveSameSegmentName(report, layout);
        validateAutorizationKeyIds(report, layout);
        validatePublicKeys(report, layout);
        if (!report.isValid()) {
            throwValidationException(report);
        }
    }

    private void validatePublicKeys(LayoutValidationReport report, Layout layout) {
        validatePublicKeyIds(report, layout);
        validateAuthorizedKeysWithPublicKeys(report, layout);
    }

    private void validatePublicKeyIds(LayoutValidationReport report, Layout layout) {
        layout.getKeys().forEach(key -> validatePublicKeyId(report, key));
    }

    private void validatePublicKeyId(LayoutValidationReport report, PublicKey publicKey) {
        if (!publicKey.getId().equals(KeyIdProvider.computeKeyId(publicKey.getKey()))) {
            report.addValidationMessage("layout.keys",
                    "key with id " + publicKey.getId() + " does not match computed key id from public key");
        }
    }

    private void validateAuthorizedKeysWithPublicKeys(LayoutValidationReport report, Layout layout) {
        Set<String> publicKeyIds = layout.getKeys().stream().map(PublicKey::getId).collect(toSet());
        Set<String> authorizedKeyIds = Stream.concat(layout.getAuthorizedKeyIds().stream(), layout.getLayoutSegments()
                .stream().map(LayoutSegment::getSteps).flatMap(List::stream).map(Step::getAuthorizedKeyIds)
                .flatMap(List::stream)).collect(toSet());

        if (publicKeyIds.size() != authorizedKeyIds.size() || !publicKeyIds.containsAll(authorizedKeyIds)) {
            report.addValidationMessage("layout.authorizedKeyIds",
                    "The defined Public keys are not equal to all defined Authorized keys");
        }
    }

    private void validateExpectedProductsHaveSameSegmentName(LayoutValidationReport report, Layout layout) {
        Set<String> sameSegmentNames = layout.getExpectedEndProducts()
                .stream()
                .map(MatchRule::getDestinationSegmentName)
                .collect(Collectors.toSet());
        if (sameSegmentNames.size() > 1) {
            report.addValidationMessage("layout.expectedEndProducts",
                    "segment names for expectedProducts should all be the same");
        }
    }

    private void validateStepNamesUnique(LayoutValidationReport report, Layout layout) {
        layout.getLayoutSegments().forEach(segment -> validateStepNamesUnique(report, segment));
    }

    private void validateStepNamesUnique(LayoutValidationReport report, LayoutSegment layoutSegment) {
        Set<String> stepNameSet = layoutSegment.getSteps().stream().map(Step::getName).collect(toSet());
        List<String> stepNameList = layoutSegment.getSteps().stream().map(Step::getName).collect(toList());
        if (stepNameSet.size() != stepNameList.size()) {
            report.addValidationMessage("layout.layoutSegments",
                    "step names for segment: " + layoutSegment.getName() + " are not unique");
        }
    }

    private void validateSegmentNamesUnique(LayoutValidationReport report, Layout layout) {
        Set<String> segmentNameSet = layout.getLayoutSegments().stream().map(LayoutSegment::getName).collect(toSet());
        List<String> segmentNameList = layout.getLayoutSegments().stream().map(LayoutSegment::getName).collect(toList());
        if (segmentNameSet.size() != segmentNameList.size()) {
            report.addValidationMessage("layout.layoutSegments",
                    "segment names are not unique");
        }
    }

    private void validateMatchRuleDestinations(LayoutValidationReport report, Layout layout) {
        if (!layout.getExpectedEndProducts().stream().allMatch(matchRule -> hasFilterDestination(matchRule, layout))) {
            report.addValidationMessage("layout.expectedEndProducts",
                    "expected product destination step name not found");
        }
    }

    private boolean hasFilterDestination(MatchRule matchRule, Layout layout) {
        return layout.getLayoutSegments().stream()
                .filter(layoutSegment -> layoutSegment.getName().equals(matchRule.getDestinationSegmentName()))
                .map(LayoutSegment::getSteps)
                .anyMatch(steps -> hasDestinationStepName(steps, matchRule.getDestinationStepName()));
    }

    private boolean hasDestinationStepName(List<Step> steps, String destinationStepName) {
        return steps.stream().anyMatch(step -> step.getName().equals(destinationStepName));
    }

    private void validateSupplyChain(LayoutValidationReport report, LayoutMetaBlock layoutMetaBlock) {
        if (!supplyChainRepository.exists(layoutMetaBlock.getSupplyChainId())) {
            report
                    .addValidationMessage("layout.supplychain",
                            "supply chain not found : " + layoutMetaBlock.getSupplyChainId());
        }
    }

    private void validateSignatures(LayoutValidationReport report, LayoutMetaBlock layoutMetaBlock) {
        Set<String> uniqueKeyIds = layoutMetaBlock.getSignatures().stream().map(Signature::getKeyId).collect(toSet());
        if (layoutMetaBlock.getSignatures().size() != uniqueKeyIds.size()) {
            report.addValidationMessage("signatures",
                    "layout can't be signed more than one time with the same keyId");
        }

        layoutMetaBlock.getSignatures()
                .forEach(signature -> signatureValidatorService.validateSignature(layoutMetaBlock.getLayout(), signature));
    }

    private void validateAutorizationKeyIds(LayoutValidationReport report, Layout layout) {
        layout.getAuthorizedKeyIds().forEach(keyid -> keyExists(report, keyid));
        layout.getLayoutSegments().forEach(layoutSegment -> layoutSegment.getSteps()
                .forEach(step -> step.getAuthorizedKeyIds()
                        .forEach(keyid -> keyExists(report, keyid))));
    }

    private void keyExists(LayoutValidationReport report, String keyId) {
        if (!accountService.keyPairExists(keyId)) {
            report
                    .addValidationMessage("layout.keys",
                            "keyId " + keyId + " not found");
        }
    }

    private void throwValidationException(LayoutValidationReport report) {
        throw LayoutValidationException
                .builder()
                .validationMessages(report.validationMessages)
                .build();
    }

    @Getter
    public static class LayoutValidationReport {
        private Map<String, List<String>> validationMessages = new HashMap<>();

        private void addValidationMessage(String field, String message) {
            validationMessages.computeIfPresent(field, (key, messages) -> {
                messages.add(message);
                return messages;
            });
            validationMessages.computeIfAbsent(field,
                    k -> new ArrayList<>(singletonList(message)));
        }

        private boolean isValid() {
            return validationMessages == null || validationMessages.isEmpty();
        }
    }
}
