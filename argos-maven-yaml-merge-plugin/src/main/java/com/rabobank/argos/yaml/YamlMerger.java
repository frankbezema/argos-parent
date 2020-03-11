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

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
class YamlMerger {

    private static final String ERROR_MESSAGE = "an error occured while merging yaml files";
    private Path sourceFilePath;
    private Path destFilePath;

    YamlMerger(final Path sourceFile, Path destinationFile) {
        this.sourceFilePath = sourceFile;
        this.destFilePath = destinationFile;
    }

    /**
     * Merge the yaml files starting with sourceFilePath
     */
    void merge() throws IOException {
        //check if dest file exists
        Path destParentDir = destFilePath.getParent();
        //create missing directories
        Files.createDirectories(destParentDir);
        Files.deleteIfExists(destFilePath);
        Files.createFile(destFilePath);
        Path parentDir = sourceFilePath.getParent();
        FileWriter fw = new FileWriter(destFilePath.toFile());
        resolve(parentDir, sourceFilePath, fw, "");
        fw.close();

    }

    /**
     * start reading the source file line by line
     * if line starts with $ref.
     * get the parentDir and file name
     * call resolve()
     * else
     * write to dest file.
     */
    private void resolve(Path parentDir, Path sourceFilePath, final FileWriter fw, String indentation) {
        try (Stream<String> input = Files.lines(sourceFilePath)) {
            input.forEach(line -> {
                //String indent="";
                if (RefUtil.isStartWithRef(line) && RefUtil.isRemoteFilepath(line)) {
                    String indent = indentation + RefUtil.getIndentation(line);
                    //get the abs file path
                    String remoteRefValue = RefUtil.getRemoteRelativeFilePath(line);
                    Path remoteFilePath = getAbsolutePath(parentDir, remoteRefValue);
                    //get the new parent Directory
                    Path newParentDir = remoteFilePath.getParent();
                    resolve(newParentDir, remoteFilePath, fw, indent);
                } else {
                    try {
                        fw.write(String.format("%s%s%n", indentation, line));
                    } catch (IOException e) {
                        log.error(ERROR_MESSAGE, e);
                    }
                }
            });

        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
        }
    }

    /**
     * this method returns the absolute path for the remote value
     */
    private Path getAbsolutePath(Path parentDir, String remoteRefValue) {
        String absFileStr = parentDir.toString() + remoteRefValue;
        return Paths.get(absFileStr);
    }


}
