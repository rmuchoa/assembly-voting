package com.cooperative.assembly.voting.meeting.agenda;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MeetingAgendaRepository extends MongoRepository<MeetingAgenda, String> {

    public Optional<MeetingAgenda> findById(String id);
    public Optional<MeetingAgenda> findByTitle(String title);

}
