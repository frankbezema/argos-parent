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
package com.rabobank.argos.argos4j;

import com.rabobank.argos.argos4j.internal.ArtifactListBuilderImpl;
import com.rabobank.argos.argos4j.internal.LinkBuilderImpl;
import com.rabobank.argos.argos4j.internal.VerifyBuilderImpl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@RequiredArgsConstructor
@EqualsAndHashCode
public class Argos4j implements Serializable {

    @Getter
    private final Argos4jSettings settings;

    public LinkBuilder getLinkBuilder(LinkBuilderSettings linkBuilderSettings) {
        return new LinkBuilderImpl(settings, linkBuilderSettings);
    }

    public VerifyBuilder getVerifyBuilder() {
        return new VerifyBuilderImpl(settings, getArtifactListBuilder());
    }
    
    public static ArtifactListBuilder getArtifactListBuilder() {
        return new ArtifactListBuilderImpl();
    }

    public static String getVersion() {
        return VersionInfo.getInfo();
    }
}
