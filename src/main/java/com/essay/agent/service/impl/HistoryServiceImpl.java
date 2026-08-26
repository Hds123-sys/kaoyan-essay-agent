package com.essay.agent.service.impl;

import com.essay.agent.entity.EssayRecord;
import com.essay.agent.service.HistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HistoryServiceImpl implements HistoryService {

    private final MongoTemplate mongoTemplate;

    public HistoryServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public EssayRecord saveRecord(EssayRecord record) {
        return mongoTemplate.save(record);
    }

    @Override
    public EssayRecord getById(Long id) {
        return mongoTemplate.findById(id.toString(), EssayRecord.class);
    }

    @Override
    public Page<EssayRecord> pageBySessionId(String sessionId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Query query = Query.query(Criteria.where("sessionId").is(sessionId)).with(pageable);
        return mongoTemplate.find(query, EssayRecord.class);
    }
}