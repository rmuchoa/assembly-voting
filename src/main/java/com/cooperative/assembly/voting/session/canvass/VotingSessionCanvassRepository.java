package com.cooperative.assembly.voting.session.canvass;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface VotingSessionCanvassRepository extends MongoRepository<VotingSessionCanvass, String> {
}
