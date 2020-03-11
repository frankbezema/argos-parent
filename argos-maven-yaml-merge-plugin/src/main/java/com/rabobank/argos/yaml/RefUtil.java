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

import org.apache.commons.lang3.StringUtils;

class RefUtil {
    private static final String REF = "$ref";
    private static final String SINGLE_QUOTE = "'";
    private static final String DOUBLE_QUOTE = "\"";
    private static final String HASH_SLASH = "#/";
    private static final String COLON = ":";
    private static final String DOT = ".";

    private RefUtil() {
    }

    /**
     * This checks if the line without indentation starts with $ref
     */
    static boolean isStartWithRef(String line) {
        return StringUtils.trim(line).startsWith(REF);
    }

    /**
     * This returns the indentation of the $ref line.
     */
    static String getIndentation(String line) {
        return StringUtils.substringBefore(line, REF);
    }

    /**
     * this method checks if the line is remote path or local path
     * remote path starts with ./ and local path starts with #
     * path could have single or double quotes also.
     * ex
     * remote path
     * $ref: './info/index.yaml'
     * <p>
     * local path
     * $ref: '#/definitions/Info'
     */
    static boolean isRemoteFilepath(String refLine) {
        String refPath = StringUtils.substringAfter(refLine, COLON);
        String refPathWithoutQuotes = removeQuotes(refPath);
        return !StringUtils.startsWith(refPathWithoutQuotes, HASH_SLASH);

    }

    /**
     * Ref path example could be
     * $ref: './baseobject.yaml'
     * $ref: "/baseobject.yaml"
     * $ref: /baseobject.yaml
     */
    private static String removeQuotes(String refPathValue) {
        String ret;
        String trimRefValue = StringUtils.trim(refPathValue);
        if (StringUtils.startsWith(trimRefValue, SINGLE_QUOTE)) {
            //get the string between "'"
            ret = StringUtils.substringBetween(trimRefValue, SINGLE_QUOTE);
        } else if (StringUtils.startsWith(trimRefValue, DOUBLE_QUOTE)) {
            ret = StringUtils.substringBetween(trimRefValue, DOUBLE_QUOTE);
        } else {
            ret = trimRefValue;
        }

        return ret;
    }


    //Get the remote
    static String getRemoteRelativeFilePath(String refLine) {
        String relFileString;
        String refPath = StringUtils.substringAfter(refLine, COLON);
        relFileString = removeQuotes(refPath);
        if (StringUtils.startsWith(relFileString, DOT)) {
            relFileString = StringUtils.stripStart(relFileString, DOT);
        }

        return relFileString;
    }


}
