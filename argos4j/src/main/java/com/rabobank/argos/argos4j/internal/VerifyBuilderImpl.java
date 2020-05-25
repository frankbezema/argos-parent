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
package com.rabobank.argos.argos4j.internal;

import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.ArtifactListBuilder;
import com.rabobank.argos.argos4j.FileCollector;
import com.rabobank.argos.argos4j.VerificationResult;
import com.rabobank.argos.argos4j.VerifyBuilder;
import com.rabobank.argos.domain.link.Artifact;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class VerifyBuilderImpl implements VerifyBuilder {

    private final Argos4jSettings settings;

    private final ArtifactListBuilder artifactListBuilder;

    @Override
    public VerifyBuilder addFileCollector(FileCollector collector) {
    	artifactListBuilder.addFileCollector(collector);
        return this;
    }

    @Override
    public VerificationResult verify(char[] keyPassphrase) {
        List<Artifact> artifacts = artifactListBuilder.collect();
        log.info("verify artifacts {}", artifacts);
        return new ArgosServiceClient(settings, keyPassphrase).verify(artifacts);
    }

}
