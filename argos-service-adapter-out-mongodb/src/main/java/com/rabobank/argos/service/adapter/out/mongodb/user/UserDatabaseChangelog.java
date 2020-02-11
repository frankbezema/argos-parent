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
package com.rabobank.argos.service.adapter.out.mongodb.user;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import static com.rabobank.argos.service.adapter.out.mongodb.user.UserRepositoryImpl.COLLECTION;
import static com.rabobank.argos.service.adapter.out.mongodb.user.UserRepositoryImpl.EMAIL;
import static com.rabobank.argos.service.adapter.out.mongodb.user.UserRepositoryImpl.USER_ID;


@ChangeLog
public class UserDatabaseChangelog {

    @ChangeSet(order = "001", id = "UserDatabaseChangelog-1", author = "bart")
    public void addIndex(MongoTemplate template) {
        template.indexOps(COLLECTION).ensureIndex(new Index(USER_ID, Sort.Direction.ASC).unique());
        template.indexOps(COLLECTION).ensureIndex(new Index(EMAIL, Sort.Direction.ASC).unique());
    }
}