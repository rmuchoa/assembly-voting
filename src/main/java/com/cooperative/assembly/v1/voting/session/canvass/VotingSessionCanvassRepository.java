package com.cooperative.assembly.v1.voting.session.canvass;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface VotingSessionCanvassRepository extends MongoRepository<VotingSessionCanvass, String> {
}
