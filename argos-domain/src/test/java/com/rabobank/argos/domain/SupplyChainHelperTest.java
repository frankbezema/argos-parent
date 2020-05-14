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
package com.rabobank.argos.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class SupplyChainHelperTest {

    @Test
    void getSupplyChainNameShouldGiveName() {
        assertThat(SupplyChainHelper.getSupplyChainName("label1.label2:name"), is("name"));
    }
    
    @Test
    void getSupplyChainNameShouldGiveException() {
    	Throwable exception = assertThrows(ArgosError.class, () -> {
    		SupplyChainHelper.getSupplyChainName("label-1.label-2:name"); 
          });
    	assertEquals("[label-1.label-2:name] not correct should be <label>.<label>:<supplyChainName> with Java package rules", exception.getMessage());
    }
    
    @Test
    void getSupplyChainPathShouldReturnPath() {
        assertThat(SupplyChainHelper.getSupplyChainPath("label1.label2:name"), is(Arrays.asList("label1", "label2")));
    }
    
    @Test
    void getSupplyChainPathShouldGiveException() {
    	Throwable exception = assertThrows(ArgosError.class, () -> {
    		SupplyChainHelper.getSupplyChainPath("label-1.label-2:name"); 
          });
    	assertEquals("[label-1.label-2:name] not correct should be <label>.<label>:<supplyChainName> with Java package rules", exception.getMessage());
    }
}
