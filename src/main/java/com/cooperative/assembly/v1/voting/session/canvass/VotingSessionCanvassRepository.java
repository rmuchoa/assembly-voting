package com.cooperative.assembly.v1.voting.session.canvass;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VotingSessionCanvassRepository extends MongoRepository<VotingSessionCanvass, String> {

    Optional<VotingSessionCanvass> findBySessionId(String sessionId);

}
