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
package com.rabobank.argos.service.adapter.out.mongodb.layout;

import com.github.mongobee.Mongobee;
import com.github.mongobee.exception.MongobeeException;
import com.mongodb.client.MongoClients;
import com.rabobank.argos.domain.layout.ApprovalConfiguration;
import com.rabobank.argos.service.domain.layout.ApprovalConfigurationRepository;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.Optional;

import static de.flapdoodle.embed.process.config.io.ProcessOutput.getDefaultInstanceSilent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApprovalConfigurationRepositoryIT {
    public static final String SEGMENT_NAME = "segmentName";
    public static final String STEP_NAME = "stepName";
    public static final String SUPPLY_CHAIN_ID = "supplyChainId";
    private MongodExecutable mongodExecutable;

    private ApprovalConfigurationRepository approvalConfigurationRepository;

    @BeforeAll
    void setup() throws IOException, MongobeeException {
        String ip = "localhost";
        int port = Network.getFreeServerPort();
        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaults(Command.MongoD).processOutput(getDefaultInstanceSilent()).build();
        MongodStarter starter = MongodStarter.getInstance(runtimeConfig);
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
        String connectionString = "mongodb://localhost:" + port;
        MongoTemplate mongoTemplate = new MongoTemplate(MongoClients.create(connectionString), "test");
        approvalConfigurationRepository = new ApprovalConfigurationRepositoryImpl(mongoTemplate);
        Mongobee runner = new Mongobee(connectionString);
        runner.setChangeLogsScanPackage("com.rabobank.argos.service.adapter.out.mongodb.layout");
        runner.setMongoTemplate(mongoTemplate);
        runner.setDbName("test");
        runner.execute();
        loadData();
    }

    @Test
    void findBySupplyChainIdSegmentNameAndStepName() {
        Optional<ApprovalConfiguration> approvalConfiguration = approvalConfigurationRepository
                .findBySupplyChainIdSegmentNameAndStepName(SUPPLY_CHAIN_ID, SEGMENT_NAME, STEP_NAME);
        assertThat(approvalConfiguration.isPresent(), is(true));
    }

    private void loadData() {
        approvalConfigurationRepository.save(ApprovalConfiguration
                .builder()
                .approvalConfigurationId("uuid")
                .segmentName(SEGMENT_NAME)
                .stepName(STEP_NAME)
                .supplyChainId(SUPPLY_CHAIN_ID)
                .build());
    }

    @AfterAll
    void clean() {
        mongodExecutable.stop();
    }
}
