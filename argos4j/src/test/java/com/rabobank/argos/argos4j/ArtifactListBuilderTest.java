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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.hamcrest.MatcherAssert.assertThat;

import static java.util.Collections.emptyList;

import com.rabobank.argos.argos4j.internal.LocalArtifactCollector;
import com.rabobank.argos.domain.link.Artifact;

@ExtendWith(MockitoExtension.class)
class ArtifactListBuilderTest {
	
	@TempDir
    static File sharedTempDir;
	
	LocalFileCollector collector1;
	
	LocalFileCollector collector2;
	
	ArtifactListBuilder artifactListBuilder;

    private static File fileDir1;
    private static File fileDir2;
    
    Artifact artifact1 = new Artifact(sharedTempDir.getPath()+"/filedir1/text1.txt", "cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91");
    Artifact artifact2 = new Artifact(sharedTempDir.getPath()+"/filedir2/text2.txt", "c1013e3865e452515184b3db2cb812872b429c5b5cf35bafdf17ae41c02a93cf");

	@BeforeEach
	void setUp() throws Exception {		
		artifactListBuilder = Argos4j.getArtifactListBuilder();
        fileDir1 = new File(sharedTempDir, "filedir1");
        fileDir1.mkdir();
        FileUtils.write(new File(fileDir1, "text1.txt"), "cool dit\r\nan other line", "UTF-8");
        
        collector1 = LocalFileCollector.builder().path(fileDir1.toPath()).build();

        fileDir2 = new File(sharedTempDir, "filedir2");
        fileDir2.mkdir();
        FileUtils.write(new File(fileDir2, "text2.txt"), "dit is ook cool\r\nan other line", "UTF-8");
        
        collector2 = LocalFileCollector.builder().path(fileDir2.toPath()).build();
		
	}

	@Test
	void addFileCollectorAndCollectEmpty() {
		List<Artifact> artifacts = artifactListBuilder.collect();
		assertThat(artifacts, is(emptyList()));
		artifactListBuilder.addFileCollector(collector1);
		artifacts = artifactListBuilder.collect();
		assertTrue(artifacts.size() == 1);
		assertThat(artifacts, is(Arrays.asList(artifact1)));
		artifactListBuilder.addFileCollector(collector2);
		artifacts = artifactListBuilder.collect();
		assertTrue(artifacts.size() == 2);
		assertThat(artifacts, is(Arrays.asList(artifact1, artifact2)));
		
	}

}
