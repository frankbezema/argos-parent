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

import com.mongodb.client.result.UpdateResult;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.service.domain.layout.LayoutMetaBlockRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LayoutMetaBlockRepositoryImpl implements LayoutMetaBlockRepository {

    static final String COLLECTION = "layoutMetaBlocks";
    static final String SUPPLY_CHAIN_ID_FIELD = "supplyChainId";
    private final MongoTemplate template;

    @Override
    public Optional<LayoutMetaBlock> findBySupplyChainId(String supplyChainId) {
        return Optional.ofNullable(template.findOne(getPrimaryQuery(supplyChainId), LayoutMetaBlock.class, COLLECTION));
    }

    @Override
    public void createOrUpdate(LayoutMetaBlock layoutMetaBlock) {
        Optional<LayoutMetaBlock> optionalLayoutMetaBlock = findBySupplyChainId(layoutMetaBlock.getSupplyChainId());
        if (optionalLayoutMetaBlock.isPresent()) {
            update(layoutMetaBlock);
        } else {
            template.save(layoutMetaBlock, COLLECTION);
        }
    }

    private boolean update(LayoutMetaBlock layoutMetaBlock) {
        Document document = new Document();
        template.getConverter().write(layoutMetaBlock, document);
        UpdateResult updateResult = template.updateFirst(getPrimaryQuery(layoutMetaBlock.getSupplyChainId()), Update.fromDocument(document), LayoutMetaBlock.class, COLLECTION);
        return updateResult.getMatchedCount() > 0;
    }

    private Query getPrimaryQuery(String supplyChainId) {
        return new Query(Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId));
    }
}
