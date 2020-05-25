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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.rabobank.argos.argos4j.ArtifactListBuilder;
import com.rabobank.argos.argos4j.FileCollector;
import com.rabobank.argos.domain.link.Artifact;

public class ArtifactListBuilderImpl implements ArtifactListBuilder {
	

    private List<FileCollector> fileCollectors = new ArrayList<>();

	@Override
	public void addFileCollector(FileCollector collector) {
        fileCollectors.add(collector);

	}

	@Override
	public List<Artifact> collect() {
		return fileCollectors.stream().map(ArtifactCollectorFactory::build).map(ArtifactCollector::collect).flatMap(List::stream).collect(Collectors.toList());
	}

}
