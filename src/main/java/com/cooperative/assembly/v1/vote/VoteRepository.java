package com.cooperative.assembly.v1.vote;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VoteRepository extends MongoRepository<Vote, String> {

    List<Vote> findByUserIdAndSessionId(final String userId, final String sessionId);

}
