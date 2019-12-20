package com.cooperative.assembly.vote;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VoteRepository extends MongoRepository<Vote, String> {

    public List<Vote> findByUserIdAndAgendaId(final String userId, final String agendaId);

}
