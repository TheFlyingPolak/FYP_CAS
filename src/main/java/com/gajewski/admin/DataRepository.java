package com.gajewski.admin;

import com.gajewski.structures.AgentResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DataRepository extends MongoRepository<AgentResponse, Long> {
    public AgentResponse findByAgentId(Long agentId);
}
