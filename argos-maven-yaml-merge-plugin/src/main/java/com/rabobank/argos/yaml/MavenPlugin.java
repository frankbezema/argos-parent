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
package com.rabobank.argos.yaml;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@Mojo(name = "merge-yaml")
public class MavenPlugin extends AbstractMojo {
    @Parameter(property = "merge-yaml.inputFile", readonly = true, required = true)
    private File inputFile;
    @Parameter(property = "merge-yaml.outPutFile", readonly = true, required = true)
    private File outPutFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Path inputFilePath = inputFile.toPath();
        Path outPutFilePath = outPutFile.toPath();
        YamlMerger yamlMerger = new YamlMerger(inputFilePath, outPutFilePath);
        try {
            yamlMerger.merge();
        } catch (IOException e) {
            getLog().error("an error occurred while merging yaml files", e);
            throw new MojoFailureException("an error occurred while merging yaml files");
        }
    }
}
